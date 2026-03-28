package com.example.nearbuyhq.app.startup;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.nearbuyhq.BuildConfig;
import com.example.nearbuyhq.R;
import com.example.nearbuyhq.dashboard.Dashboard;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

/**
 * Splash screen – first activity launched on app start.
 *
 * Startup sequence:
 *  1. initPlacesSdk()           – pre-warm Google Maps/Places native library
 *                                  (prevents ANR when LocationPickerActivity opens later)
 *  2. checkPermissionsAndProceed() – request runtime permissions needed by the app
 *                                  (location for map picker)
 *  3. checkConnectivityAndNavigate() – ensure internet is available
 *  4. navigate()                – verify Firebase auth state (handles deleted accounts)
 *                                  and route to Dashboard or Welcome
 */
public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS       = 2000; // minimum splash display time
    private static final int REQ_LOCATION_PERMISSION = 100;

    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean navigated = false; // guard – navigate only once
    private final Handler handler = new Handler(Looper.getMainLooper());

    // ── Lifecycle ─────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_NearBuyHQ_Splash);
        super.onCreate(savedInstanceState);

        // Full-screen immersive splash
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        setContentView(R.layout.activity_splash_screen);

        connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // ── Step 1: Pre-warm Maps + Places SDKs immediately ──────────────
        // Calling MapsInitializer and Places.initialize() here loads the
        // Maps GL renderer and Places native libraries during the splash
        // delay instead of mid-interaction, reducing cold-start page faults.
        initMapSdks();

        // ── Steps 2-4 start after the minimum splash display time ─────────
        handler.postDelayed(this::checkPermissionsAndProceed, SPLASH_DELAY_MS);
    }

    // ── Step 1 – Maps + Places SDK init ──────────────────────────────────

    /**
     * Pre-warm both the Google Maps rendering SDK and the Places SDK.
     *
     * MapsInitializer.initialize() – triggers the Maps GL renderer setup
     *   and begins loading the Maps native library. Without this, the first
     *   time SupportMapFragment is created it causes ~30 000 page faults
     *   that stall the main thread and produce an ANR in the calling activity.
     *
     * Places.initialize() – registers the Places API key so
     *   LocationPickerActivity's autocomplete works without a cold-start.
     */
    private void initMapSdks() {
        // Pre-warm Google Maps rendering SDK (the actual ANR culprit)
        try {
            MapsInitializer.initialize(
                    getApplicationContext(),
                    MapsInitializer.Renderer.LATEST,
                    renderer -> { /* non-blocking callback – no action needed */ });
        } catch (Exception e) {
            // Non-fatal
        }

        // Pre-warm Google Places SDK (autocomplete / place details)
        try {
            String apiKey = BuildConfig.GOOGLE_MAP_APIKEY;
            if (!apiKey.isEmpty() && !Places.isInitialized()) {
                Places.initialize(getApplicationContext(), apiKey);
            }
        } catch (Exception e) {
            // Non-fatal – LocationPickerActivity will retry if still needed
        }
    }

    // ── Step 2 – Runtime permission check ────────────────────────────────

    /**
     * Request every runtime permission the app needs before navigation.
     *
     * Permissions requested:
     *   • ACCESS_FINE_LOCATION   – required for the location picker map
     *   • ACCESS_COARSE_LOCATION – fallback for lower-accuracy location
     *
     * Navigation always proceeds after the user responds, even if denied.
     * Location is only required for the shop-location picker, not for
     * core app functionality.
     */
    private void checkPermissionsAndProceed() {
        boolean fineGranted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (fineGranted) {
            // All permissions already granted – skip the dialog
            checkConnectivityAndNavigate();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQ_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION_PERMISSION) {
            boolean granted = grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (!granted) {
                Toast.makeText(this,
                        "Location permission denied. " +
                        "You can still use the app, but the map location picker " +
                        "won't work until permission is granted.",
                        Toast.LENGTH_LONG).show();
            }
            // Always continue to the next step regardless of the user's choice
            checkConnectivityAndNavigate();
        }
    }

    // ── Step 3 – Connectivity check ───────────────────────────────────────

    /**
     * Check if the device has an active internet connection.
     * If yes → proceed to navigate().
     * If no  → show toast and register a NetworkCallback to proceed
     *           automatically when internet returns.
     */
    private void checkConnectivityAndNavigate() {
        if (isNetworkAvailable()) {
            navigate();
        } else {
            Toast.makeText(this,
                    "No internet connection. Please connect to continue.",
                    Toast.LENGTH_LONG).show();

            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    handler.post(() -> {
                        if (!navigated) {
                            Toast.makeText(SplashScreen.this,
                                    "Connected! Loading…", Toast.LENGTH_SHORT).show();
                            navigate();
                        }
                    });
                }
            };

            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();
            connectivityManager.registerNetworkCallback(request, networkCallback);
        }
    }

    // ── Step 4 – Auth check + navigation ─────────────────────────────────

    /**
     * Verify the cached Firebase user still exists on the server (handles
     * accounts deleted via the Firebase console), then route to the right screen.
     *
     * reload() makes a live network call – if the account was deleted,
     * FirebaseAuthInvalidUserException is thrown and we sign out + go to Welcome.
     */
    private void navigate() {
        if (navigated) return;

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            // No cached user → straight to Welcome
            navigated = true;
            goTo(Welcome.class);
            return;
        }

        // Server-side verification: catches deleted / disabled accounts
        firebaseUser.reload()
                .addOnSuccessListener(unused -> {
                    navigated = true;
                    goTo(Dashboard.class);
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthInvalidUserException) {
                        // Account deleted / disabled → clear local cache
                        FirebaseAuth.getInstance().signOut();
                    }
                    navigated = true;
                    goTo(Welcome.class);
                });
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /** Launch an activity and clear the entire back stack. */
    private void goTo(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /** Returns true when the device has a working, validated internet connection. */
    private boolean isNetworkAvailable() {
        if (connectivityManager == null) return false;
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) return false;
        NetworkCapabilities caps =
                connectivityManager.getNetworkCapabilities(activeNetwork);
        return caps != null
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkCallback != null && connectivityManager != null) {
            try { connectivityManager.unregisterNetworkCallback(networkCallback); }
            catch (IllegalArgumentException ignored) {}
        }
        handler.removeCallbacksAndMessages(null);
    }
}

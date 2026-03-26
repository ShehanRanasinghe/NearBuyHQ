package com.example.nearbuyhq.app.startup;

import android.content.Context;
import android.content.Intent;
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

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.dashboard.Dashboard;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Splash screen – shown on app launch.
 *
 * Behaviour:
 *  1. If no internet → show toast, wait for connection with NetworkCallback
 *  2. Once internet is available → check Firebase auth state
 *     - Already logged in → go to Dashboard (skip Welcome screen)
 *     - Not logged in     → go to Welcome
 */
public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS = 2000; // 2 s min display time

    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean navigated = false; // guard so we only navigate once
    private final Handler handler = new Handler(Looper.getMainLooper());

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

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Start decision after minimum splash delay
        handler.postDelayed(this::checkConnectivityAndNavigate, SPLASH_DELAY_MS);
    }

    /**
     * Check if the device has an active internet connection.
     * If yes → navigate immediately.
     * If no  → show toast and register a NetworkCallback to navigate automatically
     *           when internet returns.
     */
    private void checkConnectivityAndNavigate() {
        if (isNetworkAvailable()) {
            navigate();
        } else {
            // Show "No internet" toast and wait for connectivity to return
            Toast.makeText(this,
                    "No internet connection. Please connect to continue.",
                    Toast.LENGTH_LONG).show();

            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    // Internet came back – navigate on the main thread
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

    /**
     * Decide where to send the user:
     *  - Firebase user present → Dashboard (skip login/welcome)
     *  - No user              → Welcome screen
     */
    private void navigate() {
        if (navigated) return;
        navigated = true;

        boolean isLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
        Intent intent = isLoggedIn
                ? new Intent(this, Dashboard.class)
                : new Intent(this, Welcome.class);

        // Clear back stack so user cannot press Back to return here
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /** Returns true if the device currently has a working internet connection. */
    private boolean isNetworkAvailable() {
        if (connectivityManager == null) return false;
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) return false;
        NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(activeNetwork);
        return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister callback to prevent leaks
        if (networkCallback != null && connectivityManager != null) {
            try { connectivityManager.unregisterNetworkCallback(networkCallback); }
            catch (IllegalArgumentException ignored) {}
        }
        handler.removeCallbacksAndMessages(null);
    }
}

package com.example.nearbuyhq.app.startup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.dashboard.Dashboard;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Splash screen – shown for 2.5 seconds on launch.
 *
 * After the delay, checks if a user is already signed in with Firebase Auth:
 *  - Signed in  → go directly to Dashboard (skip login)
 *  - Not signed → go to Welcome (login / register flow)
 */
public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS = 2500; // 2.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply the splash theme before super/setContentView
        setTheme(R.style.Theme_NearBuyHQ_Splash);
        super.onCreate(savedInstanceState);

        // Make window truly full-screen (covers status & nav bars)
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        // Hide system UI for a clean immersive splash
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        setContentView(R.layout.activity_splash_screen);

        // Decide where to navigate after the splash delay
        new Handler().postDelayed(() -> {
            // Check if a Firebase user is already logged in
            boolean isLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;

            Intent intent;
            if (isLoggedIn) {
                // Skip login – go straight to Dashboard
                intent = new Intent(SplashScreen.this, Dashboard.class);
            } else {
                // Show the Welcome / login-register flow
                intent = new Intent(SplashScreen.this, Welcome.class);
            }

            startActivity(intent);
            finish(); // Remove splash from back stack
        }, SPLASH_DELAY_MS); // 2.5 second delay
    }
}

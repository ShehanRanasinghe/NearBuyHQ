package com.example.nearbuyhq.app.startup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nearbuyhq.R;

public class SplashScreen extends AppCompatActivity {

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

        // Navigate to Welcome screen after 3 seconds
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashScreen.this, Welcome.class);
            startActivity(intent);
            finish(); // Close splash screen so user can't go back to it
        }, 3000); // 3 second delay
    }
}

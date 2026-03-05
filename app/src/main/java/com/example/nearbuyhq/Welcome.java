package com.example.nearbuyhq;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Welcome extends AppCompatActivity {

    private static final String PREFS_NAME  = "NearBuyHQPrefs";
    private static final String KEY_ONBOARDED = "onboarding_complete";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        Button btnLogin      = findViewById(R.id.btnLogin);

//        // "Get Started" — mark onboarding done, go to Login
//        btnGetStarted.setOnClickListener(v -> {
//            markOnboarded();
//            goToLogin();
//        });
//
//        // "I already have an account" — mark onboarding done, go to Login
//        btnLogin.setOnClickListener(v -> {
//            markOnboarded();
//            goToLogin();
//        });
//    }

//    private void markOnboarded() {
//        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
//        prefs.edit().putBoolean(KEY_ONBOARDED, true).apply();
//    }

//    private void goToLogin() {
//        startActivity(new Intent(this, Login.class));
//        finish();
//    }

//    /** Call this from SplashScreen to decide which screen to open next. */
//    public static boolean isOnboarded(android.content.Context context) {
//        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
//        return prefs.getBoolean(KEY_ONBOARDED, false);
    }
}


package com.example.nearbuyhq.app.startup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.auth.Login;
import com.example.nearbuyhq.auth.Register;

// Welcome screen shown to first-time / unauthenticated users; entry point to Register or Login.
public class Welcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Hide the default ActionBar so the full-screen splash layout is unobstructed
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        Button btnLogin      = findViewById(R.id.btnLogin);

        // Navigate to the registration flow when the user taps "Get Started"
        btnGetStarted.setOnClickListener(v -> {
            startActivity(new Intent(Welcome.this, Register.class));
        });

        // Navigate to the login screen when the user taps "Log In"
        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(Welcome.this, Login.class));
        });
    }
}

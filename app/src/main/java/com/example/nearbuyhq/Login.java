package com.example.nearbuyhq;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup login button navigation
        Button loginBtn = findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(v -> {
            // Navigate to Dashboard
            Intent intent = new Intent(Login.this, Dashboard.class);
            startActivity(intent);
            finish(); // Close login screen so user can't go back to it with back button
        });

        // TODO: Setup forgot password link after R.java regenerates
        // TextView forgotPassword = findViewById(R.id.forgotPassword);
        // if (forgotPassword != null) {
        //     forgotPassword.setOnClickListener(v -> {
        //         Intent intent = new Intent(Login.this, ForgotPassword.class);
        //         startActivity(intent);
        //     });
        // }

        // Setup sign up link
        TextView signup = findViewById(R.id.signup);
        if (signup != null) {
            signup.setOnClickListener(v -> {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            });
        }
    }
}
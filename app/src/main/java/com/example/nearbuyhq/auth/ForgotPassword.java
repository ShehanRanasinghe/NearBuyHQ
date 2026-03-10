package com.example.nearbuyhq.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nearbuyhq.R;

public class ForgotPassword extends AppCompatActivity {

    private EditText emailInput;
    private Button btnSendResetLink;
    private TextView btnBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Hide ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        emailInput = findViewById(R.id.emailInput);
        btnSendResetLink = findViewById(R.id.btnSendResetLink);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        btnSendResetLink.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Reset link sent to " + email, Toast.LENGTH_SHORT).show();
                // Navigate to OTP verification
                Intent intent = new Intent(ForgotPassword.this, OTPVerification.class);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });

        btnBackToLogin.setOnClickListener(v -> {
            finish(); // Go back to login
        });
    }
}


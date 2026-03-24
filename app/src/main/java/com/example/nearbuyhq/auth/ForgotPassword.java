package com.example.nearbuyhq.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.data.repository.AuthRepository;
import com.example.nearbuyhq.data.repository.OperationCallback;

/**
 * Forgot Password screen.
 * Sends a Firebase password-reset email to the user's registered address.
 * The user clicks the link in that email to choose a new password.
 */
public class ForgotPassword extends AppCompatActivity {

    private EditText emailInput;
    private Button btnSendResetLink;
    private TextView btnBackToLogin;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Hide the default ActionBar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        emailInput = findViewById(R.id.emailInput);
        btnSendResetLink = findViewById(R.id.btnSendResetLink);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        authRepository = new AuthRepository();

        // Send password reset email via Firebase Auth
        btnSendResetLink.setOnClickListener(v -> sendResetEmail());

        // Go back to login screen
        btnBackToLogin.setOnClickListener(v -> finish());
    }

    private void sendResetEmail() {
        String email = emailInput.getText().toString().trim();

        if (email.isEmpty()) {
            emailInput.setError("Please enter your email address");
            return;
        }

        // Disable button while request is in flight
        btnSendResetLink.setEnabled(false);
        btnSendResetLink.setText("Sending...");

        // Ask Firebase to send the reset email
        authRepository.sendPasswordResetEmail(email, new OperationCallback() {
            @Override
            public void onSuccess() {
                btnSendResetLink.setEnabled(true);
                btnSendResetLink.setText("Send Reset Link");

                Toast.makeText(ForgotPassword.this,
                        "Reset link sent to " + email + "\nCheck your inbox.",
                        Toast.LENGTH_LONG).show();

                // Go back to login after success
                finish();
            }

            @Override
            public void onError(Exception exception) {
                btnSendResetLink.setEnabled(true);
                btnSendResetLink.setText("Send Reset Link");

                Toast.makeText(ForgotPassword.this,
                        "Failed: " + exception.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}

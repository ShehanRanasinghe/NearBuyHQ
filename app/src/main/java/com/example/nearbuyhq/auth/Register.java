package com.example.nearbuyhq.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.firebase.FirebaseConfig;
import com.example.nearbuyhq.data.repository.AuthRepository;
import com.example.nearbuyhq.data.repository.OperationCallback;

// Register screen – collects owner details, creates a Firebase Auth account and Firestore profile.
public class Register extends AppCompatActivity {

    private EditText fullName, email, username, password, confirmPassword;
    private Button registerBtn;
    private TextView loginLink;
    private ImageView togglePassword, toggleConfirmPassword;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Hide the default ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Bind all form fields and the repository
        fullName = findViewById(R.id.fullName);
        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        registerBtn = findViewById(R.id.registerBtn);
        loginLink = findViewById(R.id.loginLink);
        togglePassword = findViewById(R.id.togglePassword);
        toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword);
        authRepository = new AuthRepository();

        // Toggle password visibility
        togglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePassword.setImageResource(R.drawable.ic_eye_open);
            } else {
                password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePassword.setImageResource(R.drawable.ic_eye_closed);
            }
            password.setSelection(password.getText().length());
        });

        // Toggle confirm password visibility
        toggleConfirmPassword.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            if (isConfirmPasswordVisible) {
                confirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                toggleConfirmPassword.setImageResource(R.drawable.ic_eye_open);
            } else {
                confirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                toggleConfirmPassword.setImageResource(R.drawable.ic_eye_closed);
            }
            confirmPassword.setSelection(confirmPassword.getText().length());
        });

        // Validate inputs then trigger registration when the button is tapped
        registerBtn.setOnClickListener(v -> {
            if (validateInputs()) {
                registerUser();
            }
        });

        // Navigate back to Login if the user already has an account
        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
            finish();
        });
    }

    // ── Validation ────────────────────────────────────────────────────────

    // Validate all registration fields; sets inline errors and returns false on failure
    private boolean validateInputs() {
        String nameStr = fullName.getText().toString().trim();
        String emailStr = email.getText().toString().trim();
        String usernameStr = username.getText().toString().trim();
        String passwordStr = password.getText().toString().trim();
        String confirmPasswordStr = confirmPassword.getText().toString().trim();

        if (nameStr.isEmpty()) {
            fullName.setError("Full name is required");
            return false;
        }
        if (emailStr.isEmpty()) {
            email.setError("Email is required");
            return false;
        }
        if (usernameStr.isEmpty()) {
            username.setError("Username is required");
            return false;
        }
        if (passwordStr.isEmpty()) {
            password.setError("Password is required");
            return false;
        }
        if (confirmPasswordStr.isEmpty()) {
            confirmPassword.setError("Please confirm password");
            return false;
        }
        if (!passwordStr.equals(confirmPasswordStr)) {
            confirmPassword.setError("Passwords do not match");
            return false;
        }
        return true;
    }

    // ── Registration ─────────────────────────────────────────────────────

    private void registerUser() {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            Toast.makeText(this, "Firebase is disabled. Set FIREBASE_ENABLED=true", Toast.LENGTH_LONG).show();
            return;
        }

        String nameStr = fullName.getText().toString().trim();
        String emailStr = email.getText().toString().trim();
        String usernameStr = username.getText().toString().trim();
        String passwordStr = password.getText().toString().trim();

        // Show loading state while the network request is in flight
        setLoading(true);
        authRepository.register(nameStr, emailStr, usernameStr, passwordStr, new OperationCallback() {
            @Override
            public void onSuccess() {
                // Mark the account as unverified immediately after creation
                com.google.firebase.auth.FirebaseUser fbUser =
                        com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                if (fbUser != null) {
                    authRepository.setEmailUnverified(fbUser.getUid(), new OperationCallback() {
                        @Override public void onSuccess() {}
                        @Override public void onError(Exception e) {}
                    });
                }

                setLoading(false);
                Toast.makeText(Register.this, "Account created! Please verify your email.", Toast.LENGTH_SHORT).show();

                // Redirect to OTP verification to complete email confirmation
                Intent intent = new Intent(Register.this, OTPVerification.class);
                intent.putExtra("email",    emailStr);
                intent.putExtra("userName", nameStr);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(Exception exception) {
                setLoading(false);
                Toast.makeText(Register.this, "Registration failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Toggle the register button state while the network call is in progress
    private void setLoading(boolean loading) {
        registerBtn.setEnabled(!loading);
        registerBtn.setAlpha(loading ? 0.6f : 1f);
        registerBtn.setText(loading ? "Creating account..." : getString(R.string.btn_sign_up));
    }
}

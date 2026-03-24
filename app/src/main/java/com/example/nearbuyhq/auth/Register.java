package com.example.nearbuyhq.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.firebase.FirebaseConfig;
import com.example.nearbuyhq.data.repository.AuthRepository;
import com.example.nearbuyhq.data.repository.OperationCallback;

public class Register extends AppCompatActivity {

    private EditText fullName, email, username, password, confirmPassword;
    private Button registerBtn;
    private TextView loginLink;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Hide ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        fullName = findViewById(R.id.fullName);
        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        registerBtn = findViewById(R.id.registerBtn);
        loginLink = findViewById(R.id.loginLink);
        authRepository = new AuthRepository();

        registerBtn.setOnClickListener(v -> {
            if (validateInputs()) {
                registerUser();
            }
        });

        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
            finish();
        });
    }

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

    private void registerUser() {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            Toast.makeText(this, "Firebase is disabled. Set FIREBASE_ENABLED=true", Toast.LENGTH_LONG).show();
            return;
        }

        String nameStr = fullName.getText().toString().trim();
        String emailStr = email.getText().toString().trim();
        String usernameStr = username.getText().toString().trim();
        String passwordStr = password.getText().toString().trim();

        setLoading(true);
        authRepository.register(nameStr, emailStr, usernameStr, passwordStr, new OperationCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                Toast.makeText(Register.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Register.this, Login.class);
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

    private void setLoading(boolean loading) {
        registerBtn.setEnabled(!loading);
        registerBtn.setAlpha(loading ? 0.6f : 1f);
        registerBtn.setText(loading ? "Creating account..." : getString(R.string.btn_sign_up));
    }
}


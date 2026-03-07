package com.example.nearbuyhq;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Register extends AppCompatActivity {

    private EditText fullName, email, username, password, confirmPassword;
    private Button registerBtn;
    private TextView loginLink;

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

        registerBtn.setOnClickListener(v -> {
            if (validateInputs()) {
                Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
                finish();
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
}


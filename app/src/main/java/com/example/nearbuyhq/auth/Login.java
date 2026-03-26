package com.example.nearbuyhq.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.data.repository.AuthRepository;
import com.example.nearbuyhq.data.repository.OperationCallback;
import com.example.nearbuyhq.dashboard.Dashboard;

public class Login extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginBtn;
    private AuthRepository authRepository;

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

        usernameInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        authRepository = new AuthRepository();

        // Setup login button
        loginBtn.setOnClickListener(v -> {
            loginUser();
        });


        // Setup sign up link
        TextView signup = findViewById(R.id.signup);
        if (signup != null) {
            signup.setOnClickListener(v -> {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            });
        }

        if (authRepository.isLoggedIn()) {
            startActivity(new Intent(Login.this, Dashboard.class));
            finish();
        }
    }

    private void loginUser() {
        String usernameOrEmail = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (usernameOrEmail.isEmpty()) {
            usernameInput.setError("Email is required");
            return;
        }
        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            return;
        }


        setLoading(true);
        authRepository.login(usernameOrEmail, password, this, new OperationCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                Intent intent = new Intent(Login.this, Dashboard.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(Exception exception) {
                setLoading(false);
                Toast.makeText(Login.this, "Login failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        loginBtn.setEnabled(!loading);
        loginBtn.setAlpha(loading ? 0.6f : 1f);
        loginBtn.setText(loading ? "Signing in..." : getString(R.string.link_log_in));
    }
}
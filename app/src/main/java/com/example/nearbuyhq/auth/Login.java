package com.example.nearbuyhq.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.data.repository.AuthRepository;
import com.example.nearbuyhq.data.repository.DataCallback;
import com.example.nearbuyhq.data.repository.OperationCallback;
import com.example.nearbuyhq.dashboard.Dashboard;
import com.google.firebase.auth.FirebaseAuth;

// Login screen – authenticates with email or username via Firebase Auth, then routes to Dashboard.
public class Login extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginBtn;
    private ImageView togglePassword;
    private boolean isPasswordVisible = false;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Apply system-bar insets so content is not hidden behind the status/navigation bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bind views and initialise the repository
        usernameInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        togglePassword = findViewById(R.id.togglePassword);
        authRepository = new AuthRepository();

        // Toggle password visibility
        togglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                passwordInput.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePassword.setImageResource(R.drawable.ic_eye_open);
            } else {
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePassword.setImageResource(R.drawable.ic_eye_closed);
            }
            passwordInput.setSelection(passwordInput.getText().length());
        });

        // Wire the login button to the login flow
        loginBtn.setOnClickListener(v -> loginUser());

        // Navigate to Register when the sign-up link is tapped
        TextView signup = findViewById(R.id.signup);
        if (signup != null) {
            signup.setOnClickListener(v -> {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            });
        }

        // If a session already exists, check whether email verification was completed
        if (authRepository.isLoggedIn()) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            authRepository.isEmailVerifiedInFirestore(uid, new DataCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean verified) {
                    if (verified) {
                        goToDashboard();
                    } else {
                        // Registered but OTP not yet completed — redirect to OTP screen
                        String email = FirebaseAuth.getInstance().getCurrentUser() != null
                                ? FirebaseAuth.getInstance().getCurrentUser().getEmail() : "";
                        String name = FirebaseAuth.getInstance().getCurrentUser() != null
                                ? FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : "";
                        Intent intent = new Intent(Login.this, OTPVerification.class);
                        intent.putExtra("email", email != null ? email : "");
                        intent.putExtra("userName", name != null ? name : "");
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onError(Exception e) {
                    // Non-fatal – fall through and show the login screen
                }
            });
        }
    }

    // ── Login flow ────────────────────────────────────────────────────────

    private void loginUser() {
        String usernameOrEmail = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate required fields before making a network call
        if (usernameOrEmail.isEmpty()) {
            usernameInput.setError("Email is required");
            return;
        }
        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            return;
        }

        // Disable the button and show in-progress state while authenticating
        setLoading(true);
        authRepository.login(usernameOrEmail, password, this, new OperationCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                // After sign-in, confirm email-verified flag in Firestore before granting access
                String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                        ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
                if (uid.isEmpty()) {
                    goToDashboard();
                    return;
                }
                authRepository.isEmailVerifiedInFirestore(uid, new DataCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean verified) {
                        if (verified) {
                            goToDashboard();
                        } else {
                            // Account exists but OTP was never completed — send to OTP screen
                            String email = FirebaseAuth.getInstance().getCurrentUser() != null
                                    ? FirebaseAuth.getInstance().getCurrentUser().getEmail() : "";
                            String name = FirebaseAuth.getInstance().getCurrentUser() != null
                                    ? FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : "";
                            Toast.makeText(Login.this,
                                    "Please verify your email first.", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(Login.this, OTPVerification.class);
                            intent.putExtra("email", email != null ? email : "");
                            intent.putExtra("userName", name != null ? name : "");
                            startActivity(intent);
                            finish();
                        }
                    }
                    @Override
                    public void onError(Exception e) {
                        goToDashboard(); // non-fatal – allow login
                    }
                });
            }

            @Override
            public void onError(Exception exception) {
                setLoading(false);
                Toast.makeText(Login.this, "Login failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ── Navigation helpers ────────────────────────────────────────────────

    // Navigate to Dashboard and clear the back stack so the user cannot navigate back to Login
    private void goToDashboard() {
        Intent intent = new Intent(Login.this, Dashboard.class);
        startActivity(intent);
        finish();
    }

    // Toggle the login button appearance and enabled state while the request is in flight
    private void setLoading(boolean loading) {
        loginBtn.setEnabled(!loading);
        loginBtn.setAlpha(loading ? 0.6f : 1f);
        loginBtn.setText(loading ? "Signing in..." : getString(R.string.link_log_in));
    }
}
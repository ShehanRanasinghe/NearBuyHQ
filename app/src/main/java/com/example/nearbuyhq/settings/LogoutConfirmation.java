package com.example.nearbuyhq.settings;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.auth.Login;
import com.example.nearbuyhq.data.repository.AuthRepository;

/**
 * Logout confirmation dialog.
 * Signs the user out of Firebase Auth and clears the local session
 * before returning to the Login screen.
 */
public class LogoutConfirmation extends AppCompatActivity {

    private Button btnYes, btnCancel;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout_confirmation);

        // Hide the default ActionBar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        btnYes    = findViewById(R.id.btnYes);
        btnCancel = findViewById(R.id.btnCancel);
        authRepository = new AuthRepository();

        btnYes.setOnClickListener(v -> {
            // Sign out from Firebase and clear SessionManager data
            authRepository.logout(LogoutConfirmation.this);

            // Navigate to Login and clear the back stack so user cannot press Back
            Intent intent = new Intent(LogoutConfirmation.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnCancel.setOnClickListener(v -> finish()); // go back without logging out
    }
}

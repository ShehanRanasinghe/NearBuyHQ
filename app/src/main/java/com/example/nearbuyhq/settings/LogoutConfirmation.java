package com.example.nearbuyhq.settings;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.auth.Login;

public class LogoutConfirmation extends AppCompatActivity {

    private Button btnYes, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout_confirmation);

        // Hide ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        btnYes = findViewById(R.id.btnYes);
        btnCancel = findViewById(R.id.btnCancel);

        btnYes.setOnClickListener(v -> {
            // Clear session and navigate to login
            Intent intent = new Intent(LogoutConfirmation.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnCancel.setOnClickListener(v -> finish());
    }
}


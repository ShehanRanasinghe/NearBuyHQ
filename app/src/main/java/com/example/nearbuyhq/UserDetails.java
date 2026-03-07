package com.example.nearbuyhq;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UserDetails extends AppCompatActivity {

    private TextView userName, userEmail, userStatus;
    private Button btnSuspend, btnActivate, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        // Hide ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        userStatus = findViewById(R.id.userStatus);
        btnSuspend = findViewById(R.id.btnSuspend);
        btnActivate = findViewById(R.id.btnActivate);
        btnBack = findViewById(R.id.btnBack);

        // Get data from intent
        String name = getIntent().getStringExtra("user_name");
        String email = getIntent().getStringExtra("user_email");
        String status = getIntent().getStringExtra("user_status");

        // Set data
        userName.setText(name);
        userEmail.setText("Email: " + email);
        userStatus.setText("Status: " + status);

        btnSuspend.setOnClickListener(v -> {
            Toast.makeText(this, "User Suspended", Toast.LENGTH_SHORT).show();
            userStatus.setText("Status: Suspended");
        });

        btnActivate.setOnClickListener(v -> {
            Toast.makeText(this, "User Activated", Toast.LENGTH_SHORT).show();
            userStatus.setText("Status: Active");
        });

        btnBack.setOnClickListener(v -> finish());
    }
}


package com.example.nearbuyhq.users;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.firebase.FirebaseConfig;
import com.example.nearbuyhq.data.repository.DataCallback;
import com.example.nearbuyhq.data.repository.OperationCallback;
import com.example.nearbuyhq.data.repository.UserRepository;

public class UserDetails extends AppCompatActivity {

    private TextView userName, userEmail, userStatus;
    private Button btnSuspend, btnActivate, btnBack;
    private UserRepository userRepository;
    private String userId;

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
        userRepository = new UserRepository();

        // Get data from intent
        userId = getIntent().getStringExtra("user_id");
        String name = getIntent().getStringExtra("user_name");
        String email = getIntent().getStringExtra("user_email");
        String status = getIntent().getStringExtra("user_status");

        // Set data
        userName.setText(name);
        userEmail.setText("Email: " + email);
        userStatus.setText("Status: " + status);

        if (FirebaseConfig.isFirebaseEnabled() && userId != null && !userId.trim().isEmpty()) {
            userRepository.getUser(userId, new DataCallback<User>() {
                @Override
                public void onSuccess(User data) {
                    if (data == null) {
                        return;
                    }
                    userName.setText(data.getName());
                    userEmail.setText("Email: " + data.getEmail());
                    userStatus.setText("Status: " + data.getStatus());
                }

                @Override
                public void onError(Exception exception) {
                    Toast.makeText(UserDetails.this, "Using local user data", Toast.LENGTH_SHORT).show();
                }
            });
        }

        btnSuspend.setOnClickListener(v -> {
            updateStatus("Suspended", "User Suspended");
        });

        btnActivate.setOnClickListener(v -> {
            updateStatus("Active", "User Activated");
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void updateStatus(String status, String successMessage) {
        if (userId == null || userId.trim().isEmpty()) {
            userStatus.setText("Status: " + status);
            Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!FirebaseConfig.isFirebaseEnabled()) {
            userStatus.setText("Status: " + status);
            Toast.makeText(this, successMessage + " (local)", Toast.LENGTH_SHORT).show();
            return;
        }

        userRepository.updateStatus(userId, status, new OperationCallback() {
            @Override
            public void onSuccess() {
                userStatus.setText("Status: " + status);
                Toast.makeText(UserDetails.this, successMessage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception exception) {
                Toast.makeText(UserDetails.this, "Failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}


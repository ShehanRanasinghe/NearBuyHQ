package com.example.nearbuyhq.app.startup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.auth.Login;
import com.example.nearbuyhq.auth.Register;

public class Welcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Hide ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        Button btnLogin      = findViewById(R.id.btnLogin);

        btnGetStarted.setOnClickListener(v -> {
            startActivity(new Intent(Welcome.this, Register.class));
        });

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(Welcome.this, Login.class));
        });
    }
}


package com.example.nearbuyhq;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OTPVerification extends AppCompatActivity {

    private EditText otp1, otp2, otp3, otp4, otp5, otp6;
    private Button btnVerify;
    private TextView btnResendOTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Hide ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);
        btnVerify = findViewById(R.id.btnVerify);
        btnResendOTP = findViewById(R.id.btnResendOTP);

        String email = getIntent().getStringExtra("email");

        btnVerify.setOnClickListener(v -> {
            String otp = otp1.getText().toString() + otp2.getText().toString() +
                    otp3.getText().toString() + otp4.getText().toString() +
                    otp5.getText().toString() + otp6.getText().toString();

            if (otp.length() == 6) {
                Toast.makeText(this, "OTP Verified Successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(OTPVerification.this, Login.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Please enter complete OTP", Toast.LENGTH_SHORT).show();
            }
        });

        btnResendOTP.setOnClickListener(v -> {
            Toast.makeText(this, "OTP resent to " + email, Toast.LENGTH_SHORT).show();
        });
    }
}


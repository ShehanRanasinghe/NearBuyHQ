package com.example.nearbuyhq.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nearbuyhq.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * OTP / Email Verification screen – shown once after first-time registration.
 *
 * Firebase doesn't send a numeric OTP for email, but it sends an email-verification link.
 * This screen tells the user to check their inbox and polls Firebase until
 * the email is marked verified, then redirects to Login.
 *
 * The 6 digit boxes are kept for UI consistency; pressing "Verify OTP" triggers
 * a manual reload-and-check of the verification state.
 */
public class OTPVerification extends AppCompatActivity {

    private EditText otp1, otp2, otp3, otp4, otp5, otp6;
    private Button btnVerify;
    private TextView btnResendOTP;
    private String email;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final Handler handler = new Handler(Looper.getMainLooper());
    // Poll every 4 seconds while screen is visible
    private static final int POLL_INTERVAL_MS = 4000;
    private boolean verified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        email = getIntent().getStringExtra("email");

        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);
        btnVerify    = findViewById(R.id.btnVerify);
        btnResendOTP = findViewById(R.id.btnResendOTP);

        // Auto-advance focus between OTP boxes
        setupOtpAutoAdvance();

        // Send the verification email as soon as this screen opens
        sendVerificationEmail();

        // "Verify OTP" button → reload Firebase user and check isEmailVerified
        btnVerify.setOnClickListener(v -> checkEmailVerified());

        // Resend button
        btnResendOTP.setOnClickListener(v -> sendVerificationEmail());

        // Start background polling so the user can just click the link and come back
        startPolling();
    }

    // ── Firebase email verification ───────────────────────────────────────

    /** Send Firebase email-verification link to the user's inbox. */
    private void sendVerificationEmail() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        user.sendEmailVerification()
                .addOnSuccessListener(unused ->
                        Toast.makeText(this,
                                "Verification email sent to " + email +
                                        ". Please check your inbox.",
                                Toast.LENGTH_LONG).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Could not send email: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    /**
     * Reload the Firebase user object and check whether the email is verified.
     * Called both on button press and by the background poll.
     */
    private void checkEmailVerified() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            goToLogin();
            return;
        }
        btnVerify.setEnabled(false);
        btnVerify.setText("Checking…");

        user.reload().addOnCompleteListener(task -> {
            FirebaseUser refreshed = auth.getCurrentUser();
            if (refreshed != null && refreshed.isEmailVerified()) {
                onVerified();
            } else {
                btnVerify.setEnabled(true);
                btnVerify.setText("Verify OTP");
                Toast.makeText(this,
                        "Email not verified yet. Please click the link in your email.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void onVerified() {
        verified = true;
        Toast.makeText(this, "Email verified! You can now log in.", Toast.LENGTH_SHORT).show();
        goToLogin();
    }

    private void goToLogin() {
        auth.signOut(); // sign out so the user goes through the normal login flow
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ── Background poll ───────────────────────────────────────────────────

    private final Runnable pollTask = new Runnable() {
        @Override
        public void run() {
            if (verified) return;
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                user.reload().addOnCompleteListener(task -> {
                    FirebaseUser refreshed = auth.getCurrentUser();
                    if (refreshed != null && refreshed.isEmailVerified()) {
                        onVerified();
                    } else {
                        // Not yet – schedule next poll
                        handler.postDelayed(this, POLL_INTERVAL_MS);
                    }
                });
            }
        }
    };

    private void startPolling() {
        handler.postDelayed(pollTask, POLL_INTERVAL_MS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    // ── Auto-advance OTP input ────────────────────────────────────────────

    private void setupOtpAutoAdvance() {
        EditText[] fields = {otp1, otp2, otp3, otp4, otp5, otp6};
        for (int i = 0; i < fields.length; i++) {
            final int index = i;
            fields[i].addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                public void onTextChanged(CharSequence s, int a, int b, int c) {}
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && index < fields.length - 1) {
                        fields[index + 1].requestFocus();
                    }
                }
            });
        }
    }
}

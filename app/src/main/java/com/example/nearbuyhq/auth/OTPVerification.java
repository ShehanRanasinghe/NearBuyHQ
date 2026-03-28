package com.example.nearbuyhq.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.data.repository.AuthRepository;
import com.example.nearbuyhq.data.repository.OperationCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * OTP Verification screen – shown once after first-time registration.
 * <p>
 * A 6-digit OTP is generated, stored in Firestore and emailed to the user
 * via Gmail SMTP (EmailOtpService). The user enters the code here to verify
 * their email address.  No Firebase email-link is used.
 */
public class OTPVerification extends AppCompatActivity {

    private EditText otp1, otp2, otp3, otp4, otp5, otp6;
    private Button btnVerify;
    private TextView btnResendOTP, tvCountdown;
    private String email;
    private String userName;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final AuthRepository authRepository = new AuthRepository();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private CountDownTimer resendTimer;
    private static final long RESEND_COOLDOWN_MS = 60_000L; // 60 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        email    = getIntent().getStringExtra("email");
        userName = getIntent().getStringExtra("userName");
        if (userName == null || userName.isEmpty()) userName = "User";

        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);
        btnVerify    = findViewById(R.id.btnVerify);
        btnResendOTP  = findViewById(R.id.btnResendOTP);
        tvCountdown   = findViewById(R.id.tvCountdown);
        TextView tvEmailSentTo = findViewById(R.id.tvEmailSentTo);

        if (tvEmailSentTo != null) {
            tvEmailSentTo.setText(getString(R.string.otp_code_sent_to, email));
        }

        setupOtpAutoAdvance();

        // Send OTP as soon as the screen opens
        sendOtp();

        btnVerify.setOnClickListener(v -> verifyOtp());

        btnResendOTP.setOnClickListener(v -> {
            clearOtpFields();
            sendOtp();
        });
    }

    // ── Send OTP ──────────────────────────────────────────────────────────────

    private void sendOtp() {
        setVerifyEnabled(false);
        btnResendOTP.setEnabled(false);
        if (tvCountdown != null) tvCountdown.setText(R.string.otp_sending);

        EmailOtpService.sendOtp(email, userName, new EmailOtpService.OtpCallback() {
            @Override
            public void onSuccess(String otp) {
                mainHandler.post(() -> {
                    Toast.makeText(OTPVerification.this,
                            "Verification code sent to " + email,
                            Toast.LENGTH_LONG).show();
                    setVerifyEnabled(true);
                    startResendCountdown();
                });
            }

            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    Toast.makeText(OTPVerification.this,
                            "Could not send code. Check SMTP settings.\n" + errorMessage,
                            Toast.LENGTH_LONG).show();
                    setVerifyEnabled(true);
                    btnResendOTP.setEnabled(true);
                    if (tvCountdown != null) tvCountdown.setVisibility(View.GONE);
                });
            }
        });
    }

    // ── Verify OTP ────────────────────────────────────────────────────────────

    private void verifyOtp() {
        String entered = getEnteredOtp();
        if (entered.length() < 6) {
            Toast.makeText(this, "Please enter the complete 6-digit code.", Toast.LENGTH_SHORT).show();
            return;
        }

        setVerifyEnabled(false);
        btnVerify.setText(R.string.otp_verifying);

        EmailOtpService.verifyOtp(email, entered, new EmailOtpService.VerifyCallback() {
            @Override
            public void onSuccess() {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    authRepository.markEmailVerified(user.getUid(), new OperationCallback() {
                        @Override public void onSuccess() { mainHandler.post(OTPVerification.this::onVerified); }
                        @Override public void onError(Exception e) { mainHandler.post(OTPVerification.this::onVerified); }
                    });
                } else {
                    mainHandler.post(OTPVerification.this::onVerified);
                }
            }

            @Override
            public void onExpired() {
                mainHandler.post(() -> {
                    setVerifyEnabled(true);
                    btnVerify.setText(R.string.otp_verify_code);
                    Toast.makeText(OTPVerification.this,
                            "Code expired. Please request a new one.",
                            Toast.LENGTH_LONG).show();
                    clearOtpFields();
                });
            }

            @Override
            public void onInvalid() {
                mainHandler.post(() -> {
                    setVerifyEnabled(true);
                    btnVerify.setText(R.string.otp_verify_code);
                    Toast.makeText(OTPVerification.this,
                            "Incorrect code. Please try again.",
                            Toast.LENGTH_SHORT).show();
                    clearOtpFields();
                });
            }

            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    setVerifyEnabled(true);
                    btnVerify.setText(R.string.otp_verify_code);
                    Toast.makeText(OTPVerification.this,
                            "Verification failed: " + errorMessage,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void onVerified() {
        Toast.makeText(this, "Email verified! Please log in.", Toast.LENGTH_SHORT).show();
        auth.signOut();
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ── Resend countdown ──────────────────────────────────────────────────────

    private void startResendCountdown() {
        if (resendTimer != null) resendTimer.cancel();
        btnResendOTP.setEnabled(false);
        if (tvCountdown != null) tvCountdown.setVisibility(View.VISIBLE);

        resendTimer = new CountDownTimer(RESEND_COOLDOWN_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (tvCountdown != null)
                    tvCountdown.setText(getString(R.string.otp_resend_in, millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                btnResendOTP.setEnabled(true);
                if (tvCountdown != null) {
                    tvCountdown.setText(R.string.otp_didnt_receive);
                }
            }
        }.start();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String getEnteredOtp() {
        return otp1.getText().toString().trim() +
               otp2.getText().toString().trim() +
               otp3.getText().toString().trim() +
               otp4.getText().toString().trim() +
               otp5.getText().toString().trim() +
               otp6.getText().toString().trim();
    }

    private void clearOtpFields() {
        otp1.setText(""); otp2.setText(""); otp3.setText("");
        otp4.setText(""); otp5.setText(""); otp6.setText("");
        otp1.requestFocus();
    }

    private void setVerifyEnabled(boolean enabled) {
        btnVerify.setEnabled(enabled);
        btnVerify.setAlpha(enabled ? 1f : 0.6f);
        if (enabled) btnVerify.setText(R.string.otp_verify_code);
    }

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
                    } else if (s.length() == 0 && index > 0) {
                        fields[index - 1].requestFocus();
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resendTimer != null) resendTimer.cancel();
    }
}

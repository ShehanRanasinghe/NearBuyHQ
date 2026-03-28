package com.example.nearbuyhq.auth;

import com.example.nearbuyhq.BuildConfig;
import com.example.nearbuyhq.data.remote.firebase.FirebaseCollections;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Generates a 6-digit OTP, stores it in Firestore with a 10-minute expiry,
 * and sends it to the user's email address via Gmail SMTP.
 * Credentials come from BuildConfig (set in .env):
 * SMTP_EMAIL    – the sender Gmail address
 * SMTP_PASSWORD – a Gmail App Password (not the account password)
 */
public class EmailOtpService {

    public interface OtpCallback {
        void onSuccess(String otp);
        void onError(String errorMessage);
    }

    public interface VerifyCallback {
        void onSuccess();
        void onExpired();
        void onInvalid();
        void onError(String errorMessage);
    }

    private static final long OTP_EXPIRY_MS = 10 * 60 * 1000L; // 10 minutes
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    // ── Generate, store and send OTP ─────────────────────────────────────────

    /**
     * Generates a 6-digit OTP, saves it to Firestore and emails it.
     * The callback runs on a background thread – post to main thread as needed.
     */
    public static void sendOtp(String recipientEmail, String recipientName, OtpCallback callback) {
        String otp = generateOtp();
        long expiresAt = System.currentTimeMillis() + OTP_EXPIRY_MS;

        // Save OTP to Firestore
        Map<String, Object> data = new HashMap<>();
        data.put("otp", otp);
        data.put("email", recipientEmail);
        data.put("expiresAt", expiresAt);
        data.put("createdAt", System.currentTimeMillis());

        String docId = recipientEmail.replace(".", "_").replace("@", "__");
        FirebaseFirestore.getInstance()
                .collection(FirebaseCollections.OTP_CODES)
                .document(docId)
                .set(data)
                .addOnSuccessListener(unused -> {
                    // Send the email on a background thread
                    executor.execute(() -> {
                        try {
                            sendEmail(recipientEmail, recipientName, otp);
                            callback.onSuccess(otp);
                        } catch (Exception e) {
                            callback.onError(e.getMessage());
                        }
                    });
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ── Verify OTP ────────────────────────────────────────────────────────────

    /**
     * Checks the entered OTP against the stored value in Firestore.
     * Deletes the record on successful verification.
     */
    public static void verifyOtp(String email, String enteredOtp, VerifyCallback callback) {
        String docId = email.replace(".", "_").replace("@", "__");
        FirebaseFirestore.getInstance()
                .collection(FirebaseCollections.OTP_CODES)
                .document(docId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        callback.onInvalid();
                        return;
                    }
                    String storedOtp = doc.getString("otp");
                    Long expiresAt   = doc.getLong("expiresAt");

                    if (expiresAt != null && System.currentTimeMillis() > expiresAt) {
                        // Clean up expired document
                        doc.getReference().delete();
                        callback.onExpired();
                        return;
                    }
                    if (enteredOtp != null && enteredOtp.equals(storedOtp)) {
                        // Delete used OTP
                        doc.getReference().delete();
                        callback.onSuccess();
                    } else {
                        callback.onInvalid();
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static String generateOtp() {
        return String.format(Locale.US, "%06d", new Random().nextInt(1_000_000));
    }

    private static void sendEmail(String toEmail, String toName, String otp) throws Exception {
        String fromEmail = BuildConfig.SMTP_EMAIL;
        String password  = BuildConfig.SMTP_PASSWORD;

        Properties props = new Properties();
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust",       "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail, "NearBuyHQ"));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail, toName));
        message.setSubject("NearBuyHQ – Your Verification Code: " + otp);

        // HTML body
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(buildEmailHtml(toName, otp), "text/html; charset=utf-8");

        MimeMultipart multipart = new MimeMultipart("alternative");
        multipart.addBodyPart(htmlPart);
        message.setContent(multipart);

        Transport.send(message);
    }

    // ── Professional HTML email template ─────────────────────────────────────

    private static String buildEmailHtml(String name, String otp) {
        String displayName = (name != null && !name.isEmpty()) ? name : "User";
        // Split OTP digits for spaced display
        String d1 = String.valueOf(otp.charAt(0));
        String d2 = String.valueOf(otp.charAt(1));
        String d3 = String.valueOf(otp.charAt(2));
        String d4 = String.valueOf(otp.charAt(3));
        String d5 = String.valueOf(otp.charAt(4));
        String d6 = String.valueOf(otp.charAt(5));

        return "<!DOCTYPE html>" +
            "<html lang='en'><head>" +
            "<meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "<title>NearBuyHQ – Email Verification</title>" +
            "</head>" +
            "<body style='margin:0;padding:0;background-color:#f0f4f8;font-family:Arial,Helvetica,sans-serif;'>" +

            // Outer wrapper
            "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f0f4f8;padding:40px 20px;'>" +
            "<tr><td align='center'>" +
            "<table width='600' cellpadding='0' cellspacing='0' style='max-width:600px;width:100%;'>" +

            // ── Header ─────────────────────────────────────────────────────
            "<tr><td style='background:linear-gradient(135deg,#2E4F6F 0%,#1E3A55 100%);" +
            "border-radius:12px 12px 0 0;padding:36px 40px;text-align:center;'>" +
            "<div style='display:inline-block;background:#FF5A4F;border-radius:50%;width:60px;" +
            "height:60px;line-height:60px;text-align:center;font-size:28px;margin-bottom:16px;'>🛒</div>" +
            "<h1 style='color:#ffffff;margin:0;font-size:26px;font-weight:700;letter-spacing:1px;'>NearBuyHQ</h1>" +
            "<p style='color:#b3d4f0;margin:6px 0 0;font-size:14px;'>Shop Owner Portal</p>" +
            "</td></tr>" +

            // ── Body ───────────────────────────────────────────────────────
            "<tr><td style='background:#ffffff;padding:40px;'>" +
            "<p style='color:#1a1a2e;font-size:16px;margin:0 0 8px;'>Hello, <strong>" + displayName + "</strong> 👋</p>" +
            "<p style='color:#6b7280;font-size:15px;margin:0 0 28px;line-height:1.6;'>" +
            "Thank you for registering with <strong>NearBuyHQ</strong>. Use the 6-digit verification code below to confirm your email address." +
            "</p>" +

            // OTP box
            "<div style='background:linear-gradient(135deg,#2E4F6F 0%,#1E3A55 100%);" +
            "border-radius:12px;padding:32px 20px;text-align:center;margin-bottom:28px;'>" +
            "<p style='color:#b3d4f0;font-size:13px;text-transform:uppercase;letter-spacing:2px;margin:0 0 16px;'>Your Verification Code</p>" +

            // Individual digit boxes
            "<div style='display:inline-block;'>" +
            otpDigitBox(d1) + otpDigitBox(d2) + otpDigitBox(d3) +
            "<span style='color:#b3d4f0;font-size:28px;font-weight:bold;margin:0 4px;'>–</span>" +
            otpDigitBox(d4) + otpDigitBox(d5) + otpDigitBox(d6) +
            "</div>" +

            "<p style='color:#b3d4f0;font-size:13px;margin:20px 0 0;'>" +
            "⏱ This code expires in <strong style='color:#ffffff;'>10 minutes</strong>." +
            "</p>" +
            "</div>" +

            // Security note
            "<div style='background:#fff8f0;border-left:4px solid #FF5A4F;border-radius:4px;padding:16px;margin-bottom:28px;'>" +
            "<p style='color:#6b7280;font-size:13px;margin:0;line-height:1.6;'>" +
            "🔒 <strong style='color:#1a1a2e;'>Security tip:</strong> " +
            "Never share this code with anyone. NearBuyHQ staff will never ask for your OTP." +
            "</p>" +
            "</div>" +

            "<p style='color:#6b7280;font-size:14px;margin:0;line-height:1.6;'>" +
            "If you did not create an account with NearBuyHQ, please ignore this email." +
            "</p>" +
            "</td></tr>" +

            // ── Footer ─────────────────────────────────────────────────────
            "<tr><td style='background:#2E4F6F;border-radius:0 0 12px 12px;padding:24px 40px;text-align:center;'>" +
            "<p style='color:#b3d4f0;font-size:12px;margin:0;line-height:1.8;'>" +
            "© 2025 NearBuyHQ · Shop Owner Portal<br>" +
            "This is an automated message – please do not reply." +
            "</p>" +
            "</td></tr>" +

            "</table>" +
            "</td></tr></table>" +
            "</body></html>";
    }

    private static String otpDigitBox(String digit) {
        return "<span style='display:inline-block;background:rgba(255,255,255,0.15);" +
                "color:#ffffff;font-size:28px;font-weight:700;width:44px;height:52px;" +
                "line-height:52px;border-radius:8px;margin:0 3px;" +
                "border:1px solid rgba(255,255,255,0.25);text-align:center;'>" +
                digit + "</span>";
    }
}


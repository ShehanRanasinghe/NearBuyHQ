package com.example.nearbuyhq.notifications;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.SessionManager;
import com.example.nearbuyhq.core.firebase.FirebaseConfig;
import com.example.nearbuyhq.data.repository.DataCallback;
import com.example.nearbuyhq.data.repository.NotificationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Notifications screen – shows in-app alerts for the shop owner.
 *
 * Examples of notifications stored in Firestore:
 *  - "Low Stock Alert"  → a product has fallen below the threshold
 *  - "New Order"        → a customer placed an order (written by the customer app)
 *  - "Deal Expiring"    → a promotion is about to expire
 *
 * Notifications are stored in the 'notifications' Firestore collection
 * and filtered by shopId so each owner only sees their own alerts.
 */
public class Notifications extends AppCompatActivity {

    private RecyclerView recyclerViewNotifications;
    private NotificationsAdapter notificationsAdapter;
    private List<Notification> notificationsList;
    private ImageView btnBack;

    private NotificationRepository notificationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        notificationRepository = new NotificationRepository();

        recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);
        btnBack = findViewById(R.id.btnBack);

        // Start with empty list – will be filled by Firebase or sample data
        notificationsList = new ArrayList<>();
        notificationsAdapter = new NotificationsAdapter(notificationsList);
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNotifications.setAdapter(notificationsAdapter);

        btnBack.setOnClickListener(v -> finish());

        // Load real notifications from Firestore
        loadNotifications();
    }

    // ── Load from Firestore ──────────────────────────────────────────────

    /**
     * Fetch all notifications that belong to the current shop from Firestore.
     * Results are ordered newest first.
     * Falls back to sample data if Firebase is disabled or the call fails.
     */
    private void loadNotifications() {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            // Firebase is off (dev mode) – show sample data so the screen isn't empty
            showSampleNotifications();
            return;
        }

        // Get the shopId saved in session after login
        String shopId = SessionManager.getInstance(this).getShopId();

        if (shopId == null || shopId.isEmpty()) {
            // Shop hasn't been registered yet – show sample data
            showSampleNotifications();
            return;
        }

        notificationRepository.getNotificationsByShop(shopId,
                new DataCallback<List<Map<String, Object>>>() {
                    @Override
                    public void onSuccess(List<Map<String, Object>> data) {
                        List<Notification> loaded = new ArrayList<>();
                        for (Map<String, Object> map : data) {
                            // Convert Firestore map to Notification model
                            String title   = stringFrom(map, "title");
                            String message = stringFrom(map, "message");
                            long createdAt = longFrom(map, "createdAt");
                            String time    = formatTimeAgo(createdAt);

                            loaded.add(new Notification(title, message, time));
                        }

                        if (loaded.isEmpty()) {
                            // No notifications yet – show placeholders
                            showSampleNotifications();
                        } else {
                            notificationsAdapter.updateList(loaded);
                        }
                    }

                    @Override
                    public void onError(Exception exception) {
                        // Network error – fall back to sample data silently
                        Toast.makeText(Notifications.this,
                                "Could not load notifications", Toast.LENGTH_SHORT).show();
                        showSampleNotifications();
                    }
                });
    }

    // ── Sample / Fallback Data ───────────────────────────────────────────

    /** Fill the list with placeholder notifications when Firestore is unavailable. */
    private void showSampleNotifications() {
        List<Notification> samples = new ArrayList<>();
        samples.add(new Notification("New Shop Registration",   "Fresh Mart has registered on the platform",  "2 hours ago"));
        samples.add(new Notification("Deal Expiring Soon",      "50% Off on Groceries expires today",         "5 hours ago"));
        samples.add(new Notification("Shop Approval Needed",    "Tech Hub is waiting for approval",           "1 day ago"));
        samples.add(new Notification("New Report",              "User reported Fashion Plaza",                "2 days ago"));
        samples.add(new Notification("System Alert",            "Database backup completed successfully",     "3 days ago"));
        notificationsAdapter.updateList(samples);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    /**
     * Convert a Firestore epoch-millisecond timestamp to a human-readable
     * relative time string like "3 hours ago" or "2 days ago".
     */
    private String formatTimeAgo(long epochMs) {
        if (epochMs <= 0) return "just now";
        long diff    = System.currentTimeMillis() - epochMs;
        long minutes = diff / 60_000;
        long hours   = diff / 3_600_000;
        long days    = diff / 86_400_000;

        if (minutes < 1)  return "just now";
        if (minutes < 60) return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        if (hours   < 24) return hours   + " hour"   + (hours   == 1 ? "" : "s") + " ago";
        return days + " day" + (days == 1 ? "" : "s") + " ago";
    }

    private String stringFrom(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val == null ? "" : String.valueOf(val).trim();
    }

    private long longFrom(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).longValue();
        return 0L;
    }
}

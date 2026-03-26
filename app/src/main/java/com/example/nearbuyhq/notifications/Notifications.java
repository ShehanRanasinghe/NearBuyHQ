package com.example.nearbuyhq.notifications;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.SessionManager;
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
    private List<Notification> allNotifications = new ArrayList<>();
    private ImageView btnBack;
    private String searchQuery = "";

    private NotificationRepository notificationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        notificationRepository = new NotificationRepository();

        recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);
        btnBack = findViewById(R.id.btnBack);

        notificationsAdapter = new NotificationsAdapter(new ArrayList<>());
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNotifications.setAdapter(notificationsAdapter);

        btnBack.setOnClickListener(v -> finish());

        // Wire search
        EditText etSearch = findViewById(R.id.etSearchNotifications);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                    searchQuery = s.toString().trim().toLowerCase();
                    applySearch();
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

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
        // Get the shopId saved in session after login
        String shopId = SessionManager.getInstance(this).getShopId();

        if (shopId == null || shopId.isEmpty()) {
            // No shop registered yet – show empty list
            notificationsAdapter.updateList(new ArrayList<>());
            return;
        }

        notificationRepository.getNotificationsByShop(shopId,
                new DataCallback<List<Map<String, Object>>>() {
                    @Override
                    public void onSuccess(List<Map<String, Object>> data) {
                        allNotifications.clear();
                        for (Map<String, Object> map : data) {
                            // Convert Firestore map to Notification model
                            String title   = stringFrom(map, "title");
                            String message = stringFrom(map, "message");
                            long createdAt = longFrom(map, "createdAt");
                            String time    = formatTimeAgo(createdAt);

                            allNotifications.add(new Notification(title, message, time));
                        }
                        applySearch();
                        // Show empty-state if no notifications exist
                        if (allNotifications.isEmpty()) {
                            Toast.makeText(Notifications.this,
                                    "No notifications yet", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Exception exception) {
                        Toast.makeText(Notifications.this,
                                "Could not load notifications", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void applySearch() {
        if (searchQuery.isEmpty()) {
            notificationsAdapter.updateList(new ArrayList<>(allNotifications));
            return;
        }
        List<Notification> filtered = new ArrayList<>();
        for (Notification n : allNotifications) {
            if ((n.getTitle()   != null && n.getTitle().toLowerCase().contains(searchQuery))
             || (n.getMessage() != null && n.getMessage().toLowerCase().contains(searchQuery))) {
                filtered.add(n);
            }
        }
        notificationsAdapter.updateList(filtered);
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

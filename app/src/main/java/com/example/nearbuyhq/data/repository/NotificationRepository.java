package com.example.nearbuyhq.data.repository;

import com.example.nearbuyhq.core.firebase.FirebaseConfig;
import com.example.nearbuyhq.data.remote.firebase.FirebaseCollections;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * All notification data lives under NearBuyHQ/{shopId}/notifications.
 */
public class NotificationRepository {

    private final FirebaseFirestore db;

    public NotificationRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    private CollectionReference notifRef(String shopId) {
        return db.collection(FirebaseCollections.USERS)
                 .document(shopId)
                 .collection(FirebaseCollections.USER_NOTIFICATIONS);
    }

    // ── Create ────────────────────────────────────────────────────────────

    /**
     * Save a new notification document to Firestore.
     *
     * @param title    Short heading (e.g. "Low Stock Alert")
     * @param message  Details (e.g. "Tomatoes has only 3 units left")
     * @param type     Category string (e.g. "stock", "order", "system")
     * @param shopId   The shop this notification belongs to
     */
    public void createNotification(String title, String message, String type,
                                   String shopId, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        long now = System.currentTimeMillis();
        Map<String, Object> data = new HashMap<>();
        data.put("title",     title);
        data.put("message",   message);
        data.put("type",      type);
        data.put("shopId",    shopId);
        data.put("isRead",    false);       // unread by default
        data.put("createdAt", now);
        data.put("updatedAt", now);

        // Let Firestore auto-generate the document ID
        notifRef(shopId).add(data)
                .addOnSuccessListener(ref -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ── Read ──────────────────────────────────────────────────────────────

    /**
     * Load all notifications for a specific shop, newest first.
     */
    public void getNotificationsByShop(String shopId, DataCallback<List<Map<String, Object>>> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        notifRef(shopId).orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snaps -> {
                    List<Map<String, Object>> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snaps.getDocuments()) {
                        Map<String, Object> item = doc.getData();
                        if (item != null) {
                            item.put("id", doc.getId()); // inject doc id
                            list.add(item);
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onError);
    }

    // ── Mark as read ──────────────────────────────────────────────────────

    /** Mark a notification as read in Firestore. */
    public void markAsRead(String notifId, String shopId, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        notifRef(shopId).document(notifId)
                .update("isRead", true, "updatedAt", System.currentTimeMillis())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ── Delete ────────────────────────────────────────────────────────────

    /** Delete a notification document. */
    public void deleteNotification(String notifId, String shopId, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        notifRef(shopId).document(notifId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }
}

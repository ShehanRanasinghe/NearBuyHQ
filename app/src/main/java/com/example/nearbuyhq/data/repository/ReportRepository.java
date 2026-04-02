package com.example.nearbuyhq.data.repository;

import com.example.nearbuyhq.core.firebase.FirebaseConfig;
import com.example.nearbuyhq.data.remote.firebase.FirebaseCollections;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * All report data lives under NearBuyHQ/{shopId}/reports.
 * shopId == userId (they are the same thing).
 */
public class ReportRepository {

    private final FirebaseFirestore db;

    public ReportRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    private CollectionReference reportsRef(String shopId) {
        return db.collection(FirebaseCollections.USERS)
                 .document(shopId)
                 .collection(FirebaseCollections.USER_REPORTS);
    }

    // ── Create ────────────────────────────────────────────────────────────

    /**
     * Submit a new report. Typically called from the customer app,
     * but can also be created by admins.
     */
    public void createReport(String type, String subject, String description,
                             String shopId, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        long now = System.currentTimeMillis();
        Map<String, Object> data = new HashMap<>();
        data.put("type",        type);
        data.put("subject",     subject);
        data.put("description", description);
        data.put("shopId",      shopId);
        data.put("status",      "Pending");  // all new reports start as Pending
        data.put("createdAt",   now);
        data.put("updatedAt",   now);

        reportsRef(shopId).add(data)
                .addOnSuccessListener(ref -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ── Read ──────────────────────────────────────────────────────────────

    /** Load all reports for a specific shop owner. */
    public void getReportsByShop(String shopId, DataCallback<List<Map<String, Object>>> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        // NOTE: We intentionally avoid orderBy("createdAt") here because Firestore silently
        // excludes any document that doesn't have the sorted field.  Reports created by the
        // customer app may not have 'createdAt', so we fetch ALL documents and sort client-side.
        reportsRef(shopId).get()
                .addOnSuccessListener(snaps -> {
                    List<Map<String, Object>> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snaps.getDocuments()) {
                        Map<String, Object> item = doc.getData();
                        if (item != null) {
                            item.put("id", doc.getId());
                            list.add(item);
                        }
                    }
                    // Sort newest-first using createdAt / created_at / timestamp fields
                    Collections.sort(list, (a, b) -> {
                        long ta = resolveTimestamp(a, "createdAt", "created_at", "timestamp");
                        long tb = resolveTimestamp(b, "createdAt", "created_at", "timestamp");
                        return Long.compare(tb, ta);
                    });
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onError);
    }

    /** Resolve a millisecond timestamp from a map, checking multiple possible field names. */
    private static long resolveTimestamp(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object val = map.get(key);
            if (val instanceof Number) return ((Number) val).longValue();
            if (val instanceof Timestamp) return ((Timestamp) val).toDate().getTime();
        }
        return 0L;
    }

    /** @deprecated pass shopId; use getReportsByShop(shopId, cb) */
    @Deprecated
    public void getAllReports(DataCallback<List<Map<String, Object>>> callback) {
        callback.onError(new IllegalStateException("shopId required – use getReportsByShop(shopId, cb)"));
    }

    // ── Update ────────────────────────────────────────────────────────────

    public void updateStatus(String reportId, String shopId, String status, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        reportsRef(shopId).document(reportId)
                .update("status", status, "updatedAt", System.currentTimeMillis())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /** @deprecated pass shopId */
    @Deprecated
    public void updateStatus(String reportId, String status, OperationCallback callback) {
        callback.onError(new IllegalStateException("shopId required – use updateStatus(reportId, shopId, status, cb)"));
    }

    // ── Delete ────────────────────────────────────────────────────────────

    public void deleteReport(String reportId, String shopId, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        reportsRef(shopId).document(reportId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /** @deprecated pass shopId */
    @Deprecated
    public void deleteReport(String reportId, OperationCallback callback) {
        callback.onError(new IllegalStateException("shopId required – use deleteReport(reportId, shopId, cb)"));
    }
}

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

        reportsRef(shopId).orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snaps -> {
                    List<Map<String, Object>> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snaps.getDocuments()) {
                        Map<String, Object> item = doc.getData();
                        if (item != null) {
                            item.put("id", doc.getId());
                            list.add(item);
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onError);
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

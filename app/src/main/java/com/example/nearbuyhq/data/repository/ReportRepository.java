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
 * Repository for shop/user reports stored in Firestore.
 *
 * Customers can submit reports against shops from the customer app.
 * This admin app can read and resolve them.
 *
 * Firestore document structure:
 *   reports/{id}
 *     ├── type        – "Shop Violation" | "False Advertisement" | etc.
 *     ├── subject     – the shop name or item involved
 *     ├── description – details from the reporter
 *     ├── shopId      – which shop this report is about
 *     ├── status      – "Pending" | "Reviewed" | "Resolved"
 *     ├── createdAt
 *     └── updatedAt
 */
public class ReportRepository {

    private final CollectionReference reportsRef;

    public ReportRepository() {
        this.reportsRef = FirebaseFirestore.getInstance()
                .collection(FirebaseCollections.REPORTS);
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

        reportsRef.add(data)
                .addOnSuccessListener(ref -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ── Read ──────────────────────────────────────────────────────────────

    /**
     * Load all reports, newest first.
     * Returns raw maps – the caller (Reports.java) converts them to Report objects.
     */
    public void getAllReports(DataCallback<List<Map<String, Object>>> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        reportsRef.orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Map<String, Object>> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
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

    /**
     * Load reports that belong to a specific shop.
     */
    public void getReportsByShop(String shopId, DataCallback<List<Map<String, Object>>> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        reportsRef.whereEqualTo("shopId", shopId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Map<String, Object>> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
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

    // ── Update ────────────────────────────────────────────────────────────

    /**
     * Update the status of a report (e.g. "Pending" → "Reviewed" → "Resolved").
     */
    public void updateStatus(String reportId, String status, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }
        reportsRef.document(reportId)
                .update("status", status, "updatedAt", System.currentTimeMillis())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ── Delete ────────────────────────────────────────────────────────────

    /** Remove a report document. */
    public void deleteReport(String reportId, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }
        reportsRef.document(reportId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }
}


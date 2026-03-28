package com.example.nearbuyhq.data.repository;

import com.example.nearbuyhq.core.firebase.FirebaseConfig;
import com.example.nearbuyhq.data.remote.firebase.FirebaseCollections;
import com.example.nearbuyhq.orders.Order;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * All order data lives under NearBuyHQ/{shopId}/orders.
 * The shopId is the owner's userId (they are the same thing).
 */
public class OrderRepository {

    private final FirebaseFirestore db;

    public OrderRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // ── Subcollection ref ────────────────────────────────────────────────

    private CollectionReference ordersRef(String shopId) {
        return db.collection(FirebaseCollections.USERS)
                 .document(shopId)
                 .collection(FirebaseCollections.USER_ORDERS);
    }

    // ── Create ────────────────────────────────────────────────────────────

    public void createOrder(Order order, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) { callback.onError(new IllegalStateException("Firebase is disabled")); return; }
        if (order == null) { callback.onError(new IllegalArgumentException("Order is required")); return; }

        String shopId = order.getShopId();
        if (shopId == null || shopId.isEmpty()) { callback.onError(new IllegalArgumentException("shopId is required on the order")); return; }

        String id = order.getOrderId();
        if (id == null || id.trim().isEmpty()) {
            // Generate professional order number: ORD-YYYYMMDD-XXXX
            String datePart = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
            String randPart = String.format(Locale.getDefault(), "%04d", (int)(Math.random() * 9000) + 1000);
            id = "ORD-" + datePart + "-" + randPart;
        }

        ordersRef(shopId).document(id)
                .set(order.toMap())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ── Read ──────────────────────────────────────────────────────────────

    /** Load all orders for a specific shop owner — use this in Order_List, Dashboard, Analytics. */
    public void getOrdersByShopId(String shopId, DataCallback<List<Order>> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) { callback.onError(new IllegalStateException("Firebase is disabled")); return; }

        ordersRef(shopId).orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snaps -> {
                    List<Order> orders = new ArrayList<>();
                    for (DocumentSnapshot doc : snaps.getDocuments()) {
                        Order o = Order.fromMap(doc.getId(), doc.getData());
                        if (o != null) orders.add(o);
                    }
                    callback.onSuccess(orders);
                })
                .addOnFailureListener(callback::onError);
    }

    /** @deprecated Use getOrdersByShopId */
    public void getOrders(DataCallback<List<Order>> callback) {
        callback.onError(new IllegalStateException("shopId required – use getOrdersByShopId(shopId, cb)"));
    }

    public void getOrder(String orderId, String shopId, DataCallback<Order> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) { callback.onError(new IllegalStateException("Firebase is disabled")); return; }

        ordersRef(shopId).document(orderId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) { callback.onError(new IllegalStateException("Order not found")); return; }
                    callback.onSuccess(Order.fromMap(doc.getId(), doc.getData()));
                })
                .addOnFailureListener(callback::onError);
    }

    /** @deprecated pass shopId */
    public void getOrder(String orderId, DataCallback<Order> callback) {
        callback.onError(new IllegalStateException("shopId required – use getOrder(orderId, shopId, cb)"));
    }

    /** Orders filtered by date range for the Dashboard. */
    public void getOrdersByShopIdAndDateRange(String shopId, long fromMs, long toMs,
                                              DataCallback<List<Order>> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) { callback.onError(new IllegalStateException("Firebase is disabled")); return; }

        ordersRef(shopId)
                .whereGreaterThanOrEqualTo("createdAt", fromMs)
                .whereLessThanOrEqualTo("createdAt", toMs)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snaps -> {
                    List<Order> orders = new ArrayList<>();
                    for (DocumentSnapshot doc : snaps.getDocuments()) {
                        Order o = Order.fromMap(doc.getId(), doc.getData());
                        if (o != null) orders.add(o);
                    }
                    callback.onSuccess(orders);
                })
                .addOnFailureListener(callback::onError);
    }

    /** @deprecated pass shopId */
    public void getOrdersByDateRange(long fromMs, long toMs, DataCallback<List<Order>> callback) {
        callback.onError(new IllegalStateException("shopId required – use getOrdersByShopIdAndDateRange"));
    }

    // ── Update ────────────────────────────────────────────────────────────

    public void updateOrderStatus(String orderId, String shopId, String status, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) { callback.onError(new IllegalStateException("Firebase is disabled")); return; }

        ordersRef(shopId).document(orderId)
                .update("status", status, "updatedAt", System.currentTimeMillis())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /** @deprecated pass shopId */
    public void updateOrderStatus(String orderId, String status, OperationCallback callback) {
        callback.onError(new IllegalStateException("shopId required – use updateOrderStatus(orderId, shopId, status, cb)"));
    }

    // ── Delete ────────────────────────────────────────────────────────────

    public void deleteOrder(String orderId, String shopId, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) { callback.onError(new IllegalStateException("Firebase is disabled")); return; }

        ordersRef(shopId).document(orderId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /** @deprecated pass shopId */
    public void deleteOrder(String orderId, OperationCallback callback) {
        callback.onError(new IllegalStateException("shopId required – use deleteOrder(orderId, shopId, cb)"));
    }
}

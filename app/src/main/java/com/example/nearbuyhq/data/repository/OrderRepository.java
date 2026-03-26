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

public class OrderRepository {

    private final CollectionReference ordersRef;

    public OrderRepository() {
        this.ordersRef = FirebaseFirestore.getInstance().collection(FirebaseCollections.ORDERS);
    }

    public void createOrder(Order order, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }
        if (order == null) {
            callback.onError(new IllegalArgumentException("Order is required"));
            return;
        }

        String id = order.getOrderId();
        if (id == null || id.trim().isEmpty()) {
            // Generate professional order number: ORD-YYYYMMDD-XXXX
            String datePart = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
            String randPart = String.format(Locale.getDefault(), "%04d", (int)(Math.random() * 9000) + 1000);
            id = "ORD-" + datePart + "-" + randPart;
        }

        ordersRef.document(id)
                .set(order.toMap())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void getOrders(DataCallback<List<Order>> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        ordersRef.orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Order> orders = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Order order = Order.fromMap(document.getId(), document.getData());
                        if (order != null) {
                            orders.add(order);
                        }
                    }
                    callback.onSuccess(orders);
                })
                .addOnFailureListener(callback::onError);
    }

    public void getOrder(String orderId, DataCallback<Order> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }
        if (orderId == null || orderId.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Order ID is required"));
            return;
        }

        ordersRef.document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        callback.onError(new IllegalStateException("Order not found"));
                        return;
                    }
                    callback.onSuccess(Order.fromMap(documentSnapshot.getId(), documentSnapshot.getData()));
                })
                .addOnFailureListener(callback::onError);
    }

    public void updateOrderStatus(String orderId, String status, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }
        if (orderId == null || orderId.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Order ID is required"));
            return;
        }

        ordersRef.document(orderId)
                .update("status", status, "updatedAt", System.currentTimeMillis())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void deleteOrder(String orderId, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }
        ordersRef.document(orderId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /**
     * Fetch orders whose createdAt timestamp falls within [fromMs, toMs].
     * Used by the Dashboard Business Overview date-range filter.
     *
     * @param fromMs start of the range in milliseconds (epoch)
     * @param toMs   end of the range in milliseconds (epoch); pass Long.MAX_VALUE for "all time"
     */
    public void getOrdersByDateRange(long fromMs, long toMs, DataCallback<List<Order>> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        // Use createdAt field; fall back gracefully if the field is missing (old documents)
        Query query = ordersRef
                .whereGreaterThanOrEqualTo("createdAt", fromMs)
                .whereLessThanOrEqualTo("createdAt", toMs)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        query.get()
                .addOnSuccessListener(snapshots -> {
                    List<Order> orders = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Order o = Order.fromMap(doc.getId(), doc.getData());
                        if (o != null) orders.add(o);
                    }
                    callback.onSuccess(orders);
                })
                .addOnFailureListener(callback::onError);
    }
}


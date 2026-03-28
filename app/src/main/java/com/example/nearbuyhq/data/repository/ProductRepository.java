package com.example.nearbuyhq.data.repository;

import com.example.nearbuyhq.core.firebase.FirebaseConfig;
import com.example.nearbuyhq.data.remote.firebase.FirebaseCollections;
import com.example.nearbuyhq.products.ProductItem;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * All product data lives under NearBuyHQ/{userId}/products.
 * No global collection – every read and write is scoped to the owner's document.
 */
public class ProductRepository {

    private final FirebaseFirestore db;

    public ProductRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // ── Subcollection ref ────────────────────────────────────────────────

    private CollectionReference productsRef(String userId) {
        return db.collection(FirebaseCollections.USERS)
                 .document(userId)
                 .collection(FirebaseCollections.USER_PRODUCTS);
    }

    // ── Create ────────────────────────────────────────────────────────────

    public void createProduct(ProductItem item, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) { callback.onError(new IllegalStateException("Firebase is disabled")); return; }
        if (item == null || !item.isValidForSave()) { callback.onError(new IllegalArgumentException("Invalid product data")); return; }

        String userId = item.getShopId();   // userId == shopId
        if (userId == null || userId.isEmpty() || "global".equals(userId)) {
            callback.onError(new IllegalArgumentException("User ID is required to save a product"));
            return;
        }

        String docId = item.getId();
        if (docId == null || docId.trim().isEmpty()) {
            docId = productsRef(userId).document().getId();
            item.setId(docId);
        }

        productsRef(userId).document(docId)
                .set(item.toMap())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ── Update ────────────────────────────────────────────────────────────

    public void updateProduct(ProductItem item, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) { callback.onError(new IllegalStateException("Firebase is disabled")); return; }
        if (item == null || item.getId() == null || item.getId().trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Product ID is required")); return;
        }

        String userId = item.getShopId();
        if (userId == null || userId.isEmpty()) { callback.onError(new IllegalArgumentException("User ID is required")); return; }

        item.setUpdatedAt(System.currentTimeMillis());
        productsRef(userId).document(item.getId())
                .set(item.toMap())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ── Delete ────────────────────────────────────────────────────────────

    public void deleteProduct(String productId, String userId, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) { callback.onError(new IllegalStateException("Firebase is disabled")); return; }
        if (productId == null || productId.trim().isEmpty()) { callback.onError(new IllegalArgumentException("Product ID is required")); return; }

        productsRef(userId).document(productId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /** @deprecated pass userId; exists only so legacy call-sites still compile */
    @Deprecated
    public void deleteProduct(String productId, OperationCallback callback) {
        callback.onError(new IllegalStateException("userId is required – use deleteProduct(id, userId, cb)"));
    }

    // ── Read ──────────────────────────────────────────────────────────────

    /** Load a single product from the owner's subcollection. */
    public void getProduct(String productId, String userId, DataCallback<ProductItem> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) { callback.onError(new IllegalStateException("Firebase is disabled")); return; }

        productsRef(userId).document(productId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) callback.onSuccess(mapProduct(doc));
                    else callback.onError(new IllegalStateException("Product not found"));
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Load products for a shop owner (Inventory, Dashboard, Products_List).
     * Pass "All" or null for no category filter.
     */
    public void getProductsByShopId(String shopId, String category,
                                    DataCallback<List<ProductItem>> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) { callback.onError(new IllegalStateException("Firebase is disabled")); return; }
        if (shopId == null || shopId.trim().isEmpty()) { callback.onError(new IllegalArgumentException("User / shop ID is required")); return; }

        CollectionReference ref = productsRef(shopId);
        Query query = ref.orderBy("updatedAt", Query.Direction.DESCENDING);
        if (category != null && !category.trim().isEmpty() && !"All".equalsIgnoreCase(category)) {
            query = ref.whereEqualTo("category", category);
        }

        query.get()
                .addOnSuccessListener(snaps -> {
                    List<ProductItem> items = new ArrayList<>();
                    for (DocumentSnapshot doc : snaps.getDocuments()) {
                        ProductItem item = mapProduct(doc);
                        if (item != null) items.add(item);
                    }
                    callback.onSuccess(items);
                })
                .addOnFailureListener(callback::onError);
    }

    /** Convenience alias so existing callers that pass no category still compile. */
    public void getProducts(String category, DataCallback<List<ProductItem>> callback) {
        callback.onError(new IllegalStateException("shopId is required – use getProductsByShopId(shopId, category, cb)"));
    }

    /** Count low-stock products for the Dashboard. */
    public void getLowStockCount(String shopId, int threshold, DataCallback<Integer> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) { callback.onError(new IllegalStateException("Firebase is disabled")); return; }

        productsRef(shopId).get().addOnSuccessListener(snaps -> {
            int count = 0;
            for (DocumentSnapshot doc : snaps.getDocuments()) {
                ProductItem item = mapProduct(doc);
                if (item != null && item.isLowStock(threshold)) count++;
            }
            callback.onSuccess(count);
        }).addOnFailureListener(callback::onError);
    }

    private ProductItem mapProduct(DocumentSnapshot doc) {
        return ProductItem.fromMap(doc.getId(), doc.getData());
    }
}

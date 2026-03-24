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
 * Handles all Firestore read/write operations for the 'products' collection.
 *
 * Every product document has a 'shopId' field that links it to the shop.
 * The customer app queries:
 *   products WHERE name contains "rice" AND status == "Available"
 * then groups results by shopId to show shop + price + distance.
 */
public class ProductRepository {

    private final CollectionReference productsRef;

    public ProductRepository() {
        this.productsRef = FirebaseFirestore.getInstance()
                .collection(FirebaseCollections.PRODUCTS);
    }

    // ── Create ────────────────────────────────────────────────────────────

    /** Save a new product. Auto-generates a Firestore ID if none is set. */
    public void createProduct(ProductItem item, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }
        if (item == null || !item.isValidForSave()) {
            callback.onError(new IllegalArgumentException("Invalid product data"));
            return;
        }

        String docId = item.getId();
        if (docId == null || docId.trim().isEmpty()) {
            docId = productsRef.document().getId();
            item.setId(docId);
        }

        productsRef.document(docId)
                .set(item.toMap())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ── Update ────────────────────────────────────────────────────────────

    /** Overwrite a product document (full update). */
    public void updateProduct(ProductItem item, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }
        if (item == null || item.getId() == null || item.getId().trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Product ID is required"));
            return;
        }

        item.setUpdatedAt(System.currentTimeMillis());
        productsRef.document(item.getId())
                .set(item.toMap())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ── Delete ────────────────────────────────────────────────────────────

    public void deleteProduct(String productId, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }
        if (productId == null || productId.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Product ID is required"));
            return;
        }

        productsRef.document(productId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ── Read ──────────────────────────────────────────────────────────────

    /** Load a single product by its Firestore document ID. */
    public void getProduct(String productId, DataCallback<ProductItem> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }
        if (productId == null || productId.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Product ID is required"));
            return;
        }

        productsRef.document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onSuccess(mapProduct(documentSnapshot));
                    } else {
                        callback.onError(new IllegalStateException("Product not found"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Load products filtered by category (pass "All" or null for no filter).
     * Used by the global product list – loads all shops' products.
     */
    public void getProducts(String category, DataCallback<List<ProductItem>> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        Query query = productsRef.orderBy("updatedAt", Query.Direction.DESCENDING);
        if (category != null && !category.trim().isEmpty() && !"All".equalsIgnoreCase(category)) {
            query = query.whereEqualTo("category", category);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ProductItem> items = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        ProductItem item = mapProduct(document);
                        if (item != null) items.add(item);
                    }
                    callback.onSuccess(items);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Load only the products that belong to a specific shop.
     *
     * This is what the admin sees in their Inventory/Product list –
     * only their own shop's products. Also used by the customer app to
     * list all items sold by a particular shop.
     *
     * @param shopId   Firestore document ID of the shop
     * @param category Optional category filter; pass "All" or null for all
     */
    public void getProductsByShopId(String shopId, String category,
                                    DataCallback<List<ProductItem>> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }
        if (shopId == null || shopId.trim().isEmpty()) {
            // Fall back to all products if shopId is unknown
            getProducts(category, callback);
            return;
        }

        Query query = productsRef.whereEqualTo("shopId", shopId)
                .orderBy("updatedAt", Query.Direction.DESCENDING);

        if (category != null && !category.trim().isEmpty() && !"All".equalsIgnoreCase(category)) {
            query = productsRef.whereEqualTo("shopId", shopId)
                    .whereEqualTo("category", category);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ProductItem> items = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        ProductItem item = mapProduct(document);
                        if (item != null) items.add(item);
                    }
                    callback.onSuccess(items);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Count how many products are low-stock for a specific shop.
     * Used by the Dashboard to show the "Low Stock" metric.
     */
    public void getLowStockCount(String shopId, int threshold, DataCallback<Integer> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        Query query = (shopId != null && !shopId.isEmpty())
                ? productsRef.whereEqualTo("shopId", shopId)
                : productsRef;

        query.get().addOnSuccessListener(snapshots -> {
            int count = 0;
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                ProductItem item = mapProduct(doc);
                if (item != null && item.isLowStock(threshold)) count++;
            }
            callback.onSuccess(count);
        }).addOnFailureListener(callback::onError);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private ProductItem mapProduct(DocumentSnapshot documentSnapshot) {
        return ProductItem.fromMap(documentSnapshot.getId(), documentSnapshot.getData());
    }
}


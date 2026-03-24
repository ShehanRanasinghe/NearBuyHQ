package com.example.nearbuyhq.data.repository;

import com.example.nearbuyhq.core.firebase.FirebaseConfig;
import com.example.nearbuyhq.data.remote.firebase.FirebaseCollections;
import com.example.nearbuyhq.shops.Shop;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles all Firestore read/write operations for the 'shops' collection.
 *
 * Each shop document stores the owner's UID so it can be retrieved after login.
 * The latitude/longitude fields are read by the customer app to calculate
 * how far each shop is from the customer's current location.
 */
public class ShopRepository {

    private final CollectionReference shopsRef;

    public ShopRepository() {
        this.shopsRef = FirebaseFirestore.getInstance()
                .collection(FirebaseCollections.SHOPS);
    }

    // ── Create ────────────────────────────────────────────────────────────

    /** Save a new shop document. Generates an ID if one is not provided. */
    public void createShop(Shop shop, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }
        if (shop == null || !shop.isValidForSave()) {
            callback.onError(new IllegalArgumentException("Invalid shop data"));
            return;
        }

        String docId = shop.getId();
        if (docId == null || docId.trim().isEmpty()) {
            docId = shopsRef.document().getId(); // auto-generate
            shop.setId(docId);
        }

        shopsRef.document(docId)
                .set(shop.toMap())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ── Update ────────────────────────────────────────────────────────────

    /** Replace an entire shop document. */
    public void updateShop(Shop shop, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }
        if (shop == null || shop.getId() == null || shop.getId().trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Shop ID is required"));
            return;
        }
        shopsRef.document(shop.getId())
                .set(shop.toMap())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /**
     * Update only the profile fields shown on the Profile page
     * (name, openingHours, website, contact, location, category).
     */
    public void updateShopProfile(String shopId, String name, String location,
                                   String category, String openingHours,
                                   String website, String contact,
                                   OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("name",         name);
        updates.put("location",     location);
        updates.put("address",      location);       // keep alias in sync
        updates.put("category",     category);
        updates.put("openingHours", openingHours);
        updates.put("website",      website);
        updates.put("contact",      contact);
        updates.put("contactNumber",contact);        // keep alias in sync
        updates.put("updatedAt",    System.currentTimeMillis());

        shopsRef.document(shopId)
                .update(updates)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /**
     * Save GPS coordinates for a shop.
     * Called when the device obtains a location fix after the shop is registered.
     * These coordinates are used by the customer app to calculate distance.
     */
    public void updateLocation(String shopId, double latitude, double longitude,
                               OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("latitude",  latitude);
        updates.put("longitude", longitude);
        updates.put("updatedAt", System.currentTimeMillis());

        shopsRef.document(shopId)
                .update(updates)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /** Toggle shop Active / Inactive status. */
    public void updateStatus(String shopId, String status, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }
        if (shopId == null || shopId.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Shop ID is required"));
            return;
        }
        shopsRef.document(shopId)
                .update("status", status, "updatedAt", System.currentTimeMillis())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ── Delete ────────────────────────────────────────────────────────────

    public void deleteShop(String shopId, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }
        if (shopId == null || shopId.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Shop ID is required"));
            return;
        }
        shopsRef.document(shopId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ── Read ──────────────────────────────────────────────────────────────

    /** Load a single shop by its Firestore document ID. */
    public void getShop(String shopId, DataCallback<Shop> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }
        if (shopId == null || shopId.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Shop ID is required"));
            return;
        }
        shopsRef.document(shopId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onSuccess(mapShop(documentSnapshot));
                    } else {
                        callback.onError(new IllegalStateException("Shop not found"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Find the shop that belongs to a specific Firebase Auth user.
     * Returns null in the callback if the user hasn't created a shop yet.
     * This is used on startup to restore the session's shopId.
     */
    public void getShopByOwnerUid(String ownerUid, DataCallback<Shop> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }
        if (ownerUid == null || ownerUid.trim().isEmpty()) {
            callback.onSuccess(null); // no shop yet
            return;
        }
        shopsRef.whereEqualTo("ownerUid", ownerUid)
                .limit(1)          // one user = one shop in this design
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots.isEmpty()) {
                        callback.onSuccess(null); // user hasn't created a shop yet
                    } else {
                        callback.onSuccess(mapShop(snapshots.getDocuments().get(0)));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    /** Load all shops ordered by most recently updated. */
    public void getAllShops(DataCallback<List<Shop>> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }
        shopsRef.orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Shop> shops = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Shop shop = mapShop(document);
                        if (shop != null) shops.add(shop);
                    }
                    callback.onSuccess(shops);
                })
                .addOnFailureListener(callback::onError);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Shop mapShop(DocumentSnapshot documentSnapshot) {
        return Shop.fromMap(documentSnapshot.getId(), documentSnapshot.getData());
    }
}


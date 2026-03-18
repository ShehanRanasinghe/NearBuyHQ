package com.example.nearbuyhq.data.remote.firebase;

/**
 * Reserved Firestore collection names for consistent usage across features.
 * No read/write logic is implemented yet.
 */
public final class FirebaseCollections {

    public static final String USERS = "users";
    public static final String SHOPS = "shops";
    public static final String PRODUCTS = "products";
    public static final String ORDERS = "orders";
    public static final String DISCOUNTS = "discounts";
    public static final String REPORTS = "reports";
    public static final String NOTIFICATIONS = "notifications";

    private FirebaseCollections() {
        // Utility class
    }
}


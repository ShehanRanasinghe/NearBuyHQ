package com.example.nearbuyhq.data.remote.firebase;

/**
 * All Firestore collection names used across the app.
 * Centralised here so both admin and customer apps use the same names.
 */
public final class FirebaseCollections {

    // ── Core collections ──────────────────────────────────────────────────
    public static final String USERS         = "users";         // shop owner accounts
    public static final String SHOPS         = "shops";         // shop / branch data (includes lat/lng for customer app)
    public static final String PRODUCTS      = "products";      // products linked to a shopId
    public static final String ORDERS        = "orders";        // orders placed via customer app
    public static final String DISCOUNTS     = "discounts";     // generic discounts (legacy)
    public static final String PROMOTIONS    = "promotions";    // seasonal / custom promotions
    public static final String DEALS         = "deals";         // flash / short-term deals
    public static final String REPORTS       = "reports";       // user/admin submitted reports
    public static final String NOTIFICATIONS = "notifications"; // in-app notifications per shop
    public static final String OTP_CODES     = "otp_codes";     // email OTP verification codes

    private FirebaseCollections() {
        // Utility class
    }
}


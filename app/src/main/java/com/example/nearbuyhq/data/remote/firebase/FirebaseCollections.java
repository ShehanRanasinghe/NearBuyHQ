package com.example.nearbuyhq.data.remote.firebase;

/**
 * All Firestore collection/subcollection names used across the app.
 * <p>
 * Structure:
 * <pre>
 *   NearBuyHQ/                    ← root collection  (USERS constant)
 *    └── {userId}/                ← one document per shop owner
 *         ├── products/           ← USER_PRODUCTS
 *         ├── deals/              ← USER_DEALS
 *         ├── promotions/         ← USER_PROMOTIONS
 *         ├── orders/             ← USER_ORDERS
 *         ├── reports/            ← USER_REPORTS
 *         └── notifications/      ← USER_NOTIFICATIONS
 * </pre>
 * NOTE: userId == shopId — registering an account also registers the shop.
 */
public final class FirebaseCollections {

    // ── Root collection ───────────────────────────────────────────────────
    public static final String USERS = "NearBuyHQ";   // root: NearBuyHQ/{userId}

    // ── Top-level collections (pre-auth / cross-user) ─────────────────────
    /** OTP codes live at root level because they are written before a user is authenticated. */
    public static final String OTP_CODES = "otp_codes";

    // ── User-owned subcollections  NearBuyHQ/{userId}/…  ─────────────────
    public static final String USER_PRODUCTS      = "products";      // NearBuyHQ/{uid}/products
    public static final String USER_DEALS         = "deals";         // NearBuyHQ/{uid}/deals
    public static final String USER_PROMOTIONS    = "promotions";    // NearBuyHQ/{uid}/promotions
    public static final String USER_ORDERS        = "orders";        // NearBuyHQ/{uid}/orders
    public static final String USER_REPORTS       = "reports";       // NearBuyHQ/{uid}/reports
    public static final String USER_NOTIFICATIONS = "notifications"; // NearBuyHQ/{uid}/notifications

    private FirebaseCollections() { /* utility class */ }
}

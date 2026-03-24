package com.example.nearbuyhq.core;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager – Singleton that stores session info in SharedPreferences.
 *
 * After login, we cache the current user's UID, name, email, shopId and shopName
 * so every screen can quickly read them without hitting Firestore every time.
 *
 * Usage:
 *   SessionManager.getInstance(context).getShopId()
 */
public class SessionManager {

    // Name of the SharedPreferences file
    private static final String PREFS_NAME  = "nearbuyhq_session";

    // Keys stored in preferences
    private static final String KEY_USER_ID    = "userId";
    private static final String KEY_USER_NAME  = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_PHONE = "userPhone";
    private static final String KEY_SHOP_ID    = "shopId";
    private static final String KEY_SHOP_NAME  = "shopName";

    private static SessionManager instance;
    private final SharedPreferences prefs;

    // Private constructor – use getInstance()
    private SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Returns the single SessionManager instance for the application. */
    public static SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    // ── User ID ──────────────────────────────────────────────────────────

    /** Save the Firebase Auth UID after successful login / registration. */
    public void saveUserId(String uid) {
        prefs.edit().putString(KEY_USER_ID, uid).apply();
    }

    /** Get the currently logged-in user's Firebase Auth UID. */
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }

    // ── User Name ────────────────────────────────────────────────────────

    public void saveUserName(String name) {
        prefs.edit().putString(KEY_USER_NAME, name).apply();
    }

    /** Returns the display name of the logged-in shop owner. */
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "Shop Owner");
    }

    // ── User Email ───────────────────────────────────────────────────────

    public void saveUserEmail(String email) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply();
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    // ── User Phone ───────────────────────────────────────────────────────

    public void saveUserPhone(String phone) {
        prefs.edit().putString(KEY_USER_PHONE, phone).apply();
    }

    public String getUserPhone() {
        return prefs.getString(KEY_USER_PHONE, "");
    }

    // ── Shop ID ──────────────────────────────────────────────────────────

    /**
     * Save the Firestore document ID of the shop that belongs to this user.
     * This is stored after the user creates/registers their shop.
     * Products are saved with this shopId so the customer app can link them.
     */
    public void saveShopId(String shopId) {
        prefs.edit().putString(KEY_SHOP_ID, shopId).apply();
    }

    /**
     * Get the shopId for the currently logged-in owner.
     * Returns empty string if no shop has been created yet.
     */
    public String getShopId() {
        return prefs.getString(KEY_SHOP_ID, "");
    }

    /** Returns true if the user has already created their shop. */
    public boolean hasShop() {
        String id = getShopId();
        return id != null && !id.trim().isEmpty();
    }

    // ── Shop Name ────────────────────────────────────────────────────────

    public void saveShopName(String shopName) {
        prefs.edit().putString(KEY_SHOP_NAME, shopName).apply();
    }

    public String getShopName() {
        return prefs.getString(KEY_SHOP_NAME, "My Shop");
    }

    // ── Session control ──────────────────────────────────────────────────

    /** Returns true if a user is saved in the session (not necessarily Firebase auth). */
    public boolean isLoggedIn() {
        String uid = getUserId();
        return uid != null && !uid.trim().isEmpty();
    }

    /** Wipe all session data – call on logout. */
    public void clearSession() {
        prefs.edit().clear().apply();
    }
}


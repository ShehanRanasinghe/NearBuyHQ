package com.example.nearbuyhq.data.repository;

import android.content.Context;

import com.example.nearbuyhq.core.SessionManager;
import com.example.nearbuyhq.core.firebase.FirebaseConfig;
import com.example.nearbuyhq.data.remote.firebase.FirebaseCollections;
import com.example.nearbuyhq.shops.Shop;
import com.example.nearbuyhq.users.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages Firebase Authentication (register / login / logout / password reset).
 *
 * After a successful login, it:
 *   1. Loads the user's Firestore profile
 *   2. Looks up their linked shop (ownerUid == currentUid)
 *   3. Saves everything to SessionManager so other screens can read it
 */
public class AuthRepository {

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    public AuthRepository() {
        this.auth      = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    // ── Register ──────────────────────────────────────────────────────────

    /**
     * Create a new Firebase Auth account and write the user profile to Firestore.
     * The user still needs to create their shop after registration.
     */
    public void register(String fullName, String email, String username,
                         String password, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        callback.onError(new IllegalStateException("Failed to create user"));
                        return;
                    }

                    // Save user profile document to Firestore
                    long now = System.currentTimeMillis();
                    User user = new User(firebaseUser.getUid(), fullName, email, username, "Active", now, now);
                    firestore.collection(FirebaseCollections.USERS)
                            .document(firebaseUser.getUid())
                            .set(user.toMap())
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }

    // ── Login ─────────────────────────────────────────────────────────────

    /**
     * Sign in with email or username, then load the user profile + linked shop
     * into SessionManager so all screens can access them without extra Firestore calls.
     *
     * @param context Needed to initialise SessionManager
     */
    public void login(String usernameOrEmail, String password,
                      Context context, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        if (usernameOrEmail.contains("@")) {
            // Direct email login
            signInWithEmail(usernameOrEmail, password, context, callback);
        } else {
            // Username → look up their email first
            firestore.collection(FirebaseCollections.USERS)
                    .whereEqualTo("username", usernameOrEmail)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(snapshots -> handleUsernameLookup(snapshots, password, context, callback))
                    .addOnFailureListener(callback::onError);
        }
    }

    /** Sign in with just email + password (no context, used by Login.java existing code). */
    public void login(String usernameOrEmail, String password, OperationCallback callback) {
        login(usernameOrEmail, password, null, callback);
    }

    // ── Password Reset ────────────────────────────────────────────────────

    /**
     * Send a Firebase password-reset email to the given address.
     * The user clicks the link in the email to set a new password.
     */
    public void sendPasswordResetEmail(String email, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }
        if (email == null || email.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Email is required"));
            return;
        }

        auth.sendPasswordResetEmail(email.trim())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // ── Update profile ────────────────────────────────────────────────────

    /**
     * Update the user's Firestore profile (name, phone).
     */
    public void updateUserProfile(String uid, String name, String phone,
                                  OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name",      name);
        updates.put("phone",     phone);
        updates.put("updatedAt", System.currentTimeMillis());

        firestore.collection(FirebaseCollections.USERS)
                .document(uid)
                .update(updates)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /**
     * Load the current user's Firestore profile.
     */
    public void getUserProfile(String uid, DataCallback<User> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        firestore.collection(FirebaseCollections.USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        callback.onSuccess(User.fromMap(doc.getId(), doc.getData()));
                    } else {
                        callback.onError(new IllegalStateException("Profile not found"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    // ── Logout ────────────────────────────────────────────────────────────

    /** Sign out from Firebase Auth. */
    public void logout() {
        auth.signOut();
    }

    /** Sign out and clear all local session data. */
    public void logout(Context context) {
        auth.signOut();
        if (context != null) {
            SessionManager.getInstance(context).clearSession();
        }
    }

    // ── State ─────────────────────────────────────────────────────────────

    /** Returns true if a Firebase user is currently signed in. */
    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    /** Returns the current Firebase Auth user, or null if not signed in. */
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private void signInWithEmail(String email, String password,
                                  Context context, OperationCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // After sign-in, load profile + shop into session
                    FirebaseUser user = authResult.getUser();
                    if (user != null && context != null) {
                        loadSessionAfterLogin(user.getUid(), context, callback);
                    } else {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    private void handleUsernameLookup(QuerySnapshot snapshots, String password,
                                       Context context, OperationCallback callback) {
        if (snapshots.isEmpty()) {
            callback.onError(new IllegalArgumentException("User not found"));
            return;
        }
        String email = snapshots.getDocuments().get(0).getString("email");
        if (email == null || email.trim().isEmpty()) {
            callback.onError(new IllegalStateException("User email is missing"));
            return;
        }
        signInWithEmail(email, password, context, callback);
    }

    /**
     * After successful Firebase sign-in, load user profile and their linked shop
     * into SessionManager. This allows any screen to read the data without
     * an extra Firestore round-trip.
     */
    private void loadSessionAfterLogin(String uid, Context context,
                                        OperationCallback callback) {
        SessionManager session = SessionManager.getInstance(context);
        session.saveUserId(uid);

        // Load user profile from Firestore
        firestore.collection(FirebaseCollections.USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name  = doc.getString("name");
                        String email = doc.getString("email");
                        String phone = doc.getString("phone");
                        if (name  != null) session.saveUserName(name);
                        if (email != null) session.saveUserEmail(email);
                        if (phone != null) session.saveUserPhone(phone);
                    }

                    // Now look up their linked shop
                    firestore.collection(FirebaseCollections.SHOPS)
                            .whereEqualTo("ownerUid", uid)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(shopSnaps -> {
                                if (!shopSnaps.isEmpty()) {
                                    String shopId   = shopSnaps.getDocuments().get(0).getId();
                                    String shopName = shopSnaps.getDocuments().get(0).getString("name");
                                    session.saveShopId(shopId);
                                    if (shopName != null) session.saveShopName(shopName);
                                }
                                callback.onSuccess(); // done – proceed to Dashboard
                            })
                            .addOnFailureListener(e -> callback.onSuccess()); // non-fatal
                })
                .addOnFailureListener(e -> callback.onSuccess()); // non-fatal – still log in
    }
}

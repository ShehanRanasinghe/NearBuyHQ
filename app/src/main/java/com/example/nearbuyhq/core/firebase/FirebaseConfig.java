package com.example.nearbuyhq.core.firebase;

/**
 * Central Firebase switches used by the app shell.
 * Backend wiring will be added in a later step.
 */
public final class FirebaseConfig {

    private static final boolean FIREBASE_ENABLED = false;

    private FirebaseConfig() {
        // Utility class
    }

    public static boolean isFirebaseEnabled() {
        return FIREBASE_ENABLED;
    }
}


package com.example.nearbuyhq.core.firebase;

import com.example.nearbuyhq.BuildConfig;

/**
 * Central Firebase switches used by the app shell.
 * Backend wiring will be added in a later step.
 */
public final class FirebaseConfig {

    private FirebaseConfig() {
        // Utility class
    }

    public static boolean isFirebaseEnabled() {
        return true; // Firebase is always enabled in production
    }

    public static String getProjectId() {
        return BuildConfig.FIREBASE_PROJECT_ID;
    }
}


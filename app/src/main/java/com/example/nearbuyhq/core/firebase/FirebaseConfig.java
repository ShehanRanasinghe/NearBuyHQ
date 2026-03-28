package com.example.nearbuyhq.core.firebase;

import com.example.nearbuyhq.BuildConfig;

// Central Firebase feature switches – controls whether live Firestore calls are made.
// Set FIREBASE_ENABLED=true in your .env file (or gradle.properties) to activate Firebase.
public final class FirebaseConfig {

    // Prevent instantiation of this utility class
    private FirebaseConfig() {}

    // Returns true when Firebase is active; driven by BuildConfig.FIREBASE_ENABLED
    // which is set from FIREBASE_ENABLED in .env / gradle.properties.
    public static boolean isFirebaseEnabled() {
        return BuildConfig.FIREBASE_ENABLED;
    }
}


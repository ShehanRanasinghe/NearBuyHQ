package com.example.nearbuyhq.core.firebase;

// Central Firebase feature switches – controls whether live Firestore calls are made.
public final class FirebaseConfig {

    // Prevent instantiation of this utility class
    private FirebaseConfig() {}

    // Returns true when Firebase is active; set to false to stub out all backend calls during testing
    public static boolean isFirebaseEnabled() {
        return true; // Firebase is always enabled in production
    }
}


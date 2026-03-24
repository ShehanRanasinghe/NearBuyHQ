package com.example.nearbuyhq.data.repository;

import com.example.nearbuyhq.core.firebase.FirebaseConfig;
import com.example.nearbuyhq.data.remote.firebase.FirebaseCollections;
import com.example.nearbuyhq.users.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private final CollectionReference usersRef;

    public UserRepository() {
        this.usersRef = FirebaseFirestore.getInstance().collection(FirebaseCollections.USERS);
    }

    public void getUsers(DataCallback<List<User>> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }

        usersRef.orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        User user = User.fromMap(document.getId(), document.getData());
                        if (user != null) {
                            users.add(user);
                        }
                    }
                    callback.onSuccess(users);
                })
                .addOnFailureListener(callback::onError);
    }

    public void updateStatus(String userId, String status, OperationCallback callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }
        if (userId == null || userId.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("User ID is required"));
            return;
        }

        usersRef.document(userId)
                .update("status", status, "updatedAt", System.currentTimeMillis())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void getUser(String userId, DataCallback<User> callback) {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            callback.onError(new IllegalStateException("Firebase is disabled"));
            return;
        }
        usersRef.document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        callback.onError(new IllegalStateException("User not found"));
                        return;
                    }
                    callback.onSuccess(User.fromMap(documentSnapshot.getId(), documentSnapshot.getData()));
                })
                .addOnFailureListener(callback::onError);
    }
}


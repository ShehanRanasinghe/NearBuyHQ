package com.example.nearbuyhq.data.repository;

// Callback for Firestore write operations (create, update, delete) that produce no return value.
public interface OperationCallback {
    void onSuccess();
    void onError(Exception exception);
}

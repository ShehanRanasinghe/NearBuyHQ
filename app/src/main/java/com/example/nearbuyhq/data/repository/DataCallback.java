package com.example.nearbuyhq.data.repository;

// Generic callback for Firestore operations that return a typed result (T).
public interface DataCallback<T> {
    void onSuccess(T data);
    void onError(Exception exception);
}

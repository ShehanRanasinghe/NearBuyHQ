package com.example.nearbuyhq.data.repository;

public interface OperationCallback {
    void onSuccess();
    void onError(Exception exception);
}


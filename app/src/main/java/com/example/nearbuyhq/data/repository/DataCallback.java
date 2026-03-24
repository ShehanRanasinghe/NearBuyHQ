package com.example.nearbuyhq.data.repository;

public interface DataCallback<T> {
    void onSuccess(T data);
    void onError(Exception exception);
}


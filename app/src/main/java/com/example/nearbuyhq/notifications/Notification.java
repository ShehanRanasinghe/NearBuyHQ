package com.example.nearbuyhq.notifications;

public class Notification {
    private String title;
    private String message;
    private String time;

    public Notification(String title, String message, String time) {
        this.title = title;
        this.message = message;
        this.time = time;
    }

    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
}


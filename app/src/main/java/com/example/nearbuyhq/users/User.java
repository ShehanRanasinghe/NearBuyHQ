package com.example.nearbuyhq.users;

public class User {
    private String id;
    private String name;
    private String email;
    private String status;

    public User(String id, String name, String email, String status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.status = status;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}


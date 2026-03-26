package com.example.nearbuyhq.users;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String id;
    private String name;
    private String email;
    private String username;
    private String phone;      // shop owner's phone number
    private String status;
    private long createdAt;
    private long updatedAt;

    // Short constructor – used where phone is not yet known
    public User(String id, String name, String email, String status) {
        this(id, name, email, "", "", status, System.currentTimeMillis(), System.currentTimeMillis());
    }

    // Medium constructor – backward compat (no phone)
    public User(String id, String name, String email, String username, String status, long createdAt, long updatedAt) {
        this(id, name, email, username, "", status, createdAt, updatedAt);
    }

    // Full constructor with phone field
    public User(String id, String name, String email, String username, String phone,
                String status, long createdAt, long updatedAt) {
        this.id        = id;
        this.name      = name;
        this.email     = email;
        this.username  = username;
        this.phone     = phone;
        this.status    = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId()        { return id; }
    public String getName()      { return name; }
    public String getEmail()     { return email; }
    public String getUsername()  { return username; }
    public String getPhone()     { return phone; }    // used by ProfilePage
    public String getStatus()    { return status; }
    public long   getCreatedAt() { return createdAt; }
    public long   getUpdatedAt() { return updatedAt; }

    public void setStatus(String status) {
        this.status    = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public void setPhone(String phone) { this.phone = phone; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name",      name);
        map.put("email",     email);
        map.put("username",  username);
        map.put("phone",     phone);     // stored so ProfilePage can display it
        map.put("status",    status);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }

    public static User fromMap(String id, Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        return new User(
                id,
                stringValue(map.get("name")),
                stringValue(map.get("email")),
                stringValue(map.get("username")),
                stringValue(map.get("phone")),   // read phone from Firestore doc
                defaultIfEmpty(stringValue(map.get("status")), "Active"),
                longValue(map.get("createdAt")),
                longValue(map.get("updatedAt"))
        );
    }

    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static long longValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException ignored) {
                return 0L;
            }
        }
        return 0L;
    }

    private static String defaultIfEmpty(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }
}


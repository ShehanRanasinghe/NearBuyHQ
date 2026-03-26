package com.example.nearbuyhq.data.model;

import java.util.HashMap;
import java.util.Map;

public class User {
    // ── User fields ───────────────────────────────────────────────────────
    private String id;
    private String name;
    private String email;
    private String username;
    private String phone;
    private String status;
    private long createdAt;
    private long updatedAt;

    // ── Shop fields (stored in the same users/{uid} document) ─────────────
    private String shopName;
    private String shopLocation;
    private String openingHours;
    private double latitude;
    private double longitude;

    // Short constructor
    public User(String id, String name, String email, String status) {
        this(id, name, email, "", "", status, System.currentTimeMillis(), System.currentTimeMillis());
    }

    // Medium constructor – backward compat (no phone)
    public User(String id, String name, String email, String username, String status, long createdAt, long updatedAt) {
        this(id, name, email, username, "", status, createdAt, updatedAt);
    }

    // Full constructor
    public User(String id, String name, String email, String username, String phone,
                String status, long createdAt, long updatedAt) {
        this.id           = id;
        this.name         = name;
        this.email        = email;
        this.username     = username;
        this.phone        = phone;
        this.status       = status;
        this.createdAt    = createdAt;
        this.updatedAt    = updatedAt;
        // Shop fields default to empty/zero
        this.shopName     = "";
        this.shopLocation = "";
        this.openingHours = "";
        this.latitude     = 0.0;
        this.longitude    = 0.0;
    }

    // ── User getters ──────────────────────────────────────────────────────
    public String getId()        { return id; }
    public String getName()      { return name; }
    public String getEmail()     { return email; }
    public String getUsername()  { return username; }
    public String getPhone()     { return phone; }
    public String getStatus()    { return status; }
    public long   getCreatedAt() { return createdAt; }
    public long   getUpdatedAt() { return updatedAt; }

    // ── Shop getters ──────────────────────────────────────────────────────
    public String getShopName()     { return shopName     != null ? shopName     : ""; }
    public String getShopLocation() { return shopLocation != null ? shopLocation : ""; }
    public String getOpeningHours() { return openingHours != null ? openingHours : ""; }
    public double getLatitude()     { return latitude; }
    public double getLongitude()    { return longitude; }
    public boolean hasLocation()    { return latitude != 0.0 || longitude != 0.0; }

    // ── Setters ───────────────────────────────────────────────────────────
    public void setStatus(String status) {
        this.status    = status;
        this.updatedAt = System.currentTimeMillis();
    }
    public void setPhone(String phone)             { this.phone        = phone; }
    public void setShopName(String shopName)       { this.shopName     = shopName; }
    public void setShopLocation(String loc)        { this.shopLocation = loc; }
    public void setOpeningHours(String hours)      { this.openingHours = hours; }
    public void setLatitude(double lat)            { this.latitude     = lat; }
    public void setLongitude(double lng)           { this.longitude    = lng; }

    // ── Firestore serialisation ───────────────────────────────────────────
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name",         name);
        map.put("email",        email);
        map.put("username",     username);
        map.put("phone",        phone);
        map.put("status",       status);
        map.put("shopName",     shopName     != null ? shopName     : "");
        map.put("shopLocation", shopLocation != null ? shopLocation : "");
        map.put("openingHours", openingHours != null ? openingHours : "");
        map.put("latitude",     latitude);
        map.put("longitude",    longitude);
        map.put("createdAt",    createdAt);
        map.put("updatedAt",    updatedAt);
        return map;
    }

    public static User fromMap(String id, Map<String, Object> map) {
        if (map == null) return null;
        User user = new User(
                id,
                stringValue(map.get("name")),
                stringValue(map.get("email")),
                stringValue(map.get("username")),
                stringValue(map.get("phone")),
                defaultIfEmpty(stringValue(map.get("status")), "Active"),
                longValue(map.get("createdAt")),
                longValue(map.get("updatedAt"))
        );
        user.shopName     = stringValue(map.get("shopName"));
        user.shopLocation = stringValue(map.get("shopLocation"));
        user.openingHours = stringValue(map.get("openingHours"));
        user.latitude     = doubleValue(map.get("latitude"));
        user.longitude    = doubleValue(map.get("longitude"));
        return user;
    }

    // ── Private helpers ───────────────────────────────────────────────────
    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static long longValue(Object value) {
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof String) {
            try { return Long.parseLong((String) value); } catch (NumberFormatException ignored) {}
        }
        return 0L;
    }

    private static double doubleValue(Object value) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String) {
            try { return Double.parseDouble((String) value); } catch (NumberFormatException ignored) {}
        }
        return 0.0;
    }

    private static String defaultIfEmpty(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }
}

package com.example.nearbuyhq.shops;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a shop / branch owned by a registered shop admin.
 *
 * IMPORTANT fields for the customer app:
 *  - latitude / longitude  → used to calculate distance from customer to this shop
 *  - status                → "Active" shops are shown to customers
 *  - ownerUid              → links this shop to the Firebase Auth user who registered it
 */
public class Shop {
    private String id;
    private String name;
    private String owner;      // display name of the owner
    private String ownerUid;   // Firebase Auth UID – links shop to its owner account
    private String location;   // human-readable address
    private String category;
    private String status;     // "Active" | "Inactive"
    private String contact;
    private String openingHours;
    private String website;
    private double latitude;   // GPS latitude  (needed by customer app for distance)
    private double longitude;  // GPS longitude (needed by customer app for distance)
    private long createdAt;
    private long updatedAt;

    // ── Compact constructor (backward compat) ────────────────────────────
    public Shop(String id, String name, String owner, String location, String category, String status) {
        this(id, name, owner, location, category, status, "", System.currentTimeMillis(), System.currentTimeMillis());
    }

    // ── Full constructor without lat/lng ─────────────────────────────────
    public Shop(String id, String name, String owner, String location, String category, String status,
                String contact, long createdAt, long updatedAt) {
        this(id, name, owner, "", location, category, status, contact,
                "", "", 0.0, 0.0, createdAt, updatedAt);
    }

    // ── Full constructor with all fields ─────────────────────────────────
    public Shop(String id, String name, String owner, String ownerUid,
                String location, String category, String status, String contact,
                String openingHours, String website,
                double latitude, double longitude,
                long createdAt, long updatedAt) {
        this.id           = id;
        this.name         = name;
        this.owner        = owner;
        this.ownerUid     = ownerUid;
        this.location     = location;
        this.category     = category;
        this.status       = status;
        this.contact      = contact;
        this.openingHours = openingHours;
        this.website      = website;
        this.latitude     = latitude;
        this.longitude    = longitude;
        this.createdAt    = createdAt;
        this.updatedAt    = updatedAt;
    }

    // ── Getters ──────────────────────────────────────────────────────────
    public String getId()           { return id; }
    public String getName()         { return name; }
    public String getOwner()        { return owner; }
    public String getOwnerUid()     { return ownerUid; }
    public String getLocation()     { return location; }
    public String getCategory()     { return category; }
    public String getStatus()       { return status; }
    public String getContact()      { return contact; }
    public String getOpeningHours() { return openingHours; }
    public String getWebsite()      { return website; }
    public double getLatitude()     { return latitude; }
    public double getLongitude()    { return longitude; }
    public long   getCreatedAt()    { return createdAt; }
    public long   getUpdatedAt()    { return updatedAt; }

    // ── Setters ──────────────────────────────────────────────────────────
    public void setId(String id)               { this.id = id; }
    public void setLatitude(double latitude)   { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setOwnerUid(String ownerUid)   { this.ownerUid = ownerUid; }
    public void setOpeningHours(String h)      { this.openingHours = h; }
    public void setWebsite(String w)           { this.website = w; }
    public void setName(String name)           { this.name = name; }
    public void setContact(String contact)     { this.contact = contact; }
    public void setLocation(String location)   { this.location = location; }
    public void setCategory(String category)   { this.category = category; }

    public void setStatus(String status) {
        this.status    = status;
        this.updatedAt = System.currentTimeMillis();
    }

    // ── Validation ───────────────────────────────────────────────────────
    public boolean isValidForSave() {
        return !isBlank(name) && !isBlank(owner) && !isBlank(location)
                && !isBlank(category) && !isBlank(contact);
    }

    /** Returns true if GPS coordinates have been captured for this shop. */
    public boolean hasLocation() {
        return latitude != 0.0 && longitude != 0.0;
    }

    // ── Serialisation ────────────────────────────────────────────────────
    /**
     * Convert to a Firestore-compatible map.
     * The customer app reads these fields to display shop info and calculate distance.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name",         name);
        map.put("owner",        owner);
        map.put("ownerUid",     ownerUid);          // link to Firebase Auth user
        map.put("location",     location);
        map.put("address",      location);           // alias for customer app
        map.put("locationText", location);           // alias for customer app
        map.put("category",     category);
        map.put("status",       status);
        map.put("contact",      contact);
        map.put("contactNumber",contact);            // alias for customer app
        map.put("openingHours", openingHours);
        map.put("website",      website);
        map.put("latitude",     latitude);           // GPS latitude  – used by customer app
        map.put("longitude",    longitude);          // GPS longitude – used by customer app
        map.put("createdAt",    createdAt);
        map.put("updatedAt",    updatedAt);
        return map;
    }

    public static Shop fromMap(String id, Map<String, Object> map) {
        if (map == null) return null;

        // Support both "location" and "address" field names
        String location = stringValue(map.get("location"));
        if (location.isEmpty()) location = stringValue(map.get("address"));

        Shop shop = new Shop(
                id,
                stringValue(map.get("name")),
                stringValue(map.get("owner")),
                stringValue(map.get("ownerUid")),
                location,
                stringValue(map.get("category")),
                defaultIfBlank(stringValue(map.get("status")), "Active"),
                firstNonBlank(stringValue(map.get("contact")), stringValue(map.get("contactNumber"))),
                stringValue(map.get("openingHours")),
                stringValue(map.get("website")),
                doubleValue(map.get("latitude")),
                doubleValue(map.get("longitude")),
                longValue(map.get("createdAt")),
                longValue(map.get("updatedAt"))
        );

        if (shop.getCreatedAt() == 0L) shop.createdAt = System.currentTimeMillis();
        if (shop.getUpdatedAt() == 0L) shop.updatedAt = shop.getCreatedAt();
        return shop;
    }

    // ── Private helpers ──────────────────────────────────────────────────
    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static long longValue(Object value) {
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof String) {
            try { return Long.parseLong((String) value); }
            catch (NumberFormatException ignored) { return 0L; }
        }
        return 0L;
    }

    private static double doubleValue(Object value) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String) {
            try { return Double.parseDouble((String) value); }
            catch (NumberFormatException ignored) { return 0.0; }
        }
        return 0.0;
    }

    private static String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private static String firstNonBlank(String first, String second) {
        return !isBlank(first) ? first : second;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

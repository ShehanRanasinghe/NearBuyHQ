package com.example.nearbuyhq.discounts;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Promotion {

    public static final String TYPE_RAMADAN    = "Ramadan";
    public static final String TYPE_CHRISTMAS  = "Christmas";
    public static final String TYPE_NEW_YEAR   = "New Year";
    public static final String TYPE_THAI_PONGAL = "Thai Pongal";
    public static final String TYPE_CUSTOM     = "Custom";

    private String id;
    private String userId;  // ← owner's userId (== shopId)
    private String title;
    private String type;
    private int    discountPercentage;
    private String startDate;       // "YYYY-MM-DD"
    private String endDate;         // "YYYY-MM-DD"
    private String productName;
    private double originalPrice;
    private boolean isActive;
    private long createdAt;
    private long updatedAt;

    public Promotion() {}

    public Promotion(String id, String title, String type, int discountPercentage,
                     String startDate, String endDate,
                     String productName, double originalPrice, boolean isActive) {
        this(id, title, type, discountPercentage, startDate, endDate, productName, originalPrice,
                isActive, System.currentTimeMillis(), System.currentTimeMillis());
    }

    public Promotion(String id, String title, String type, int discountPercentage,
                     String startDate, String endDate,
                     String productName, double originalPrice, boolean isActive,
                     long createdAt, long updatedAt) {
        this.id                 = id;
        this.title              = title;
        this.type               = type;
        this.discountPercentage = discountPercentage;
        this.startDate          = startDate;
        this.endDate            = endDate;
        this.productName        = productName;
        this.originalPrice      = originalPrice;
        this.isActive           = isActive;
        this.createdAt          = createdAt;
        this.updatedAt          = updatedAt;
    }

    // ── JSON serialisation ──────────────────────────────────────────────────

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id",                 id);
        obj.put("title",              title);
        obj.put("type",               type);
        obj.put("discountPercentage", discountPercentage);
        obj.put("startDate",          startDate);
        obj.put("endDate",            endDate);
        obj.put("productName",        productName);
        obj.put("originalPrice",      originalPrice);
        obj.put("isActive",           isActive);
        return obj;
    }

    public static Promotion fromJson(JSONObject obj) throws JSONException {
        return new Promotion(
                obj.getString("id"),
                obj.getString("title"),
                obj.getString("type"),
                obj.getInt("discountPercentage"),
                obj.getString("startDate"),
                obj.getString("endDate"),
                obj.optString("productName", ""),
                obj.optDouble("originalPrice", 0.0),
                obj.optBoolean("isActive", true),
                obj.optLong("createdAt", System.currentTimeMillis()),
                obj.optLong("updatedAt", System.currentTimeMillis())
        );
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId",             userId != null ? userId : "");
        map.put("title",              title);
        map.put("type",               type);
        map.put("discountPercentage", discountPercentage);
        map.put("startDate",          startDate);
        map.put("endDate",            endDate);
        map.put("productName",        productName);
        map.put("originalPrice",      originalPrice);
        map.put("isActive",           isActive);
        map.put("createdAt",          createdAt);
        map.put("updatedAt",          updatedAt);
        return map;
    }

    public static Promotion fromMap(String id, Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        Promotion p = new Promotion(
                id,
                value(map.get("title")),
                value(map.get("type")),
                intValue(map.get("discountPercentage")),
                value(map.get("startDate")),
                value(map.get("endDate")),
                value(map.get("productName")),
                doubleValue(map.get("originalPrice")),
                booleanValue(map.get("isActive"), true),
                longValue(map.get("createdAt")),
                longValue(map.get("updatedAt"))
        );
        p.userId = value(map.get("userId"));
        return p;
    }

    // ── Derived helpers ─────────────────────────────────────────────────────

    public double getDiscountedPrice() {
        return originalPrice - (originalPrice * discountPercentage / 100.0);
    }

    /** Returns a seasonal emoji for the promotion type. */
    public String getTypeEmoji() {
        switch (type) {
            case TYPE_RAMADAN:     return "\uD83C\uDF19";   // 🌙
            case TYPE_CHRISTMAS:   return "\uD83C\uDF84";   // 🎄
            case TYPE_NEW_YEAR:    return "\uD83C\uDF86";   // 🎆
            case TYPE_THAI_PONGAL: return "\uD83E\uDEF4";  // 🪴  (pot plant – closest to Pongal)
            default:               return "\uD83C\uDFF7";   // 🏷️
        }
    }

    // ── Getters / Setters ───────────────────────────────────────────────────

    public String getId()                       { return id; }
    public void   setId(String id)              { this.id = id; }

    public String getTitle()                    { return title; }
    public void   setTitle(String title)        { this.title = title; }

    public String getType()                     { return type; }
    public void   setType(String type)          { this.type = type; }

    public int  getDiscountPercentage()         { return discountPercentage; }
    public void setDiscountPercentage(int d)    { this.discountPercentage = d; }

    public String getStartDate()                { return startDate; }
    public void   setStartDate(String d)        { this.startDate = d; }

    public String getEndDate()                  { return endDate; }
    public void   setEndDate(String d)          { this.endDate = d; }

    public String getProductName()              { return productName; }
    public void   setProductName(String p)      { this.productName = p; }

    public double getOriginalPrice()            { return originalPrice; }
    public void   setOriginalPrice(double p)    { this.originalPrice = p; }

    public boolean isActive()                   { return isActive; }
    public void    setActive(boolean active)    { this.isActive = active; }

    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }

    public String getUserId()              { return userId != null ? userId : ""; }
    public void   setUserId(String userId) { this.userId = userId; }

    private static String value(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static int intValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private static double doubleValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException ignored) {
                return 0.0;
            }
        }
        return 0.0;
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

    private static boolean booleanValue(Object value, boolean fallback) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return fallback;
    }
}

package com.example.nearbuyhq;

import org.json.JSONException;
import org.json.JSONObject;

public class Promotion {

    public static final String TYPE_RAMADAN    = "Ramadan";
    public static final String TYPE_CHRISTMAS  = "Christmas";
    public static final String TYPE_NEW_YEAR   = "New Year";
    public static final String TYPE_THAI_PONGAL = "Thai Pongal";
    public static final String TYPE_CUSTOM     = "Custom";

    private String id;
    private String title;
    private String type;
    private int    discountPercentage;
    private String startDate;       // "YYYY-MM-DD"
    private String endDate;         // "YYYY-MM-DD"
    private String productName;
    private double originalPrice;
    private boolean isActive;

    public Promotion() {}

    public Promotion(String id, String title, String type, int discountPercentage,
                     String startDate, String endDate,
                     String productName, double originalPrice, boolean isActive) {
        this.id                 = id;
        this.title              = title;
        this.type               = type;
        this.discountPercentage = discountPercentage;
        this.startDate          = startDate;
        this.endDate            = endDate;
        this.productName        = productName;
        this.originalPrice      = originalPrice;
        this.isActive           = isActive;
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
                obj.optBoolean("isActive", true)
        );
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
}

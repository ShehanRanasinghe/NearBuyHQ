package com.example.nearbuyhq.products;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Product data model.
 * Added: imageUrl field — stores the Supabase public URL for the product image.
 * This maps to the "image_url" column in your Supabase products table.
 */
public class ProductItem {

    private String id;
    private String shopId;
    private String name;
    private String description;
    private String category;
    private String unit;
    private double price;
    private int    quantity;
    private String status;
    private String imageUrl;   // ← NEW: Supabase public URL
    private long   createdAt;
    private long   updatedAt;

    // ── Constructor ───────────────────────────────────────────────────────────
    public ProductItem(String id, String shopId, String name, String description,
                       String category, String unit, double price, int quantity,
                       String status, String imageUrl,
                       long createdAt, long updatedAt) {
        this.id          = id;
        this.shopId      = shopId;
        this.name        = name;
        this.description = description;
        this.category    = category;
        this.unit        = unit;
        this.price       = price;
        this.quantity    = quantity;
        this.status      = status;
        this.imageUrl    = imageUrl;
        this.createdAt   = createdAt;
        this.updatedAt   = updatedAt;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String getId()          { return id; }
    public String getShopId()      { return shopId; }
    public String getName()        { return name; }
    public String getDescription() { return description; }
    public String getCategory()    { return category; }
    public String getUnit()        { return unit; }
    public double getPrice()       { return price; }
    public int    getQuantity()    { return quantity; }
    public String getStatus()      { return status; }
    public String getImageUrl()    { return imageUrl; }  // ← NEW
    public long   getCreatedAt()   { return createdAt; }
    public long   getUpdatedAt()   { return updatedAt; }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setId(String id)             { this.id        = id; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public void setImageUrl(String url)      { this.imageUrl  = url; }  // ← NEW

    // ── Utility ───────────────────────────────────────────────────────────────
    public boolean isOutOfStock()            { return quantity <= 0; }
    public boolean isLowStock(int threshold) { return quantity > 0 && quantity <= threshold; }

    public boolean isValidForSave() {
        return !isBlank(name) && !isBlank(category)
                && !isBlank(unit) && price >= 0d && quantity >= 0;
    }

    /**
     * Converts ProductItem to a Map for Firebase storage.
     * image_url is included so it stays in sync.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("shopId",        shopId);
        map.put("name",          name);
        map.put("itemName",      name);          // legacy alias
        map.put("description",   description);
        map.put("itemDetails",   description);   // legacy alias
        map.put("category",      category);
        map.put("unit",          unit);
        map.put("price",         price);
        map.put("quantity",      quantity);
        map.put("stockQuantity", quantity);      // legacy alias
        map.put("status",        status);
        map.put("imageUrl",      imageUrl != null ? imageUrl : "");  // ← NEW
        map.put("createdAt",     createdAt);
        map.put("updatedAt",     updatedAt);
        return map;
    }

    public String formattedPrice() {
        return String.format(Locale.US, "%.2f", price);
    }

    /**
     * Rebuilds a ProductItem from a Firebase document map.
     * Reads imageUrl from the "imageUrl" key.
     */
    public static ProductItem fromMap(String id, Map<String, Object> map) {
        if (map == null) return null;

        // Handle legacy description key
        String description = stringValue(map.get("description"));
        if (description.isEmpty()) {
            description = stringValue(map.get("itemDetails"));
        }

        // Handle legacy quantity key
        int quantity = intValue(map.get("quantity"));
        if (quantity == 0 && map.get("stockQuantity") != null) {
            quantity = intValue(map.get("stockQuantity"));
        }

        ProductItem item = new ProductItem(
                id,
                defaultIfBlank(stringValue(map.get("shopId")), "global"),
                firstNonBlank(stringValue(map.get("name")), stringValue(map.get("itemName"))),
                description,
                defaultIfBlank(stringValue(map.get("category")), "General"),
                defaultIfBlank(stringValue(map.get("unit")), "unit"),
                doubleValue(map.get("price")),
                quantity,
                defaultIfBlank(stringValue(map.get("status")), resolveStatus(quantity)),
                stringValue(map.get("imageUrl")),   // ← NEW: reads imageUrl from Firebase doc
                longValue(map.get("createdAt")),
                longValue(map.get("updatedAt"))
        );

        if (item.createdAt == 0L) item.createdAt = System.currentTimeMillis();
        if (item.updatedAt == 0L) item.updatedAt = item.createdAt;
        return item;
    }

    public static String resolveStatus(int quantity) {
        if (quantity <= 0)   return "Out of Stock";
        if (quantity <= 10)  return "Low Stock";
        return "Available";
    }

    // ── Private type-safe helpers ─────────────────────────────────────────────
    private static int intValue(Object v) {
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof String) {
            try { return Integer.parseInt((String) v); }
            catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    private static double doubleValue(Object v) {
        if (v instanceof Number) return ((Number) v).doubleValue();
        if (v instanceof String) {
            try { return Double.parseDouble((String) v); }
            catch (NumberFormatException ignored) {}
        }
        return 0d;
    }

    private static long longValue(Object v) {
        if (v instanceof Number) return ((Number) v).longValue();
        if (v instanceof String) {
            try { return Long.parseLong((String) v); }
            catch (NumberFormatException ignored) {}
        }
        return 0L;
    }

    private static String stringValue(Object v) {
        return v == null ? "" : String.valueOf(v).trim();
    }

    private static String firstNonBlank(String a, String b) {
        return !isBlank(a) ? a : b;
    }

    private static String defaultIfBlank(String v, String fallback) {
        return isBlank(v) ? fallback : v;
    }

    private static boolean isBlank(String v) {
        return v == null || v.trim().isEmpty();
    }
}
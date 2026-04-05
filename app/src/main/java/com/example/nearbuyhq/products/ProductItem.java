package com.example.nearbuyhq.products;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// Product data model – represents a single inventory item stored in NearBuyHQ/{shopId}/products.
public class ProductItem {

    private String id;
    private String shopId;
    private String name;
    private String description;
    private String category;
    private String unit;
    private double price;
    private int quantity;
    private String status;
    private String expiryDate;
    private long createdAt;
    private long updatedAt;

    public ProductItem(String id, String shopId, String name, String description, String category,
                       String unit, double price, int quantity, String status, String expiryDate, long createdAt, long updatedAt) {
        this.id = id;
        this.shopId = shopId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.unit = unit;
        this.price = price;
        this.quantity = quantity;
        this.status = status;
        this.expiryDate = expiryDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public String getShopId() { return shopId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getUnit() { return unit; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getStatus() { return status; }
    public String getExpiryDate() { return expiryDate; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }

    public void setId(String id) { this.id = id; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public boolean isOutOfStock() {
        return quantity <= 0;
    }

    public boolean isLowStock(int threshold) {
        return quantity > 0 && quantity <= threshold;
    }

    public boolean isValidForSave() {
        return !isBlank(name) && !isBlank(category) && !isBlank(unit) && price >= 0d && quantity >= 0;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("shopId", shopId);
        map.put("name", name);
        map.put("itemName", name);
        map.put("description", description);
        map.put("itemDetails", description);
        map.put("category", category);
        map.put("unit", unit);
        map.put("price", price);
        map.put("quantity", quantity);
        map.put("stockQuantity", quantity);
        map.put("status", status);
        map.put("expiryDate", defaultIfBlank(expiryDate, ""));
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }

    public String formattedPrice() {
        return String.format(Locale.US, "%.2f", price);
    }

    public static ProductItem fromMap(String id, Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        String description = stringValue(map.get("description"));
        if (description.isEmpty()) {
            description = stringValue(map.get("itemDetails"));
        }

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
                stringValue(map.get("expiryDate")),
                longValue(map.get("createdAt")),
                longValue(map.get("updatedAt"))
        );

        if (item.createdAt == 0L) {
            item.createdAt = System.currentTimeMillis();
        }
        if (item.updatedAt == 0L) {
            item.updatedAt = item.createdAt;
        }

        return item;
    }

    public static String resolveStatus(int quantity) {
        if (quantity <= 0) {
            return "Out of Stock";
        }
        return quantity <= 10 ? "Low Stock" : "Available";
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
                return 0d;
            }
        }
        return 0d;
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

    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static String firstNonBlank(String first, String second) {
        return !isBlank(first) ? first : second;
    }

    private static String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}


package com.example.nearbuyhq.discounts;

import java.util.HashMap;
import java.util.Map;

public class Deal {
    private String id;
    private String title;
    private String shopName;
    private String discount;
    private String description;
    private String validity;
    private long createdAt;
    private long updatedAt;

    public Deal(String id, String title, String shopName, String discount, String validity) {
        this(id, title, shopName, discount, "", validity, System.currentTimeMillis(), System.currentTimeMillis());
    }

    public Deal(String id, String title, String shopName, String discount, String description,
                String validity, long createdAt, long updatedAt) {
        this.id = id;
        this.title = title;
        this.shopName = shopName;
        this.discount = discount;
        this.description = description;
        this.validity = validity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getShopName() { return shopName; }
    public String getDiscount() { return discount; }
    public String getDescription() { return description; }
    public String getValidity() { return validity; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("shopName", shopName);
        map.put("discount", discount);
        map.put("description", description);
        map.put("validity", validity);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }

    public static Deal fromMap(String id, Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        return new Deal(
                id,
                value(map.get("title")),
                value(map.get("shopName")),
                value(map.get("discount")),
                value(map.get("description")),
                value(map.get("validity")),
                longValue(map.get("createdAt")),
                longValue(map.get("updatedAt"))
        );
    }

    private static String value(Object object) {
        return object == null ? "" : String.valueOf(object).trim();
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
}


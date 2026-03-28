package com.example.nearbuyhq.orders;

import java.util.HashMap;
import java.util.Map;

// Order data model – represents a single customer order stored in NearBuyHQ/{shopId}/orders.
public class Order {
    private String orderId, customerName, status, orderDate;
    private String customerPhone;
    private String customerAddress;
    private String shopId;
    private double orderTotal;
    private long createdAt;
    private long updatedAt;

    // Short constructor used when only the key order fields are available (e.g. from UI intent)
    public Order(String orderId, String customerName, String status,
                 double orderTotal, String orderDate) {
        this(orderId, customerName, status, orderTotal, orderDate, "", "", System.currentTimeMillis(), System.currentTimeMillis());
    }

    // Full constructor used when loading from Firestore
    public Order(String orderId, String customerName, String status,
                 double orderTotal, String orderDate, String customerPhone,
                 String customerAddress, long createdAt, long updatedAt) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.status = status;
        this.orderTotal = orderTotal;
        this.orderDate = orderDate;
        this.customerPhone = customerPhone;
        this.customerAddress = customerAddress;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ── Getters ───────────────────────────────────────────────────────────

    public String getOrderId()         { return orderId; }
    public String getCustomerName()    { return customerName; }
    public String getStatus()          { return status; }
    public double getOrderTotal()      { return orderTotal; }
    public String getOrderDate()       { return orderDate; }
    public String getCustomerPhone()   { return customerPhone; }
    public String getCustomerAddress() { return customerAddress; }
    public long   getCreatedAt()       { return createdAt; }
    public long   getUpdatedAt()       { return updatedAt; }
    public String getShopId()          { return shopId == null ? "" : shopId; }
    public void   setShopId(String shopId) { this.shopId = shopId; }

    // ── Status update ─────────────────────────────────────────────────────

    // Update the status and automatically refresh the updatedAt timestamp
    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    // ── Firestore serialisation ───────────────────────────────────────────

    // Serialize order fields to a Map for writing to Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("customerName",    customerName);
        map.put("status",          status);
        map.put("orderTotal",      orderTotal);
        map.put("total",           orderTotal);
        map.put("orderDate",       orderDate);
        map.put("customerPhone",   customerPhone);
        map.put("customerAddress", customerAddress);
        map.put("shopId",          shopId == null ? "" : shopId);
        map.put("createdAt",       createdAt);
        map.put("updatedAt",       updatedAt);
        return map;
    }

    // Deserialize a Firestore document snapshot into an Order object
    public static Order fromMap(String id, Map<String, Object> map) {
        if (map == null) return null;
        Order o = new Order(
                id,
                value(map.get("customerName")),
                defaultIfEmpty(value(map.get("status")), "Pending"),
                doubleValue(map.get("orderTotal"), map.get("total")),
                value(map.get("orderDate")),
                value(map.get("customerPhone")),
                value(map.get("customerAddress")),
                longValue(map.get("createdAt")),
                longValue(map.get("updatedAt"))
        );
        o.setShopId(value(map.get("shopId")));
        return o;
    }

    private static String value(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    private static String defaultIfEmpty(String value, String fallback) {
        return value.isEmpty() ? fallback : value;
    }

    private static double doubleValue(Object primary, Object secondary) {
        if (primary instanceof Number) {
            return ((Number) primary).doubleValue();
        }
        if (secondary instanceof Number) {
            return ((Number) secondary).doubleValue();
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
}
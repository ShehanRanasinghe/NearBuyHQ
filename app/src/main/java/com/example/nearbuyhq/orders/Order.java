package com.example.nearbuyhq.orders;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    private String itemsSummary;   // e.g. "Apples x1" – used when no items array exists
    // Items list (product name + quantity rows) populated from Firestore
    private List<Map<String, Object>> items = new ArrayList<>();

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
    public List<Map<String, Object>> getItems() { return items; }
    public String getItemsSummary()    { return itemsSummary == null ? "" : itemsSummary; }

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

    // Deserialize a Firestore document snapshot into an Order object.
    // Supports both camelCase (admin app) and alternative field names (customer app).
    public static Order fromMap(String id, Map<String, Object> map) {
        if (map == null) return null;

        // Support many naming conventions for the order total.
        // totalAmountRaw is the clean numeric value; totalAmount may be a string like "Rs.340"
        double total = doubleValue(
                map.get("totalAmountRaw"), map.get("totalAmount"),
                map.get("orderTotal"),     map.get("total"),
                map.get("amount"),         map.get("grandTotal"),
                map.get("subtotal"),       map.get("price"));

        // Support multiple naming conventions for date
        String date = firstNonEmpty(
                value(map.get("orderDate")),
                value(map.get("order_date")),
                value(map.get("date"))
        );
        if (date.isEmpty()) {
            long ts = longValue(map.get("createdAt"), map.get("created_at"), map.get("timestamp"));
            if (ts > 0) {
                date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(ts));
            }
        }

        String customerName = firstNonEmpty(
                value(map.get("customerName")),
                value(map.get("customer_name")),
                value(map.get("name"))
        );

        String phone = firstNonEmpty(
                value(map.get("customerPhone")),
                value(map.get("customer_phone")),
                value(map.get("phone"))
        );

        String address = firstNonEmpty(
                value(map.get("customerAddress")),
                value(map.get("customer_address")),
                value(map.get("address"))
        );

        long createdAt = longValue(map.get("createdAt"), map.get("created_at"), map.get("timestamp"));
        long updatedAt = longValue(map.get("updatedAt"), map.get("updated_at"), map.get("timestamp"));

        Order o = new Order(
                id,
                customerName,
                defaultIfEmpty(value(map.get("status")), "Pending"),
                total,
                date,
                phone,
                address,
                createdAt,
                updatedAt
        );
        o.setShopId(firstNonEmpty(value(map.get("shopId")), value(map.get("shop_id"))));

        // Extract items list – try common field names used by customer apps
        Object rawItems = firstNonNullFrom(map, "items", "orderItems", "products", "cartItems", "lineItems");
        if (rawItems instanceof List) {
            for (Object item : (List<?>) rawItems) {
                if (item instanceof Map) {
                    //noinspection unchecked
                    o.items.add((Map<String, Object>) item);
                }
            }
        }

        // Fallback: build a synthetic item row from itemsSummary ("Apples x1") when no items array exists
        String summary = firstNonEmpty(
                value(map.get("itemsSummary")),
                value(map.get("items_summary")),
                value(map.get("orderSummary")));
        o.itemsSummary = summary;
        if (o.items.isEmpty() && !summary.isEmpty()) {
            // Parse "Product Name x{qty}" pattern
            Map<String, Object> syntheticItem = new HashMap<>();
            int xIdx = summary.lastIndexOf(" x");
            if (xIdx > 0) {
                syntheticItem.put("name",     summary.substring(0, xIdx).trim());
                syntheticItem.put("quantity", summary.substring(xIdx + 2).trim());
            } else {
                syntheticItem.put("name", summary);
            }
            if (total > 0) syntheticItem.put("price", total);
            o.items.add(syntheticItem);
        }
        return o;
    }

    private static String value(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    private static String defaultIfEmpty(String value, String fallback) {
        return value.isEmpty() ? fallback : value;
    }

    private static String firstNonEmpty(String... candidates) {
        for (String c : candidates) {
            if (c != null && !c.isEmpty()) return c;
        }
        return "";
    }

    private static Object firstNonNullFrom(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object val = map.get(key);
            if (val != null) return val;
        }
        return null;
    }

    // Accepts any number of candidate values; returns the first non-zero numeric result.
    // Also handles String values like "Rs.340" by stripping non-numeric characters.
    private static double doubleValue(Object... candidates) {
        for (Object v : candidates) {
            if (v instanceof Number) {
                double d = ((Number) v).doubleValue();
                if (d != 0) return d;
            }
            if (v instanceof String) {
                try {
                    // Strip currency symbols, letters, spaces – keep digits and dot
                    String s = ((String) v).replaceAll("[^0-9.]", "").trim();
                    if (!s.isEmpty()) {
                        double d = Double.parseDouble(s);
                        if (d != 0) return d;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return 0d;
    }

    private static long longValue(Object... candidates) {
        for (Object value : candidates) {
            if (value == null) continue;
            if (value instanceof Number) {
                long v = ((Number) value).longValue();
                if (v != 0) return v;
            }
            if (value instanceof Timestamp) {
                long v = ((Timestamp) value).toDate().getTime();
                if (v != 0) return v;
            }
            if (value instanceof String) {
                try {
                    long v = Long.parseLong((String) value);
                    if (v != 0) return v;
                } catch (NumberFormatException ignored) {}
            }
        }
        return 0L;
    }
}
package com.example.nearbuyhq.orders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.SessionManager;
import com.example.nearbuyhq.data.repository.DataCallback;
import com.example.nearbuyhq.data.repository.OperationCallback;
import com.example.nearbuyhq.data.repository.OrderRepository;

import java.util.List;
import java.util.Locale;
import java.util.Map;

// Order details screen – displays full order info and allows the owner to update the order status.
public class Order_details extends AppCompatActivity {

    private String orderId;
    private String selectedStatus = "Pending";
    private OrderRepository orderRepository;

    private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvHeaderStatus;
    private TextView tvCustomerName, tvCustomerPhone, tvCustomerAddress;
    private TextView tvTotalPrice, btnUpdateStatus;
    private LinearLayout containerItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        orderRepository = new OrderRepository();
        bindViews();
        bindIntentData();
        setupStatusButtons();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnUpdateStatus.setOnClickListener(v -> updateStatus());

        if (orderId != null && !orderId.isEmpty()) {
            loadOrder();
        }
    }

    // ── View binding ──────────────────────────────────────────────────────

    private void bindViews() {
        tvOrderId        = findViewById(R.id.tv_order_id);
        tvOrderDate      = findViewById(R.id.tv_order_date);
        tvOrderStatus    = findViewById(R.id.tv_order_status);
        tvHeaderStatus   = findViewById(R.id.tv_header_status);
        tvCustomerName   = findViewById(R.id.tv_customer_name);
        tvCustomerPhone  = findViewById(R.id.tv_customer_phone);
        tvCustomerAddress= findViewById(R.id.tv_customer_address);
        tvTotalPrice     = findViewById(R.id.tv_total_price);
        btnUpdateStatus  = findViewById(R.id.btn_update_status);
        containerItems   = findViewById(R.id.container_items);
    }

    // ── Intent data ───────────────────────────────────────────────────────

    private void bindIntentData() {
        orderId = getIntent().getStringExtra("order_id");
        String customerName = getIntent().getStringExtra("customer_name");
        String status       = getIntent().getStringExtra("status");
        double total        = getIntent().getDoubleExtra("total", 0d);
        String date         = getIntent().getStringExtra("date");

        if (status != null && !status.isEmpty()) selectedStatus = status;

        tvOrderId.setText(orderId == null ? "N/A" : "#ORD-" + orderId);
        tvCustomerName.setText(customerName == null || customerName.isEmpty() ? "—" : customerName);
        tvOrderDate.setText(date == null || date.isEmpty() ? "—" : date);
        tvTotalPrice.setText(total > 0
                ? String.format(Locale.getDefault(), "Rs. %.2f", total)
                : "—");
        tvCustomerPhone.setText("Loading…");
        tvCustomerAddress.setText("Loading…");
        addPlaceholderItemRow("Loading items…");
        applyStatusText();
    }

    // ── Status buttons ────────────────────────────────────────────────────

    private void setupStatusButtons() {
        findViewById(R.id.btn_pending).setOnClickListener(v -> {
            selectedStatus = "Pending"; applyStatusText(); });
        findViewById(R.id.btn_processing).setOnClickListener(v -> {
            selectedStatus = "Processing"; applyStatusText(); });
        findViewById(R.id.btn_delivered).setOnClickListener(v -> {
            selectedStatus = "Delivered"; applyStatusText(); });
    }

    private void applyStatusText() {
        tvOrderStatus.setText(selectedStatus);
        tvHeaderStatus.setText(selectedStatus);
    }

    // ── Firestore load ────────────────────────────────────────────────────

    private void loadOrder() {
        String userId = SessionManager.getInstance(this).getUserId();
        orderRepository.getOrder(orderId, userId, new DataCallback<Order>() {
            @Override
            public void onSuccess(Order data) {
                if (data == null) return;
                selectedStatus = data.getStatus();
                tvOrderId.setText("#ORD-" + data.getOrderId());
                tvCustomerName.setText(emptyOr(data.getCustomerName(), "—"));
                tvOrderDate.setText(emptyOr(data.getOrderDate(), "—"));
                tvTotalPrice.setText(data.getOrderTotal() > 0
                        ? String.format(Locale.getDefault(), "Rs. %.2f", data.getOrderTotal())
                        : "—");
                tvCustomerPhone.setText(emptyOr(data.getCustomerPhone(), "Not provided"));
                tvCustomerAddress.setText(emptyOr(data.getCustomerAddress(), "Not provided"));
                populateItems(data.getItems());
                applyStatusText();
            }

            @Override
            public void onError(Exception exception) {
                // Intent data already shown; just clear the loading placeholders
                tvCustomerPhone.setText("Not available");
                tvCustomerAddress.setText("Not available");
                containerItems.removeAllViews();
                addPlaceholderItemRow("Product details not available");
            }
        });
    }

    // ── Items container ───────────────────────────────────────────────────

    private void populateItems(List<Map<String, Object>> items) {
        containerItems.removeAllViews();
        if (items == null || items.isEmpty()) {
            addPlaceholderItemRow("No product details recorded for this order");
            return;
        }
        for (Map<String, Object> item : items) {
            String name = strFrom(item, "productName", "name", "itemName", "product");
            String qty  = strFrom(item, "quantity",    "qty",  "count",    "amount");
            double price = numFrom(item, "price", "itemPrice", "unitPrice", "amount");

            View row = LayoutInflater.from(this).inflate(R.layout.item_order_product_row, containerItems, false);
            ((TextView) row.findViewById(R.id.tv_product_name)).setText(name.isEmpty() ? "Unknown item" : name);
            String qtyLabel = qty.isEmpty() ? "" : "Qty: " + qty;
            ((TextView) row.findViewById(R.id.tv_product_qty)).setText(qtyLabel);
            ((TextView) row.findViewById(R.id.tv_product_price)).setText(price > 0
                    ? String.format(Locale.getDefault(), "Rs. %.2f", price) : "");
            containerItems.addView(row);
        }
    }

    private void addPlaceholderItemRow(String message) {
        containerItems.removeAllViews();
        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setTextColor(0xFF9AA0AC);
        tv.setTextSize(13f);
        containerItems.addView(tv);
    }

    // ── Status update ─────────────────────────────────────────────────────

    private void updateStatus() {
        if (orderId == null || orderId.trim().isEmpty()) {
            Toast.makeText(this, "Missing order ID", Toast.LENGTH_SHORT).show();
            return;
        }
        btnUpdateStatus.setEnabled(false);
        String userId = SessionManager.getInstance(this).getUserId();
        orderRepository.updateOrderStatus(orderId, userId, selectedStatus, new OperationCallback() {
            @Override public void onSuccess() {
                btnUpdateStatus.setEnabled(true);
                Toast.makeText(Order_details.this, "Status updated to " + selectedStatus, Toast.LENGTH_SHORT).show();
            }
            @Override public void onError(Exception e) {
                btnUpdateStatus.setEnabled(true);
                Toast.makeText(Order_details.this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private static String emptyOr(String s, String fallback) {
        return (s == null || s.trim().isEmpty()) ? fallback : s.trim();
    }

    private static String strFrom(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            Object v = map.get(k);
            if (v != null) { String s = String.valueOf(v).trim(); if (!s.isEmpty()) return s; }
        }
        return "";
    }

    private static double numFrom(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            Object v = map.get(k);
            if (v instanceof Number) { double d = ((Number) v).doubleValue(); if (d != 0) return d; }
        }
        return 0d;
    }
}
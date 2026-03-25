package com.example.nearbuyhq.orders;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.firebase.FirebaseConfig;
import com.example.nearbuyhq.data.repository.DataCallback;
import com.example.nearbuyhq.data.repository.OperationCallback;
import com.example.nearbuyhq.data.repository.OrderRepository;

import java.util.Locale;

public class Order_details extends AppCompatActivity {

    private String orderId;
    private String selectedStatus = "Pending";
    private OrderRepository orderRepository;
    private TextView tvOrderId;
    private TextView tvOrderDate;
    private TextView tvOrderStatus;
    private TextView tvHeaderStatus;
    private TextView tvCustomerName;
    private TextView tvCustomerPhone;
    private TextView tvCustomerAddress;
    private TextView tvTotalPrice;
    private TextView btnUpdateStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        orderRepository = new OrderRepository();
        bindViews();
        bindIntentData();
        setupStatusButtons();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnUpdateStatus.setOnClickListener(v -> updateStatus());

        if (FirebaseConfig.isFirebaseEnabled() && orderId != null && !orderId.isEmpty()) {
            loadOrder();
        }
    }

    private void bindViews() {
        tvOrderId = findViewById(R.id.tv_order_id);
        tvOrderDate = findViewById(R.id.tv_order_date);
        tvOrderStatus = findViewById(R.id.tv_order_status);
        tvHeaderStatus = findViewById(R.id.tv_header_status);
        tvCustomerName = findViewById(R.id.tv_customer_name);
        tvCustomerPhone = findViewById(R.id.tv_customer_phone);
        tvCustomerAddress = findViewById(R.id.tv_customer_address);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        btnUpdateStatus = findViewById(R.id.btn_update_status);
    }

    private void bindIntentData() {
        orderId = getIntent().getStringExtra("order_id");
        String customerName = getIntent().getStringExtra("customer_name");
        String status = getIntent().getStringExtra("status");
        double total = getIntent().getDoubleExtra("total", 0d);
        String date = getIntent().getStringExtra("date");

        if (status != null && !status.isEmpty()) {
            selectedStatus = status;
        }

        tvOrderId.setText(orderId == null ? "#ORD-N/A" : String.format(Locale.getDefault(), "#ORD-%s", orderId));
        tvCustomerName.setText(customerName == null ? "Customer" : customerName);
        tvOrderDate.setText(date == null || date.isEmpty() ? "N/A" : date);
        tvTotalPrice.setText(String.format(Locale.getDefault(), "$%.2f", total));
        tvCustomerPhone.setText("N/A");
        tvCustomerAddress.setText("N/A");
        applyStatusText();
    }

    private void setupStatusButtons() {
        findViewById(R.id.btn_pending).setOnClickListener(v -> {
            selectedStatus = "Pending";
            applyStatusText();
        });
        findViewById(R.id.btn_processing).setOnClickListener(v -> {
            selectedStatus = "Processing";
            applyStatusText();
        });
        findViewById(R.id.btn_delivered).setOnClickListener(v -> {
            selectedStatus = "Delivered";
            applyStatusText();
        });
    }

    private void applyStatusText() {
        tvOrderStatus.setText(selectedStatus);
        tvHeaderStatus.setText(selectedStatus);
    }

    private void loadOrder() {
        orderRepository.getOrder(orderId, new DataCallback<Order>() {
            @Override
            public void onSuccess(Order data) {
                if (data == null) {
                    return;
                }
                selectedStatus = data.getStatus();
                tvOrderId.setText(String.format(Locale.getDefault(), "#ORD-%s", data.getOrderId()));
                tvCustomerName.setText(data.getCustomerName());
                tvOrderDate.setText(data.getOrderDate());
                tvTotalPrice.setText(String.format(Locale.getDefault(), "$%.2f", data.getOrderTotal()));
                tvCustomerPhone.setText(data.getCustomerPhone().isEmpty() ? "N/A" : data.getCustomerPhone());
                tvCustomerAddress.setText(data.getCustomerAddress().isEmpty() ? "N/A" : data.getCustomerAddress());
                applyStatusText();
            }

            @Override
            public void onError(Exception exception) {
                Toast.makeText(Order_details.this, "Using local order data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatus() {
        if (orderId == null || orderId.trim().isEmpty()) {
            Toast.makeText(this, "Missing order ID", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!FirebaseConfig.isFirebaseEnabled()) {
            Toast.makeText(this, "Enable Firebase to update orders", Toast.LENGTH_SHORT).show();
            return;
        }

        btnUpdateStatus.setEnabled(false);
        orderRepository.updateOrderStatus(orderId, selectedStatus, new OperationCallback() {
            @Override
            public void onSuccess() {
                btnUpdateStatus.setEnabled(true);
                Toast.makeText(Order_details.this, "Order status updated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception exception) {
                btnUpdateStatus.setEnabled(true);
                Toast.makeText(Order_details.this, "Update failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
package com.example.nearbuyhq;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Order_List extends AppCompatActivity {

    private RecyclerView recyclerOrders;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set status bar color
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.deep_blue));

        setContentView(R.layout.activity_order_list);

        // Hide ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        btnBack = findViewById(R.id.btn_back);
        recyclerOrders = findViewById(R.id.recycler_orders);

        // Initialize sample data
        initSampleOrders();

        // Setup RecyclerView
        orderAdapter = new OrderAdapter(this, orderList);
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setAdapter(orderAdapter);

        // Set click listener for order items
        orderAdapter.setOnOrderClickListener(order -> {
            Intent intent = new Intent(Order_List.this, Order_details.class);
            intent.putExtra("order_id", order.getOrderId());
            intent.putExtra("customer_name", order.getCustomerName());
            intent.putExtra("status", order.getStatus());
            intent.putExtra("total", order.getOrderTotal());
            intent.putExtra("date", order.getOrderDate());
            startActivity(intent);
        });

        // Back button click listener
        btnBack.setOnClickListener(v -> finish());
    }

    private void initSampleOrders() {
        orderList = new ArrayList<>();
        orderList.add(new Order("1001", "John Smith", "Delivered", 45.99, "2024-03-05"));
        orderList.add(new Order("1002", "Sarah Johnson", "Processing", 78.50, "2024-03-06"));
        orderList.add(new Order("1003", "Mike Brown", "Pending", 32.25, "2024-03-06"));
        orderList.add(new Order("1004", "Emily Davis", "Delivered", 120.00, "2024-03-04"));
        orderList.add(new Order("1005", "David Wilson", "Pending", 56.75, "2024-03-06"));
        orderList.add(new Order("1006", "Lisa Anderson", "Processing", 89.99, "2024-03-05"));
        orderList.add(new Order("1007", "James Taylor", "Delivered", 65.40, "2024-03-03"));
        orderList.add(new Order("1008", "Maria Garcia", "Cancelled", 42.80, "2024-03-04"));
        orderList.add(new Order("1009", "Robert Martinez", "Pending", 95.60, "2024-03-06"));
        orderList.add(new Order("1010", "Jennifer Lee", "Processing", 38.25, "2024-03-05"));
        orderList.add(new Order("1011", "William White", "Delivered", 145.50, "2024-03-02"));
        orderList.add(new Order("1012", "Amanda Harris", "Pending", 73.90, "2024-03-06"));
    }
}
package com.example.nearbuyhq.orders;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.dashboard.Analytics;
import com.example.nearbuyhq.dashboard.Dashboard;
import com.example.nearbuyhq.products.Inventory;
import com.example.nearbuyhq.settings.ProfilePage;

import java.util.ArrayList;
import java.util.List;

public class Order_List extends AppCompatActivity {

    private RecyclerView recyclerOrders;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private ImageView btnBack;
    private LinearLayout navDashboard, navProducts, navOrders, navAnalytics, navProfile;
    private ImageView navDashboardIcon, navProductsIcon, navOrdersIcon, navAnalyticsIcon, navProfileIcon;
    private TextView navDashboardText, navProductsText, navOrdersText, navAnalyticsText, navProfileText;

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
        initBottomNavigationViews();

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

        setupBottomNavigation();
        resetNavSelection();
        setNavActive(navOrdersIcon, navOrdersText);
    }

    private void initBottomNavigationViews() {
        navDashboard = findViewById(R.id.navDashboard);
        navProducts = findViewById(R.id.navProducts);
        navOrders = findViewById(R.id.navOrders);
        navAnalytics = findViewById(R.id.navAnalytics);
        navProfile = findViewById(R.id.navProfile);

        navDashboardIcon = findViewById(R.id.navDashboardIcon);
        navProductsIcon = findViewById(R.id.navProductsIcon);
        navOrdersIcon = findViewById(R.id.navOrdersIcon);
        navAnalyticsIcon = findViewById(R.id.navAnalyticsIcon);
        navProfileIcon = findViewById(R.id.navProfileIcon);

        navDashboardText = findViewById(R.id.navDashboardText);
        navProductsText = findViewById(R.id.navProductsText);
        navOrdersText = findViewById(R.id.navOrdersText);
        navAnalyticsText = findViewById(R.id.navAnalyticsText);
        navProfileText = findViewById(R.id.navProfileText);
    }

    private void setupBottomNavigation() {
        View.OnClickListener navClickListener = v -> {
            resetNavSelection();
            int id = v.getId();
            if (id == R.id.navDashboard) {
                setNavActive(navDashboardIcon, navDashboardText);
                Intent intent = new Intent(Order_List.this, Dashboard.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } else if (id == R.id.navProducts) {
                setNavActive(navProductsIcon, navProductsText);
                startActivity(new Intent(Order_List.this, Inventory.class));
            } else if (id == R.id.navOrders) {
                setNavActive(navOrdersIcon, navOrdersText);
            } else if (id == R.id.navAnalytics) {
                setNavActive(navAnalyticsIcon, navAnalyticsText);
                startActivity(new Intent(Order_List.this, Analytics.class));
            } else if (id == R.id.navProfile) {
                setNavActive(navProfileIcon, navProfileText);
                startActivity(new Intent(Order_List.this, ProfilePage.class));
            }
        };

        navDashboard.setOnClickListener(navClickListener);
        navProducts.setOnClickListener(navClickListener);
        navOrders.setOnClickListener(navClickListener);
        navAnalytics.setOnClickListener(navClickListener);
        navProfile.setOnClickListener(navClickListener);
    }

    private void resetNavSelection() {
        int inactive = ContextCompat.getColor(this, R.color.nav_inactive);
        navDashboardIcon.setColorFilter(inactive);
        navProductsIcon.setColorFilter(inactive);
        navOrdersIcon.setColorFilter(inactive);
        navAnalyticsIcon.setColorFilter(inactive);
        navProfileIcon.setColorFilter(inactive);

        navDashboardText.setTextColor(inactive);
        navProductsText.setTextColor(inactive);
        navOrdersText.setTextColor(inactive);
        navAnalyticsText.setTextColor(inactive);
        navProfileText.setTextColor(inactive);

        navDashboardText.setTypeface(null);
        navProductsText.setTypeface(null);
        navOrdersText.setTypeface(null);
        navAnalyticsText.setTypeface(null);
        navProfileText.setTypeface(null);
    }

    private void setNavActive(ImageView icon, TextView text) {
        int active = ContextCompat.getColor(this, R.color.coral_primary);
        icon.setColorFilter(active);
        text.setTextColor(active);
        text.setTypeface(null, Typeface.BOLD);
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
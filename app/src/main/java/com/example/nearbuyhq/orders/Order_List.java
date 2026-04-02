package com.example.nearbuyhq.orders;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.SessionManager;
import com.example.nearbuyhq.data.repository.DataCallback;
import com.example.nearbuyhq.data.repository.OrderRepository;
import com.example.nearbuyhq.dashboard.Analytics;
import com.example.nearbuyhq.dashboard.Dashboard;
import com.example.nearbuyhq.products.Inventory;
import com.example.nearbuyhq.settings.ProfilePage;

import java.util.ArrayList;
import java.util.List;

// Order list screen – displays all orders for the current shop and allows status filtering.
public class Order_List extends AppCompatActivity {

    private RecyclerView recyclerOrders;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private List<Order> allOrders;
    private ImageView btnBack;
    private TextView tvTotalOrders, tvPendingCount, tvDeliveredCount;
    private TextView tabAll, tabPending, tabProcessing, tabDelivered, tabCancelled;
    private OrderRepository orderRepository;
    private String activeFilter = "All";
    private String searchQuery = "";
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

        tvTotalOrders = findViewById(R.id.tv_total_orders);
        tvPendingCount = findViewById(R.id.tv_pending_count);
        tvDeliveredCount = findViewById(R.id.tv_delivered_count);
        tabAll = findViewById(R.id.tab_all);
        tabPending = findViewById(R.id.tab_pending);
        tabProcessing = findViewById(R.id.tab_processing);
        tabDelivered = findViewById(R.id.tab_delivered);
        tabCancelled = findViewById(R.id.tab_cancelled);

        orderRepository = new OrderRepository();
        allOrders = new ArrayList<>();
        orderList = new ArrayList<>();
        setupFilterTabs();

        // Wire search
        EditText etSearch = findViewById(R.id.etSearchOrders);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                    searchQuery = s.toString().trim().toLowerCase();
                    applyOrderFilter();
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

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
        loadOrders(); // initial load from Firestore
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    // ── Bottom navigation ─────────────────────────────────────────────────
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


    // ── Status filter ─────────────────────────────────────────────────────
    private void setupFilterTabs() {
        tabAll.setOnClickListener(v -> setFilter("All"));
        tabPending.setOnClickListener(v -> setFilter("Pending"));
        tabProcessing.setOnClickListener(v -> setFilter("Processing"));
        tabDelivered.setOnClickListener(v -> setFilter("Delivered"));
        tabCancelled.setOnClickListener(v -> setFilter("Cancelled"));
    }

    private void setFilter(String filter) {
        activeFilter = filter;
        applyOrderFilter();
    }

    private void applyOrderFilter() {
        orderList.clear();
        for (Order order : allOrders) {
            boolean matchesFilter = "All".equals(activeFilter) || activeFilter.equalsIgnoreCase(order.getStatus());
            boolean matchesSearch = searchQuery.isEmpty()
                    || (order.getOrderId() != null && order.getOrderId().toLowerCase().contains(searchQuery))
                    || (order.getCustomerName() != null && order.getCustomerName().toLowerCase().contains(searchQuery));
            if (matchesFilter && matchesSearch) {
                orderList.add(order);
            }
        }
        orderAdapter.notifyDataSetChanged();
        updateSummary();
    }

    private void updateSummary() {
        int pending = 0;
        int delivered = 0;
        for (Order order : allOrders) {
            if ("Pending".equalsIgnoreCase(order.getStatus())) {
                pending++;
            }
            if ("Delivered".equalsIgnoreCase(order.getStatus())) {
                delivered++;
            }
        }
        tvTotalOrders.setText(String.valueOf(allOrders.size()));
        tvPendingCount.setText(String.valueOf(pending));
        tvDeliveredCount.setText(String.valueOf(delivered));
    }

    // ── Firestore load ────────────────────────────────────────────────────

    private void loadOrders() {
        String userId = SessionManager.getInstance(this).getUserId();
        orderRepository.getOrdersByShopId(userId, new DataCallback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> data) {
                allOrders.clear();
                allOrders.addAll(data);
                applyOrderFilter();
            }

            @Override
            public void onError(Exception exception) {
                // Show empty list on error rather than sample data
                allOrders.clear();
                applyOrderFilter();
                Toast.makeText(Order_List.this, "Could not load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
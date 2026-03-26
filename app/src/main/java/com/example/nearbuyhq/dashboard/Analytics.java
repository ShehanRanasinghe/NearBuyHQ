package com.example.nearbuyhq.dashboard;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.SessionManager;
import com.example.nearbuyhq.data.repository.DataCallback;
import com.example.nearbuyhq.data.repository.OrderRepository;
import com.example.nearbuyhq.data.repository.ProductRepository;
import com.example.nearbuyhq.data.repository.UserRepository;
import com.example.nearbuyhq.orders.Order;
import com.example.nearbuyhq.orders.Order_List;
import com.example.nearbuyhq.products.Inventory;
import com.example.nearbuyhq.products.ProductItem;
import com.example.nearbuyhq.settings.ProfilePage;
import com.example.nearbuyhq.data.model.User;

import java.util.List;
import java.util.Locale;

/**
 * Analytics screen – shows business overview statistics loaded live from Firestore.
 *
 * Metrics shown:
 *  - Total Revenue (sum of all Delivered order totals)
 *  - Total Sales   (count of Delivered orders)
 *  - Average Order Value
 *  - Total Customers (count of users registered in Firestore)
 */
public class Analytics extends AppCompatActivity {

    // ── Stat TextViews ────────────────────────────────────────────────────
    private TextView txtTotalRevenue, txtTotalSales, txtAvgOrderValue, txtTotalCustomers;

    // ── Bottom navigation ─────────────────────────────────────────────────
    private LinearLayout navDashboard, navProducts, navOrders, navAnalytics, navProfile;
    private ImageView navDashboardIcon, navProductsIcon, navOrdersIcon, navAnalyticsIcon, navProfileIcon;
    private TextView navDashboardText, navProductsText, navOrdersText, navAnalyticsText, navProfileText;

    // ── Repositories ──────────────────────────────────────────────────────
    private OrderRepository   orderRepository;
    private UserRepository    userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        orderRepository = new OrderRepository();
        userRepository  = new UserRepository();

        // Stat cards
        txtTotalRevenue    = findViewById(R.id.txtTotalRevenue);
        txtTotalSales      = findViewById(R.id.txtTotalSales);
        txtAvgOrderValue   = findViewById(R.id.txtAvgOrderValue);
        txtTotalCustomers  = findViewById(R.id.txtTotalCustomers);

        // Bottom navigation
        navDashboard = findViewById(R.id.navDashboard);
        navProducts  = findViewById(R.id.navProducts);
        navOrders    = findViewById(R.id.navOrders);
        navAnalytics = findViewById(R.id.navAnalytics);
        navProfile   = findViewById(R.id.navProfile);

        navDashboardIcon = findViewById(R.id.navDashboardIcon);
        navProductsIcon  = findViewById(R.id.navProductsIcon);
        navOrdersIcon    = findViewById(R.id.navOrdersIcon);
        navAnalyticsIcon = findViewById(R.id.navAnalyticsIcon);
        navProfileIcon   = findViewById(R.id.navProfileIcon);

        navDashboardText = findViewById(R.id.navDashboardText);
        navProductsText  = findViewById(R.id.navProductsText);
        navOrdersText    = findViewById(R.id.navOrdersText);
        navAnalyticsText = findViewById(R.id.navAnalyticsText);
        navProfileText   = findViewById(R.id.navProfileText);

        // Back button (exists in the analytics layout)
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Highlight Analytics as active
        setNavActive(navAnalyticsIcon, navAnalyticsText);

        setupBottomNavigation();
        loadAnalyticsData();
    }

    // ── Load from Firebase ────────────────────────────────────────────────

    /**
     * Load orders from Firestore and compute all four metrics.
     * Also loads the total customer count from the users collection.
     */
    private void loadAnalyticsData() {
        // ── Order-based stats ─────────────────────────────────────────
        orderRepository.getOrders(new DataCallback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> orders) {
                double totalRevenue = 0;
                int    deliveredCount = 0;

                for (Order o : orders) {
                    if ("Delivered".equalsIgnoreCase(o.getStatus())) {
                        totalRevenue += o.getOrderTotal();
                        deliveredCount++;
                    }
                }

                final double revenue = totalRevenue;
                final int    sales   = deliveredCount;
                final double avg     = (sales > 0) ? revenue / sales : 0;

                runOnUiThread(() -> {
                    if (txtTotalRevenue  != null)
                        txtTotalRevenue.setText(String.format(Locale.US, "Rs. %.0f", revenue));
                    if (txtTotalSales    != null)
                        txtTotalSales.setText(String.valueOf(sales));
                    if (txtAvgOrderValue != null)
                        txtAvgOrderValue.setText(String.format(Locale.US, "Rs. %.2f", avg));
                });
            }
            @Override
            public void onError(Exception e) { /* keep placeholder */ }
        });

        // ── Total customers (registered users count) ──────────────────
        userRepository.getUsers(new DataCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                runOnUiThread(() -> {
                    if (txtTotalCustomers != null)
                        txtTotalCustomers.setText(String.valueOf(users.size()));
                });
            }
            @Override
            public void onError(Exception e) { /* keep placeholder */ }
        });
    }

    // ── Navigation ────────────────────────────────────────────────────────

    private void setupBottomNavigation() {
        View.OnClickListener navClickListener = v -> {
            resetNavSelection();
            int id = v.getId();
            if (id == R.id.navDashboard) {
                setNavActive(navDashboardIcon, navDashboardText);
                Intent intent = new Intent(Analytics.this, Dashboard.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } else if (id == R.id.navProducts) {
                setNavActive(navProductsIcon, navProductsText);
                startActivity(new Intent(Analytics.this, Inventory.class));
            } else if (id == R.id.navOrders) {
                setNavActive(navOrdersIcon, navOrdersText);
                startActivity(new Intent(Analytics.this, Order_List.class));
            } else if (id == R.id.navAnalytics) {
                setNavActive(navAnalyticsIcon, navAnalyticsText);
                // Already on Analytics
            } else if (id == R.id.navProfile) {
                setNavActive(navProfileIcon, navProfileText);
                startActivity(new Intent(Analytics.this, ProfilePage.class));
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
        navDashboardIcon.setColorFilter(inactive); navProductsIcon.setColorFilter(inactive);
        navOrdersIcon.setColorFilter(inactive);    navAnalyticsIcon.setColorFilter(inactive);
        navProfileIcon.setColorFilter(inactive);
        navDashboardText.setTextColor(inactive);   navProductsText.setTextColor(inactive);
        navOrdersText.setTextColor(inactive);      navAnalyticsText.setTextColor(inactive);
        navProfileText.setTextColor(inactive);
        navDashboardText.setTypeface(null); navProductsText.setTypeface(null);
        navOrdersText.setTypeface(null);    navAnalyticsText.setTypeface(null);
        navProfileText.setTypeface(null);
    }

    private void setNavActive(ImageView icon, TextView text) {
        int active = ContextCompat.getColor(this, R.color.coral_primary);
        icon.setColorFilter(active);
        text.setTextColor(active);
        text.setTypeface(null, Typeface.BOLD);
    }
}

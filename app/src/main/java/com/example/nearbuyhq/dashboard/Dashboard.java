package com.example.nearbuyhq.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.SessionManager;
import com.example.nearbuyhq.data.repository.DataCallback;
import com.example.nearbuyhq.data.repository.OrderRepository;
import com.example.nearbuyhq.data.repository.ProductRepository;
import com.example.nearbuyhq.discounts.DiscountsActivity;
import com.example.nearbuyhq.notifications.Notifications;
import com.example.nearbuyhq.orders.Order;
import com.example.nearbuyhq.orders.Order_List;
import com.example.nearbuyhq.products.Add_Product;
import com.example.nearbuyhq.products.Inventory;
import com.example.nearbuyhq.products.ProductItem;
import com.example.nearbuyhq.reports.Reports;
import com.example.nearbuyhq.settings.ProfilePage;
import com.example.nearbuyhq.shops.ShopsList;
import com.example.nearbuyhq.users.UsersList;

import java.util.List;
import java.util.Locale;

/**
 * Dashboard – the main home screen shown after login.
 *
 * Displays live statistics loaded from Firestore:
 *  - Owner name and shop name from SessionManager
 *  - Total products, low-stock count, total orders, and total revenue
 */
public class Dashboard extends AppCompatActivity {

    // ── Bottom navigation views ──────────────────────────────────────────
    private LinearLayout navDashboard, navProducts, navOrders, navAnalytics, navProfile;
    private ImageView navDashboardIcon, navProductsIcon, navOrdersIcon, navAnalyticsIcon, navProfileIcon;
    private TextView navDashboardText, navProductsText, navOrdersText, navAnalyticsText, navProfileText;

    // ── Quick action buttons ─────────────────────────────────────────────
    private LinearLayout btnAddProduct, btnManageInventory, btnDiscounts;
    private LinearLayout btnManageOrders, btnViewReports;

    // ── Top bar icons ────────────────────────────────────────────────────
    private ImageView btnSearch, btnNotifications;

    // ── Stats TextViews (from layout) ────────────────────────────────────
    private TextView tvAdminName, tvShopName, tvShopStatus;
    private TextView tvTotalProducts, tvLowStock, tvTotalOrders, tvTotalRevenue;

    // ── Repositories / session ───────────────────────────────────────────
    private ProductRepository productRepository;
    private OrderRepository   orderRepository;
    private SessionManager    session;

    private static final int LOW_STOCK_THRESHOLD = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.deep_blue));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.bg_white));

        setContentView(R.layout.activity_dashboard);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        session           = SessionManager.getInstance(this);
        productRepository = new ProductRepository();
        orderRepository   = new OrderRepository();

        initViews();
        setupBottomNavigation();
        setupQuickActions();
        setupTopBarActions();

        // Populate name / shop from session immediately (fast)
        populateHeaderFromSession();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh stats every time we return to the Dashboard
        loadDashboardStats();
    }

    // ── Init ──────────────────────────────────────────────────────────────

    private void initViews() {
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

        // Quick actions
        btnAddProduct      = findViewById(R.id.btnAddProduct);
        btnManageInventory = findViewById(R.id.btnManageInventory);
        btnDiscounts       = findViewById(R.id.btnDiscounts);
        btnManageOrders    = findViewById(R.id.btnManageOrders);
        btnViewReports     = findViewById(R.id.btnViewReports);

        // Top bar
        btnSearch        = findViewById(R.id.btnSearch);
        btnNotifications = findViewById(R.id.btnNotifications);

        // Live stats – these TextViews are in the dashboard layout
        tvAdminName     = findViewById(R.id.tvAdminName);
        tvShopName      = findViewById(R.id.tvShopName);
        tvShopStatus    = findViewById(R.id.tvShopStatus);
        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvLowStock      = findViewById(R.id.tvLowStock);
        tvTotalOrders   = findViewById(R.id.tvTotalOrders);
        tvTotalRevenue  = findViewById(R.id.tvTotalRevenue);
    }

    // ── Header ────────────────────────────────────────────────────────────

    /** Show name/shop from SessionManager before Firestore data arrives. */
    private void populateHeaderFromSession() {
        if (tvAdminName  != null) tvAdminName.setText(session.getUserName());
        if (tvShopName   != null) tvShopName.setText(session.getShopName());
        if (tvShopStatus != null) tvShopStatus.setText("● OPEN");
    }

    // ── Stats loading ─────────────────────────────────────────────────────

    /**
     * Load all four dashboard metrics from Firestore:
     *   Total Products, Low Stock, Total Orders, Total Revenue
     */
    private void loadDashboardStats() {
        String shopId = session.getShopId();

        // ── Products ──────────────────────────────────────────────────
        productRepository.getProductsByShopId(shopId, "All",
                new DataCallback<List<ProductItem>>() {
                    @Override
                    public void onSuccess(List<ProductItem> items) {
                        int total    = items.size();
                        int lowStock = 0;
                        for (ProductItem p : items) {
                            if (p.isLowStock(LOW_STOCK_THRESHOLD)) lowStock++;
                        }
                        final int finalLow = lowStock;
                        runOnUiThread(() -> {
                            if (tvTotalProducts != null) tvTotalProducts.setText(String.valueOf(total));
                            if (tvLowStock      != null) tvLowStock.setText(String.valueOf(finalLow));
                        });
                    }
                    @Override
                    public void onError(Exception e) { /* keep placeholder values */ }
                });

        // ── Orders ────────────────────────────────────────────────────
        orderRepository.getOrders(new DataCallback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> orders) {
                int    totalOrders  = orders.size();
                double totalRevenue = 0;
                for (Order o : orders) {
                    if ("Delivered".equalsIgnoreCase(o.getStatus())) {
                        totalRevenue += o.getOrderTotal();
                    }
                }
                final double rev = totalRevenue;
                runOnUiThread(() -> {
                    if (tvTotalOrders  != null) tvTotalOrders.setText(String.valueOf(totalOrders));
                    if (tvTotalRevenue != null) tvTotalRevenue.setText(
                            String.format(Locale.US, "Rs. %.0f", rev));
                });
            }
            @Override
            public void onError(Exception e) { /* keep placeholder values */ }
        });
    }

    // ── Navigation ────────────────────────────────────────────────────────

    private void setupBottomNavigation() {
        View.OnClickListener navClickListener = v -> {
            resetNavSelection();
            int id = v.getId();

            if (id == R.id.navDashboard) {
                setNavActive(navDashboardIcon, navDashboardText);
            } else if (id == R.id.navProducts) {
                setNavActive(navProductsIcon, navProductsText);
                startActivity(new Intent(Dashboard.this, Inventory.class));
            } else if (id == R.id.navOrders) {
                setNavActive(navOrdersIcon, navOrdersText);
                startActivity(new Intent(Dashboard.this, Order_List.class));
            } else if (id == R.id.navAnalytics) {
                setNavActive(navAnalyticsIcon, navAnalyticsText);
                startActivity(new Intent(Dashboard.this, Analytics.class));
            } else if (id == R.id.navProfile) {
                setNavActive(navProfileIcon, navProfileText);
                startActivity(new Intent(Dashboard.this, ProfilePage.class));
            }
        };

        navDashboard.setOnClickListener(navClickListener);
        navProducts.setOnClickListener(navClickListener);
        navOrders.setOnClickListener(navClickListener);
        navAnalytics.setOnClickListener(navClickListener);
        navProfile.setOnClickListener(navClickListener);
    }

    private void resetNavSelection() {
        int inactiveColor = ContextCompat.getColor(this, R.color.nav_inactive);
        navDashboardIcon.setColorFilter(inactiveColor); navProductsIcon.setColorFilter(inactiveColor);
        navOrdersIcon.setColorFilter(inactiveColor);    navAnalyticsIcon.setColorFilter(inactiveColor);
        navProfileIcon.setColorFilter(inactiveColor);
        navDashboardText.setTextColor(inactiveColor);   navProductsText.setTextColor(inactiveColor);
        navOrdersText.setTextColor(inactiveColor);      navAnalyticsText.setTextColor(inactiveColor);
        navProfileText.setTextColor(inactiveColor);
        navDashboardText.setTypeface(null); navProductsText.setTypeface(null);
        navOrdersText.setTypeface(null);    navAnalyticsText.setTypeface(null);
        navProfileText.setTypeface(null);
    }

    private void setNavActive(ImageView icon, TextView text) {
        int activeColor = ContextCompat.getColor(this, R.color.coral_primary);
        icon.setColorFilter(activeColor);
        text.setTextColor(activeColor);
        text.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void setupQuickActions() {
        btnAddProduct.setOnClickListener(v ->
                startActivity(new Intent(Dashboard.this, Add_Product.class)));
        btnManageInventory.setOnClickListener(v ->
                startActivity(new Intent(Dashboard.this, Inventory.class)));
        btnDiscounts.setOnClickListener(v ->
                startActivity(new Intent(Dashboard.this, DiscountsActivity.class)));
        btnManageOrders.setOnClickListener(v ->
                startActivity(new Intent(Dashboard.this, Order_List.class)));
        btnViewReports.setOnClickListener(v ->
                startActivity(new Intent(Dashboard.this, Reports.class)));
    }

    private void setupTopBarActions() {
        btnSearch.setOnClickListener(v ->
                Toast.makeText(this, "Search – coming soon!", Toast.LENGTH_SHORT).show());
        btnNotifications.setOnClickListener(v ->
                startActivity(new Intent(Dashboard.this, Notifications.class)));
    }
}
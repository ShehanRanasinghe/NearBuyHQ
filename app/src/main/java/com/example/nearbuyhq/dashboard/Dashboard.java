package com.example.nearbuyhq.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.SessionManager;
import com.example.nearbuyhq.data.repository.AuthRepository;
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
import com.example.nearbuyhq.users.User;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Dashboard – main home screen shown after login.
 *
 * Shows live stats from Firestore:
 *  - Owner name + shop name from SessionManager (or reloaded from Firestore)
 *  - Total products, low-stock count
 *  - Orders + revenue filtered by the selected time period
 */
public class Dashboard extends AppCompatActivity {

    // ── Bottom navigation ─────────────────────────────────────────────────
    private LinearLayout navDashboard, navProducts, navOrders, navAnalytics, navProfile;
    private ImageView navDashboardIcon, navProductsIcon, navOrdersIcon, navAnalyticsIcon, navProfileIcon;
    private TextView navDashboardText, navProductsText, navOrdersText, navAnalyticsText, navProfileText;

    // ── Quick actions ─────────────────────────────────────────────────────
    private LinearLayout btnAddProduct, btnManageInventory, btnDiscounts;
    private LinearLayout btnManageOrders, btnViewReports;

    // ── Top bar icons ─────────────────────────────────────────────────────
    private ImageView btnSearch, btnNotifications;

    // ── Stats TextViews ───────────────────────────────────────────────────
    private TextView tvAdminName, tvShopName, tvShopStatus;
    private TextView tvTotalProducts, tvLowStock, tvTotalOrders, tvTotalRevenue;

    // ── Business Overview filter button ──────────────────────────────────
    private TextView btnOverviewFilter;

    // ── Current filter period ─────────────────────────────────────────────
    private String currentFilter = "Today"; // default

    // ── Repositories / session ────────────────────────────────────────────
    private ProductRepository productRepository;
    private OrderRepository   orderRepository;
    private AuthRepository    authRepository;
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
        authRepository    = new AuthRepository();

        initViews();
        setupBottomNavigation();
        setupQuickActions();
        setupTopBarActions();
        setupOverviewFilter();

        // Show cached session data immediately while Firestore loads
        populateHeaderFromSession();

        // If session name is still default, reload fresh from Firestore
        if (session.getUserName().equals("Shop Owner") || session.getUserName().isEmpty()) {
            reloadSessionFromFirebase();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateHeaderFromSession();
        loadDashboardStats();
    }

    // ── Init ──────────────────────────────────────────────────────────────

    private void initViews() {
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

        btnAddProduct      = findViewById(R.id.btnAddProduct);
        btnManageInventory = findViewById(R.id.btnManageInventory);
        btnDiscounts       = findViewById(R.id.btnDiscounts);
        btnManageOrders    = findViewById(R.id.btnManageOrders);
        btnViewReports     = findViewById(R.id.btnViewReports);

        btnSearch        = findViewById(R.id.btnSearch);
        btnNotifications = findViewById(R.id.btnNotifications);

        tvAdminName     = findViewById(R.id.tvAdminName);
        tvShopName      = findViewById(R.id.tvShopName);
        tvShopStatus    = findViewById(R.id.tvShopStatus);
        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvLowStock      = findViewById(R.id.tvLowStock);
        tvTotalOrders   = findViewById(R.id.tvTotalOrders);
        tvTotalRevenue  = findViewById(R.id.tvTotalRevenue);
        btnOverviewFilter = findViewById(R.id.btnOverviewFilter);
    }

    // ── Header ────────────────────────────────────────────────────────────

    private void populateHeaderFromSession() {
        String name = session.getUserName();
        String shop = session.getShopName();
        if (tvAdminName  != null) tvAdminName.setText(name.isEmpty() ? "Shop Owner" : name);
        if (tvShopName   != null) tvShopName.setText(shop.isEmpty()  ? "My Shop"    : shop);
        if (tvShopStatus != null) tvShopStatus.setText("● OPEN");
    }

    /**
     * If session has no user name (e.g. first open after install with saved auth token),
     * reload the user profile + shop from Firestore.
     */
    private void reloadSessionFromFirebase() {
        com.google.firebase.auth.FirebaseUser fbUser =
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser == null) return;

        String uid = fbUser.getUid();
        session.saveUserId(uid);

        authRepository.getUserProfile(uid, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                session.saveUserName(user.getName());
                session.saveUserEmail(user.getEmail());
                runOnUiThread(() -> populateHeaderFromSession());
            }
            @Override public void onError(Exception e) {}
        });

        // Load shop via owner UID
        new com.example.nearbuyhq.data.repository.ShopRepository()
                .getShopByOwnerUid(uid, new DataCallback<com.example.nearbuyhq.shops.Shop>() {
                    @Override
                    public void onSuccess(com.example.nearbuyhq.shops.Shop shop) {
                        if (shop != null) {
                            session.saveShopId(shop.getId());
                            session.saveShopName(shop.getName());
                            runOnUiThread(() -> populateHeaderFromSession());
                            // Refresh stats now that we have the shopId
                            runOnUiThread(() -> loadDashboardStats());
                        }
                    }
                    @Override public void onError(Exception e) {}
                });
    }

    // ── Overview filter dropdown ──────────────────────────────────────────

    private void setupOverviewFilter() {
        if (btnOverviewFilter == null) return;
        btnOverviewFilter.setOnClickListener(v -> showFilterMenu(v));
    }

    private void showFilterMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 0, 0, "Today");
        popup.getMenu().add(0, 1, 1, "Yesterday");
        popup.getMenu().add(0, 2, 2, "Last 7 Days");
        popup.getMenu().add(0, 3, 3, "This Month");
        popup.getMenu().add(0, 4, 4, "Lifetime");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0: currentFilter = "Today";       break;
                case 1: currentFilter = "Yesterday";   break;
                case 2: currentFilter = "Last 7 Days"; break;
                case 3: currentFilter = "This Month";  break;
                case 4: currentFilter = "Lifetime";    break;
            }
            btnOverviewFilter.setText(currentFilter + " ▾");
            loadFilteredRevenue();
            return true;
        });
        popup.show();
    }

    // ── Stats loading ─────────────────────────────────────────────────────

    private void loadDashboardStats() {
        String shopId = session.getShopId();

        // Products count
        productRepository.getProductsByShopId(shopId, "All",
                new DataCallback<List<ProductItem>>() {
                    @Override
                    public void onSuccess(List<ProductItem> items) {
                        int total    = items.size();
                        int lowStock = 0;
                        for (ProductItem p : items) {
                            if (p.isLowStock(LOW_STOCK_THRESHOLD)) lowStock++;
                        }
                        final int fl = lowStock;
                        runOnUiThread(() -> {
                            if (tvTotalProducts != null) tvTotalProducts.setText(String.valueOf(total));
                            if (tvLowStock      != null) tvLowStock.setText(String.valueOf(fl));
                        });
                    }
                    @Override public void onError(Exception e) {}
                });

        // Orders + revenue for current filter
        loadFilteredRevenue();
    }

    /**
     * Load orders filtered by the selected time period and update the
     * Orders + Revenue cards in the Business Overview.
     */
    private void loadFilteredRevenue() {
        long[] range = getDateRange(currentFilter);
        long from = range[0];
        long to   = range[1];

        orderRepository.getOrdersByDateRange(from, to, new DataCallback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> orders) {
                int    count   = orders.size();
                double revenue = 0;
                for (Order o : orders) {
                    if ("Delivered".equalsIgnoreCase(o.getStatus())) {
                        revenue += o.getOrderTotal();
                    }
                }
                final double rev = revenue;
                final int    cnt = count;
                runOnUiThread(() -> {
                    if (tvTotalOrders  != null) tvTotalOrders.setText(String.valueOf(cnt));
                    if (tvTotalRevenue != null)
                        tvTotalRevenue.setText(String.format(Locale.US, "Rs. %.0f", rev));
                });
            }
            @Override public void onError(Exception e) {
                // Fallback to all-orders query if date-range fails (e.g. missing index)
                orderRepository.getOrders(new DataCallback<List<Order>>() {
                    @Override
                    public void onSuccess(List<Order> orders) {
                        int cnt = orders.size();
                        double rev = 0;
                        for (Order o : orders) {
                            if ("Delivered".equalsIgnoreCase(o.getStatus())) rev += o.getOrderTotal();
                        }
                        final double finalRev = rev;
                        runOnUiThread(() -> {
                            if (tvTotalOrders  != null) tvTotalOrders.setText(String.valueOf(cnt));
                            if (tvTotalRevenue != null)
                                tvTotalRevenue.setText(String.format(Locale.US, "Rs. %.0f", finalRev));
                        });
                    }
                    @Override public void onError(Exception e2) {}
                });
            }
        });
    }

    /**
     * Returns [fromMs, toMs] timestamp range for the given filter label.
     */
    private long[] getDateRange(String filter) {
        Calendar cal = Calendar.getInstance();
        long to = cal.getTimeInMillis();
        long from;

        switch (filter) {
            case "Yesterday":
                cal.add(Calendar.DAY_OF_YEAR, -1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                from = cal.getTimeInMillis();
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                to = cal.getTimeInMillis();
                break;
            case "Last 7 Days":
                cal.add(Calendar.DAY_OF_YEAR, -7);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                from = cal.getTimeInMillis();
                break;
            case "This Month":
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                from = cal.getTimeInMillis();
                break;
            case "Lifetime":
                from = 0L; // epoch – all records
                to   = Long.MAX_VALUE / 2; // very far future
                break;
            default: // "Today"
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                from = cal.getTimeInMillis();
                break;
        }
        return new long[]{from, to};
    }

    // ── Navigation ────────────────────────────────────────────────────────

    private void setupBottomNavigation() {
        View.OnClickListener nav = v -> {
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
        navDashboard.setOnClickListener(nav);
        navProducts.setOnClickListener(nav);
        navOrders.setOnClickListener(nav);
        navAnalytics.setOnClickListener(nav);
        navProfile.setOnClickListener(nav);
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
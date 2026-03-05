package com.example.nearbuyhq;

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

public class Dashboard extends AppCompatActivity {

    // Bottom navigation views
    private LinearLayout navDashboard, navShops, navProducts, navOrders, navProfile;
    private ImageView navDashboardIcon, navShopsIcon, navProductsIcon, navOrdersIcon, navProfileIcon;
    private TextView navDashboardText, navShopsText, navProductsText, navOrdersText, navProfileText;

    // Quick action buttons
    private LinearLayout btnAddShop, btnAddProduct, btnManageOrders, btnManagePrices, btnDeliveries, btnSettings;

    // Top bar icons
    private ImageView btnSearch, btnNotifications, btnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set status bar color to match the dark theme
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.dashboard_surface));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.nav_bar_bg));

        setContentView(R.layout.activity_dashboard);

        // Hide ActionBar for a clean look
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        setupBottomNavigation();
        setupQuickActions();
        setupTopBarActions();
    }

    private void initViews() {
        // Bottom navigation
        navDashboard = findViewById(R.id.navDashboard);
        navShops = findViewById(R.id.navShops);
        navProducts = findViewById(R.id.navProducts);
        navOrders = findViewById(R.id.navOrders);
        navProfile = findViewById(R.id.navProfile);

        navDashboardIcon = findViewById(R.id.navDashboardIcon);
        navShopsIcon = findViewById(R.id.navShopsIcon);
        navProductsIcon = findViewById(R.id.navProductsIcon);
        navOrdersIcon = findViewById(R.id.navOrdersIcon);
        navProfileIcon = findViewById(R.id.navProfileIcon);

        navDashboardText = findViewById(R.id.navDashboardText);
        navShopsText = findViewById(R.id.navShopsText);
        navProductsText = findViewById(R.id.navProductsText);
        navOrdersText = findViewById(R.id.navOrdersText);
        navProfileText = findViewById(R.id.navProfileText);

        // Quick actions
        btnAddShop = findViewById(R.id.btnAddShop);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnManageOrders = findViewById(R.id.btnManageOrders);
        btnManagePrices = findViewById(R.id.btnManagePrices);
        btnDeliveries = findViewById(R.id.btnDeliveries);
        btnSettings = findViewById(R.id.btnSettings);

        // Top bar
        btnSearch = findViewById(R.id.btnSearch);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnProfile = findViewById(R.id.btnProfile);
    }

    private void setupBottomNavigation() {
        View.OnClickListener navClickListener = v -> {
            resetNavSelection();
            int id = v.getId();

            if (id == R.id.navDashboard) {
                setNavActive(navDashboardIcon, navDashboardText);
            } else if (id == R.id.navShops) {
                setNavActive(navShopsIcon, navShopsText);
                Toast.makeText(this, "Shops", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.navProducts) {
                setNavActive(navProductsIcon, navProductsText);
                Toast.makeText(this, "Products", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.navOrders) {
                setNavActive(navOrdersIcon, navOrdersText);
                Toast.makeText(this, "Orders", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.navProfile) {
                setNavActive(navProfileIcon, navProfileText);
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
            }
        };

        navDashboard.setOnClickListener(navClickListener);
        navShops.setOnClickListener(navClickListener);
        navProducts.setOnClickListener(navClickListener);
        navOrders.setOnClickListener(navClickListener);
        navProfile.setOnClickListener(navClickListener);
    }

    private void resetNavSelection() {
        int inactiveColor = ContextCompat.getColor(this, R.color.text_hint_dark);

        navDashboardIcon.setColorFilter(inactiveColor);
        navShopsIcon.setColorFilter(inactiveColor);
        navProductsIcon.setColorFilter(inactiveColor);
        navOrdersIcon.setColorFilter(inactiveColor);
        navProfileIcon.setColorFilter(inactiveColor);

        navDashboardText.setTextColor(inactiveColor);
        navShopsText.setTextColor(inactiveColor);
        navProductsText.setTextColor(inactiveColor);
        navOrdersText.setTextColor(inactiveColor);
        navProfileText.setTextColor(inactiveColor);

        navDashboardText.setTypeface(null);
        navShopsText.setTypeface(null);
        navProductsText.setTypeface(null);
        navOrdersText.setTypeface(null);
        navProfileText.setTypeface(null);
    }

    private void setNavActive(ImageView icon, TextView text) {
        int activeColor = ContextCompat.getColor(this, R.color.amber_primary);
        icon.setColorFilter(activeColor);
        text.setTextColor(activeColor);
        text.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void setupQuickActions() {
        btnAddShop.setOnClickListener(v ->
                Toast.makeText(this, "Add Shop", Toast.LENGTH_SHORT).show());
        btnAddProduct.setOnClickListener(v ->
                Toast.makeText(this, "Add Product", Toast.LENGTH_SHORT).show());
        btnManageOrders.setOnClickListener(v ->
                Toast.makeText(this, "Manage Orders", Toast.LENGTH_SHORT).show());
        btnManagePrices.setOnClickListener(v ->
                Toast.makeText(this, "Manage Prices", Toast.LENGTH_SHORT).show());
        btnDeliveries.setOnClickListener(v ->
                Toast.makeText(this, "Deliveries", Toast.LENGTH_SHORT).show());
        btnSettings.setOnClickListener(v ->
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show());
    }

    private void setupTopBarActions() {
        btnSearch.setOnClickListener(v ->
                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show());
        btnNotifications.setOnClickListener(v ->
                Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show());
        btnProfile.setOnClickListener(v ->
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show());
    }
}
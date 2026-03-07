package com.example.nearbuyhq;

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

public class Dashboard extends AppCompatActivity {

    // Bottom navigation views
    private LinearLayout navDashboard, navProducts, navOrders, navAnalytics, navProfile;
    private ImageView navDashboardIcon, navProductsIcon, navOrdersIcon, navAnalyticsIcon, navProfileIcon;
    private TextView navDashboardText, navProductsText, navOrdersText, navAnalyticsText, navProfileText;

    // Quick action buttons
    private LinearLayout btnAddProduct, btnManageInventory, btnCreatePromotion;
    private LinearLayout btnManageOrders, btnUpdateLocation, btnViewReports;

    // Top bar icons
    private ImageView btnSearch, btnNotifications, btnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set status bar color to match the deep blue header
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.deep_blue));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.bg_white));

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

        // Quick actions
        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnManageInventory = findViewById(R.id.btnManageInventory);
        btnCreatePromotion = findViewById(R.id.btnCreatePromotion);
        btnManageOrders = findViewById(R.id.btnManageOrders);
        btnUpdateLocation = findViewById(R.id.btnUpdateLocation);
        btnViewReports = findViewById(R.id.btnViewReports);

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
            } else if (id == R.id.navProducts) {
                setNavActive(navProductsIcon, navProductsText);
                // Navigate to Products List
                Intent intent = new Intent(Dashboard.this, Products_List.class);
                startActivity(intent);
            } else if (id == R.id.navOrders) {
                setNavActive(navOrdersIcon, navOrdersText);
                Toast.makeText(this, "Orders", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.navAnalytics) {
                setNavActive(navAnalyticsIcon, navAnalyticsText);
                // Navigate to Analytics
                Intent analyticsIntent = new Intent(Dashboard.this, Analytics.class);
                startActivity(analyticsIntent);
            } else if (id == R.id.navProfile) {
                setNavActive(navProfileIcon, navProfileText);
                Intent profileIntent = new Intent(Dashboard.this, ProfilePage.class);
                startActivity(profileIntent);
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

        navDashboardIcon.setColorFilter(inactiveColor);
        navProductsIcon.setColorFilter(inactiveColor);
        navOrdersIcon.setColorFilter(inactiveColor);
        navAnalyticsIcon.setColorFilter(inactiveColor);
        navProfileIcon.setColorFilter(inactiveColor);

        navDashboardText.setTextColor(inactiveColor);
        navProductsText.setTextColor(inactiveColor);
        navOrdersText.setTextColor(inactiveColor);
        navAnalyticsText.setTextColor(inactiveColor);
        navProfileText.setTextColor(inactiveColor);

        navDashboardText.setTypeface(null);
        navProductsText.setTypeface(null);
        navOrdersText.setTypeface(null);
        navAnalyticsText.setTypeface(null);
        navProfileText.setTypeface(null);
    }

    private void setNavActive(ImageView icon, TextView text) {
        int activeColor = ContextCompat.getColor(this, R.color.coral_primary);
        icon.setColorFilter(activeColor);
        text.setTextColor(activeColor);
        text.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void setupQuickActions() {
        btnAddProduct.setOnClickListener(v -> {
            // Navigate to Add Product
            Intent intent = new Intent(Dashboard.this, Add_Product.class);
            startActivity(intent);
        });
        btnManageInventory.setOnClickListener(v -> {
            // Navigate to Products List
            Intent intent = new Intent(Dashboard.this, Products_List.class);
            startActivity(intent);
        });
        btnCreatePromotion.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, Promotions.class);
            startActivity(intent);
        });
        btnManageOrders.setOnClickListener(v ->
                Toast.makeText(this, "View Orders", Toast.LENGTH_SHORT).show());
        btnUpdateLocation.setOnClickListener(v ->
                Toast.makeText(this, "Update Shop Location", Toast.LENGTH_SHORT).show());
        btnViewReports.setOnClickListener(v ->
                Toast.makeText(this, "View Reports", Toast.LENGTH_SHORT).show());
    }

    private void setupTopBarActions() {
        btnSearch.setOnClickListener(v ->
                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show());
        btnNotifications.setOnClickListener(v ->
                Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show());
        btnProfile.setOnClickListener(v -> {
            Intent profileIntent = new Intent(Dashboard.this, ProfilePage.class);
            startActivity(profileIntent);
        });
    }
}
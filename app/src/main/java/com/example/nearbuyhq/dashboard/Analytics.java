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
import com.example.nearbuyhq.orders.Order_List;
import com.example.nearbuyhq.products.Inventory;
import com.example.nearbuyhq.settings.ProfilePage;

public class Analytics extends AppCompatActivity {

    private LinearLayout navDashboard, navProducts, navOrders, navAnalytics, navProfile;
    private ImageView navDashboardIcon, navProductsIcon, navOrdersIcon, navAnalyticsIcon, navProfileIcon;
    private TextView navDashboardText, navProductsText, navOrdersText, navAnalyticsText, navProfileText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

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

        // Highlight Analytics as active
        setNavActive(navAnalyticsIcon, navAnalyticsText);

        setupBottomNavigation();
    }

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
}

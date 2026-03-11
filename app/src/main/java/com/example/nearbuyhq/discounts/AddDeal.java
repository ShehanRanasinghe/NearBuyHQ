package com.example.nearbuyhq.discounts;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.dashboard.Analytics;
import com.example.nearbuyhq.dashboard.Dashboard;
import com.example.nearbuyhq.orders.Order_List;
import com.example.nearbuyhq.products.Inventory;
import com.example.nearbuyhq.settings.ProfilePage;

public class AddDeal extends AppCompatActivity {

    private EditText dealTitle, dealDescription, dealDiscount, dealValidity;
    private Spinner shopSelection;
    private TextView btnSubmit, btnCancel;

    // Bottom navigation
    private LinearLayout navDashboard, navProducts, navOrders, navAnalytics, navProfile;
    private ImageView navDashboardIcon, navProductsIcon, navOrdersIcon, navAnalyticsIcon, navProfileIcon;
    private TextView navDashboardText, navProductsText, navOrdersText, navAnalyticsText, navProfileText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_deal);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        dealTitle = findViewById(R.id.dealTitle);
        dealDescription = findViewById(R.id.dealDescription);
        dealDiscount = findViewById(R.id.dealDiscount);
        dealValidity = findViewById(R.id.dealValidity);
        shopSelection = findViewById(R.id.shopSelection);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnCancel = findViewById(R.id.btnCancel);

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

        // Setup shop spinner
        String[] shops = {"Fresh Mart", "Tech Hub", "Fashion Plaza", "Book Haven", "Coffee Corner", "Fitness First"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, shops);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shopSelection.setAdapter(adapter);

        btnSubmit.setOnClickListener(v -> {
            if (validateInputs()) {
                Toast.makeText(this, "Deal created successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnCancel.setOnClickListener(v -> finish());

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        View.OnClickListener navClickListener = v -> {
            resetNavSelection();
            int id = v.getId();

            if (id == R.id.navDashboard) {
                setNavActive(navDashboardIcon, navDashboardText);
                Intent intent = new Intent(AddDeal.this, Dashboard.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } else if (id == R.id.navProducts) {
                setNavActive(navProductsIcon, navProductsText);
                startActivity(new Intent(AddDeal.this, Inventory.class));
            } else if (id == R.id.navOrders) {
                setNavActive(navOrdersIcon, navOrdersText);
                startActivity(new Intent(AddDeal.this, Order_List.class));
            } else if (id == R.id.navAnalytics) {
                setNavActive(navAnalyticsIcon, navAnalyticsText);
                startActivity(new Intent(AddDeal.this, Analytics.class));
            } else if (id == R.id.navProfile) {
                setNavActive(navProfileIcon, navProfileText);
                startActivity(new Intent(AddDeal.this, ProfilePage.class));
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

    private boolean validateInputs() {
        if (dealTitle.getText().toString().trim().isEmpty()) {
            dealTitle.setError("Deal title is required");
            return false;
        }
        if (dealDescription.getText().toString().trim().isEmpty()) {
            dealDescription.setError("Description is required");
            return false;
        }
        if (dealDiscount.getText().toString().trim().isEmpty()) {
            dealDiscount.setError("Discount is required");
            return false;
        }
        if (dealValidity.getText().toString().trim().isEmpty()) {
            dealValidity.setError("Validity date is required");
            return false;
        }
        return true;
    }
}


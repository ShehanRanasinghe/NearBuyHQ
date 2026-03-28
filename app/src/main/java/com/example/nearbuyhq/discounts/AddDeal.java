package com.example.nearbuyhq.discounts;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.SessionManager;
import com.example.nearbuyhq.data.repository.DiscountRepository;
import com.example.nearbuyhq.data.repository.OperationCallback;
import com.example.nearbuyhq.dashboard.Analytics;
import com.example.nearbuyhq.dashboard.Dashboard;
import com.example.nearbuyhq.orders.Order_List;
import com.example.nearbuyhq.products.Inventory;
import com.example.nearbuyhq.settings.ProfilePage;

import java.util.Calendar;
import java.util.Locale;

// Add/Edit deal screen – creates or updates a shop-wide deal stored in NearBuyHQ/{userId}/deals.
public class AddDeal extends AppCompatActivity {

    private EditText dealTitle, dealDescription, dealValidity;
    private TextView btnSubmit, btnCancel;
    private DiscountRepository discountRepository;
    private boolean editMode;
    private String editDealId;
    private long originalCreatedAt;

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
        dealValidity = findViewById(R.id.dealValidity);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnCancel = findViewById(R.id.btnCancel);
        discountRepository = new DiscountRepository();

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

        // Date picker for Valid Until
        dealValidity.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year  = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day   = cal.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(AddDeal.this, (view, y, m, d) -> {
                String date = String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y);
                dealValidity.setText(date);
            }, year, month, day).show();
        });

        bindEditPayload();

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> {
            if (validateInputs()) {
                saveDeal();
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
        if (dealValidity.getText().toString().trim().isEmpty()) {
            dealValidity.setError("Validity date is required");
            return false;
        }
        return true;
    }

    private void saveDeal() {
        SessionManager session = SessionManager.getInstance(this);
        String userId   = session.getUserId();
        String shopName = session.getShopName();

        long now = System.currentTimeMillis();
        Deal deal = new Deal(
                editMode ? editDealId : "",
                dealTitle.getText().toString().trim(),
                shopName,
                "",
                dealDescription.getText().toString().trim(),
                dealValidity.getText().toString().trim(),
                editMode ? originalCreatedAt : now,
                now
        );
        deal.setUserId(userId);

        setSaving(true);
        discountRepository.saveDeal(deal, new OperationCallback() {
            @Override
            public void onSuccess() {
                setSaving(false);
                Toast.makeText(AddDeal.this, editMode ? "Deal updated" : "Deal created successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(Exception exception) {
                setSaving(false);
                Toast.makeText(AddDeal.this, "Failed to save: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setSaving(boolean saving) {
        btnSubmit.setEnabled(!saving);
        btnSubmit.setAlpha(saving ? 0.6f : 1f);
        btnSubmit.setText(saving ? "Saving..." : (editMode ? "Update Deal" : "Create Deal"));
    }

    private void bindEditPayload() {
        editMode = getIntent().getBooleanExtra("is_edit", false);
        if (!editMode) {
            originalCreatedAt = System.currentTimeMillis();
            return;
        }

        editDealId = getIntent().getStringExtra("deal_id");
        originalCreatedAt = getIntent().getLongExtra("deal_created_at", System.currentTimeMillis());

        dealTitle.setText(getIntent().getStringExtra("deal_title"));
        dealDescription.setText(getIntent().getStringExtra("deal_description"));
        dealValidity.setText(getIntent().getStringExtra("deal_validity"));

        btnSubmit.setText("Update Deal");
    }
}

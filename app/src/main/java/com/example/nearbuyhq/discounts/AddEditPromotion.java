package com.example.nearbuyhq.discounts;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.SessionManager;
import com.example.nearbuyhq.data.repository.DataCallback;
import com.example.nearbuyhq.data.repository.DiscountRepository;
import com.example.nearbuyhq.data.repository.OperationCallback;
import com.example.nearbuyhq.dashboard.Analytics;
import com.example.nearbuyhq.dashboard.Dashboard;
import com.example.nearbuyhq.orders.Order_List;
import com.example.nearbuyhq.products.Inventory;
import com.example.nearbuyhq.settings.ProfilePage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

// AddEditPromotion – form screen for creating a new promotion or editing an existing one.
public class AddEditPromotion extends AppCompatActivity {

    static final String EXTRA_PROMO_ID = "promo_id";

    private EditText  etTitle, etProduct, etOriginalPrice, etDiscount, etNotes;
    private Spinner   spinnerType;
    private TextView  tvStartDate, tvEndDate, tvDiscountedPrice, tvFormTitle;
    private Switch    switchActive;
    private Button    btnSave;

    // Bottom navigation
    private LinearLayout navDashboard, navProducts, navOrders, navAnalytics, navProfile;
    private ImageView navDashboardIcon, navProductsIcon, navOrdersIcon, navAnalyticsIcon, navProfileIcon;
    private TextView navDashboardText, navProductsText, navOrdersText, navAnalyticsText, navProfileText;

    private Promotion editing; // null means creating a new promotion
    private DiscountRepository discountRepository;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private static final String[] TYPES = {
            Promotion.TYPE_RAMADAN,
            Promotion.TYPE_CHRISTMAS,
            Promotion.TYPE_NEW_YEAR,
            Promotion.TYPE_THAI_PONGAL,
            Promotion.TYPE_CUSTOM
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.deep_blue));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.bg_white));

        setContentView(R.layout.activity_add_edit_promotion);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // ── Bind views ──────────────────────────────────────────────────
        tvFormTitle       = findViewById(R.id.tvFormTitle);
        etTitle           = findViewById(R.id.etPromoTitle);
        spinnerType       = findViewById(R.id.spinnerPromoType);
        etProduct         = findViewById(R.id.etPromoProduct);
        etOriginalPrice   = findViewById(R.id.etPromoOriginalPrice);
        etDiscount        = findViewById(R.id.etPromoDiscount);
        tvDiscountedPrice = findViewById(R.id.tvPromoDiscountedPrice);
        tvStartDate       = findViewById(R.id.tvPromoStartDate);
        tvEndDate         = findViewById(R.id.tvPromoEndDate);
        etNotes           = findViewById(R.id.etPromoNotes);
        switchActive      = findViewById(R.id.switchPromoActive);
        btnSave           = findViewById(R.id.btnSavePromotion);
        discountRepository = new DiscountRepository();

        // ── Spinner setup ────────────────────────────────────────────────
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, TYPES);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // ── Default dates (today) ────────────────────────────────────────
        String today = sdf.format(new Date());
        tvStartDate.setText(today);
        tvEndDate.setText(today);

        // ── Date pickers ─────────────────────────────────────────────────
        tvStartDate.setOnClickListener(v -> showDatePicker(tvStartDate));
        tvEndDate.setOnClickListener(v -> showDatePicker(tvEndDate));

        // ── Auto-calculate discounted price ──────────────────────────────
        TextWatcher priceWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) {}
            public void afterTextChanged(Editable s) { recalcDiscount(); }
        };
        etOriginalPrice.addTextChangedListener(priceWatcher);
        etDiscount.addTextChangedListener(priceWatcher);

        // ── Load if editing ──────────────────────────────────────────────
        String promoId = getIntent().getStringExtra(EXTRA_PROMO_ID);
        if (promoId != null) {
            loadForEditing(promoId);
            tvFormTitle.setText("Edit Promotion");
            btnSave.setText("Update Promotion");
        } else {
            tvFormTitle.setText("New Promotion");
        }

        // ── Bottom Navigation ────────────────────────────────────────────
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

        setupBottomNavigation();

        // Wire back and save buttons
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> savePromotion());
    }

    // ── Bottom Navigation ────────────────────────────────────────────────────

    private void setupBottomNavigation() {
        View.OnClickListener navClickListener = v -> {
            resetNavSelection();
            int id = v.getId();
            if (id == R.id.navDashboard) {
                setNavActive(navDashboardIcon, navDashboardText);
                Intent intent = new Intent(AddEditPromotion.this, Dashboard.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } else if (id == R.id.navProducts) {
                setNavActive(navProductsIcon, navProductsText);
                startActivity(new Intent(AddEditPromotion.this, Inventory.class));
            } else if (id == R.id.navOrders) {
                setNavActive(navOrdersIcon, navOrdersText);
                startActivity(new Intent(AddEditPromotion.this, Order_List.class));
            } else if (id == R.id.navAnalytics) {
                setNavActive(navAnalyticsIcon, navAnalyticsText);
                startActivity(new Intent(AddEditPromotion.this, Analytics.class));
            } else if (id == R.id.navProfile) {
                setNavActive(navProfileIcon, navProfileText);
                startActivity(new Intent(AddEditPromotion.this, ProfilePage.class));
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

    // ── Date picker ─────────────────────────────────────────────────────────

    private void showDatePicker(TextView target) {
        Calendar cal = Calendar.getInstance();
        try {
            Date existing = sdf.parse(target.getText().toString());
            if (existing != null) cal.setTime(existing);
        } catch (ParseException ignored) {}

        new DatePickerDialog(this, (view, year, month, day) -> {
            cal.set(year, month, day);
            target.setText(sdf.format(cal.getTime()));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    // ── Discounted price preview ─────────────────────────────────────────────

    // Recalculate and display the final price whenever original price or discount % changes
    private void recalcDiscount() {
        try {
            double original = Double.parseDouble(etOriginalPrice.getText().toString());
            int    pct      = Integer.parseInt(etDiscount.getText().toString());
            if (pct < 0 || pct > 100) {
                tvDiscountedPrice.setText("—");
                return;
            }
            double discounted = original - (original * pct / 100.0);
            tvDiscountedPrice.setText(
                    String.format(Locale.getDefault(), "Rs. %.2f", discounted));
        } catch (NumberFormatException e) {
            tvDiscountedPrice.setText("—");
        }
    }

    // ── Load existing promotion for editing ──────────────────────────────────

    private void loadForEditing(String promoId) {
        String userId = SessionManager.getInstance(this).getUserId();
        discountRepository.getPromotion(promoId, userId, new DataCallback<Promotion>() {
            @Override
            public void onSuccess(Promotion data) {
                editing = data;
                populateFormFromEditing();
            }

            @Override
            public void onError(Exception exception) {
                Toast.makeText(AddEditPromotion.this, "Failed to load promotion", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Populate all form fields from the loaded Promotion object
    private void populateFormFromEditing() {
        if (editing == null) return;
        etTitle.setText(editing.getTitle());
        etProduct.setText(editing.getProductName());
        if (editing.getOriginalPrice() > 0) {
            etOriginalPrice.setText(String.valueOf(editing.getOriginalPrice()));
        }
        etDiscount.setText(String.valueOf(editing.getDiscountPercentage()));
        tvStartDate.setText(editing.getStartDate());
        tvEndDate.setText(editing.getEndDate());
        switchActive.setChecked(editing.isActive());

        for (int i = 0; i < TYPES.length; i++) {
            if (TYPES[i].equals(editing.getType())) {
                spinnerType.setSelection(i);
                break;
            }
        }
        recalcDiscount();
    }

    // ── Save / Update ────────────────────────────────────────────────────────

    // Validate form inputs, build a Promotion object, and persist to Firestore
    private void savePromotion() {
        // Validate
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) { etTitle.setError("Title is required"); return; }

        String discountStr = etDiscount.getText().toString().trim();
        if (discountStr.isEmpty()) { etDiscount.setError("Enter discount %"); return; }

        int discount;
        try {
            discount = Integer.parseInt(discountStr);
            if (discount < 0 || discount > 100) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            etDiscount.setError("Enter 0–100");
            return;
        }

        double originalPrice = 0;
        String priceStr = etOriginalPrice.getText().toString().trim();
        if (!priceStr.isEmpty()) {
            try { originalPrice = Double.parseDouble(priceStr); }
            catch (NumberFormatException e) {
                etOriginalPrice.setError("Enter a valid price");
                return;
            }
        }

        String type      = TYPES[spinnerType.getSelectedItemPosition()];
        String product   = etProduct.getText().toString().trim();
        String startDate = tvStartDate.getText().toString();
        String endDate   = tvEndDate.getText().toString();
        boolean active   = switchActive.isChecked();

        // Build the Promotion object
        String id = (editing != null) ? editing.getId() : UUID.randomUUID().toString();
        Promotion p = new Promotion(id, title, type, discount,
                startDate, endDate, product, originalPrice, active);

        // Attach owner's userId so the repository can dual-write to the subcollection
        String userId = (editing != null && !editing.getUserId().isEmpty())
                ? editing.getUserId()
                : SessionManager.getInstance(this).getUserId();
        p.setUserId(userId);


        setSaving(true);
        discountRepository.savePromotion(p, new OperationCallback() {
            @Override
            public void onSuccess() {
                setSaving(false);
                Toast.makeText(AddEditPromotion.this,
                        editing != null ? "Promotion updated!" : "Promotion added!",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception exception) {
                setSaving(false);
                Toast.makeText(AddEditPromotion.this, "Save failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Toggle the save button state while the Firestore write is in progress
    private void setSaving(boolean saving) {
        btnSave.setEnabled(!saving);
        btnSave.setAlpha(saving ? 0.6f : 1f);
        btnSave.setText(saving ? "Saving..." : (editing != null ? "Update Promotion" : "Save Promotion"));
    }
}

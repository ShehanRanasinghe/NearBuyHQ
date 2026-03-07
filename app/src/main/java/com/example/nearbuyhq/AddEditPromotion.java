package com.example.nearbuyhq;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class AddEditPromotion extends AppCompatActivity {

    static final String EXTRA_PROMO_ID = "promo_id";

    private EditText  etTitle, etProduct, etOriginalPrice, etDiscount, etNotes;
    private Spinner   spinnerType;
    private TextView  tvStartDate, tvEndDate, tvDiscountedPrice, tvFormTitle;
    private Switch    switchActive;
    private Button    btnSave;

    private Promotion editing; // null means creating a new promotion

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

        // ── Back / Save ──────────────────────────────────────────────────
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> savePromotion());
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
                    String.format(Locale.getDefault(), "$%.2f", discounted));
        } catch (NumberFormatException e) {
            tvDiscountedPrice.setText("—");
        }
    }

    // ── Load existing promotion for editing ──────────────────────────────────

    private void loadForEditing(String promoId) {
        SharedPreferences prefs = getSharedPreferences(Promotions.PREFS_NAME, MODE_PRIVATE);
        String json = prefs.getString(Promotions.PREFS_KEY, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (promoId.equals(obj.optString("id"))) {
                    editing = Promotion.fromJson(obj);
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (editing == null) return;

        etTitle.setText(editing.getTitle());
        etProduct.setText(editing.getProductName());
        if (editing.getOriginalPrice() > 0)
            etOriginalPrice.setText(String.valueOf(editing.getOriginalPrice()));
        etDiscount.setText(String.valueOf(editing.getDiscountPercentage()));
        tvStartDate.setText(editing.getStartDate());
        tvEndDate.setText(editing.getEndDate());
        switchActive.setChecked(editing.isActive());

        // Select the matching spinner position
        for (int i = 0; i < TYPES.length; i++) {
            if (TYPES[i].equals(editing.getType())) {
                spinnerType.setSelection(i);
                break;
            }
        }
        recalcDiscount();
    }

    // ── Save / Update ────────────────────────────────────────────────────────

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

        // Persist: load → update/insert → save
        SharedPreferences prefs = getSharedPreferences(Promotions.PREFS_NAME, MODE_PRIVATE);
        String json = prefs.getString(Promotions.PREFS_KEY, "[]");
        JSONArray updated = new JSONArray();
        boolean replaced = false;

        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (id.equals(obj.optString("id"))) {
                    updated.put(p.toJson());
                    replaced = true;
                } else {
                    updated.put(obj);
                }
            }
            if (!replaced) updated.put(p.toJson());
        } catch (JSONException e) {
            e.printStackTrace();
            try { updated.put(p.toJson()); } catch (JSONException ignored) {}
        }

        prefs.edit().putString(Promotions.PREFS_KEY, updated.toString()).apply();
        Toast.makeText(this,
                editing != null ? "Promotion updated!" : "Promotion added!",
                Toast.LENGTH_SHORT).show();
        finish();
    }
}

package com.example.nearbuyhq.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.nearbuyhq.R;

public class ProfilePage extends AppCompatActivity {

    // Action buttons
    private ImageView btnBack;
    private LinearLayout btnEditProfile, btnLogout;
    private LinearLayout btnEditShopDetails;

    // Account info TextViews
    private TextView tvShopLocation;

    // Shop Details TextViews
    private TextView tvStoreCategory, tvOpeningHours, tvWebsite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.profile_teal_header));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.bg_white));

        setContentView(R.layout.activity_profile);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack            = (ImageView) findViewById(R.id.btnBack);    

        btnEditProfile     = findViewById(R.id.btnEditProfile);
        btnLogout          = findViewById(R.id.btnLogout);
        btnEditShopDetails = findViewById(R.id.btnEditShopDetails);

        tvShopLocation  = findViewById(R.id.tvShopLocation);

        tvStoreCategory = findViewById(R.id.tvStoreCategory);
        tvOpeningHours  = findViewById(R.id.tvOpeningHours);
        tvWebsite       = findViewById(R.id.tvWebsite);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnLogout.setOnClickListener(v -> showLogoutDialog());
        btnEditShopDetails.setOnClickListener(v -> showEditShopDialog());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Edit Profile
    // ─────────────────────────────────────────────────────────────────────────
    private void showEditProfileDialog() {
        LinearLayout container = buildDialogContainer();
        EditText etName  = addDialogField(container, "Owner Name",  "");
        EditText etEmail = addDialogField(container, "Email",       "");
        EditText etPhone = addDialogField(container, "Phone",       "");

        new AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setView(container)
                .setPositiveButton("Save", (dialog, which) ->
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show())
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Edit Shop Details  (location + category + hours + website)
    // ─────────────────────────────────────────────────────────────────────────
    private void showEditShopDialog() {
        LinearLayout container = buildDialogContainer();
        EditText etLocation = addDialogField(container, "Store Location",  tvShopLocation.getText().toString());
        EditText etCategory = addDialogField(container, "Store Category",  tvStoreCategory.getText().toString());
        EditText etHours    = addDialogField(container, "Opening Hours",   tvOpeningHours.getText().toString());
        EditText etWebsite  = addDialogField(container, "Website / Social",tvWebsite.getText().toString());

        new AlertDialog.Builder(this)
                .setTitle("Edit Shop Details")
                .setView(container)
                .setPositiveButton("Save", (dialog, which) -> {
                    String loc = etLocation.getText().toString().trim();
                    String cat = etCategory.getText().toString().trim();
                    String hrs = etHours.getText().toString().trim();
                    String web = etWebsite.getText().toString().trim();

                    if (!loc.isEmpty()) tvShopLocation.setText(loc);
                    if (!cat.isEmpty()) tvStoreCategory.setText(cat);
                    if (!hrs.isEmpty()) tvOpeningHours.setText(hrs);
                    if (!web.isEmpty()) tvWebsite.setText(web);

                    Toast.makeText(this, "Shop details updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Logout
    // ─────────────────────────────────────────────────────────────────────────
    private void showLogoutDialog() {
        Intent intent = new Intent(ProfilePage.this, LogoutConfirmation.class);
        startActivity(intent);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Dialog helpers
    // ─────────────────────────────────────────────────────────────────────────
    private LinearLayout buildDialogContainer() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        container.setPadding(pad, dp(8), pad, 0);
        return container;
    }

    private EditText addDialogField(LinearLayout parent, String hint, String value) {
        TextView label = new TextView(this);
        label.setText(hint);
        label.setTextSize(12);
        label.setTextColor(ContextCompat.getColor(this, R.color.text_dark_secondary));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(10);
        parent.addView(label, lp);

        EditText et = new EditText(this);
        et.setHint(hint);
        et.setText(value);
        et.setTextSize(15);
        et.setTextColor(ContextCompat.getColor(this, R.color.text_dark_primary));
        et.setSingleLine(true);
        parent.addView(et, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return et;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}


package com.example.nearbuyhq;

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

public class ProfilePage extends AppCompatActivity {

    // Action buttons
    private ImageView btnBack;
    private LinearLayout btnEditProfile, btnLogout;
    private LinearLayout btnEditShopDetails;
    private LinearLayout btnAddBranch;
    private LinearLayout btnEditBranch1, btnEditBranch2;

    // Account info TextViews
    private TextView tvShopLocation;

    // Shop Details TextViews
    private TextView tvStoreCategory, tvOpeningHours, tvWebsite;

    // Branch 1 TextViews
    private TextView tvBranch1Name, tvBranch1Address, tvBranch1Phone;

    // Branch 2 TextViews
    private TextView tvBranch2Name, tvBranch2Address, tvBranch2Phone;

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
        btnAddBranch       = findViewById(R.id.btnAddBranch);
        btnEditBranch1     = findViewById(R.id.btnEditBranch1);
        btnEditBranch2     = findViewById(R.id.btnEditBranch2);

        tvShopLocation  = findViewById(R.id.tvShopLocation);

        tvStoreCategory = findViewById(R.id.tvStoreCategory);
        tvOpeningHours  = findViewById(R.id.tvOpeningHours);
        tvWebsite       = findViewById(R.id.tvWebsite);

        tvBranch1Name    = findViewById(R.id.tvBranch1Name);
        tvBranch1Address = findViewById(R.id.tvBranch1Address);
        tvBranch1Phone   = findViewById(R.id.tvBranch1Phone);

        tvBranch2Name    = findViewById(R.id.tvBranch2Name);
        tvBranch2Address = findViewById(R.id.tvBranch2Address);
        tvBranch2Phone   = findViewById(R.id.tvBranch2Phone);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnLogout.setOnClickListener(v -> showLogoutDialog());
        btnEditShopDetails.setOnClickListener(v -> showEditShopDialog());
        btnAddBranch.setOnClickListener(v -> showAddBranchDialog());
        btnEditBranch1.setOnClickListener(v -> showEditBranchDialog(
                tvBranch1Name, tvBranch1Address, tvBranch1Phone));
        btnEditBranch2.setOnClickListener(v -> showEditBranchDialog(
                tvBranch2Name, tvBranch2Address, tvBranch2Phone));
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
    //  Add Branch
    // ─────────────────────────────────────────────────────────────────────────
    private void showAddBranchDialog() {
        LinearLayout container = buildDialogContainer();
        EditText etName    = addDialogField(container, "Branch Name",    "");
        EditText etAddress = addDialogField(container, "Branch Address", "");
        EditText etPhone   = addDialogField(container, "Branch Phone",   "");

        new AlertDialog.Builder(this)
                .setTitle("Add New Branch")
                .setView(container)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Branch name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(this, "Branch \"" + name + "\" added", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Edit Branch  (generic – works for any branch card)
    // ─────────────────────────────────────────────────────────────────────────
    private void showEditBranchDialog(TextView nameView, TextView addrView, TextView phoneView) {
        LinearLayout container = buildDialogContainer();
        EditText etName    = addDialogField(container, "Branch Name",    nameView.getText().toString());
        EditText etAddress = addDialogField(container, "Branch Address", addrView.getText().toString());
        EditText etPhone   = addDialogField(container, "Branch Phone",   phoneView.getText().toString());

        new AlertDialog.Builder(this)
                .setTitle("Edit Branch")
                .setView(container)
                .setPositiveButton("Update", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String addr = etAddress.getText().toString().trim();
                    String ph   = etPhone.getText().toString().trim();

                    if (!name.isEmpty()) nameView.setText(name);
                    if (!addr.isEmpty()) addrView.setText(addr);
                    if (!ph.isEmpty())   phoneView.setText(ph);

                    Toast.makeText(this, "Branch updated", Toast.LENGTH_SHORT).show();
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


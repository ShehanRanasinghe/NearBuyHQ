package com.example.nearbuyhq.settings;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
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
import com.example.nearbuyhq.core.SessionManager;
import com.example.nearbuyhq.dashboard.Analytics;
import com.example.nearbuyhq.dashboard.Dashboard;
import com.example.nearbuyhq.data.repository.AuthRepository;
import com.example.nearbuyhq.data.repository.DataCallback;
import com.example.nearbuyhq.data.repository.OperationCallback;
import com.example.nearbuyhq.data.repository.ShopRepository;
import com.example.nearbuyhq.orders.Order_List;
import com.example.nearbuyhq.products.Inventory;
import com.example.nearbuyhq.shops.Shop;

/**
 * Profile page – shows the logged-in shop owner's personal info and shop details.
 *
 * Data is loaded from Firebase Firestore on resume and can be edited via dialogs.
 * All changes are saved back to Firestore immediately.
 */
public class ProfilePage extends AppCompatActivity {

    // ── Top header views ────────────────────────────────────────────────
    private TextView tvOwnerName, tvProfileShopName;

    // ── Account info views ───────────────────────────────────────────────
    private TextView tvEmail, tvPhone, tvShopNameDetail, tvShopLocation;

    // ── Shop details views ───────────────────────────────────────────────
    private TextView tvStoreCategory, tvOpeningHours, tvWebsite;

    // ── Action buttons ───────────────────────────────────────────────────
    private ImageView btnBack;
    private LinearLayout btnEditProfile, btnLogout, btnEditShopDetails;

    // ── Bottom navigation ────────────────────────────────────────────────
    private LinearLayout navDashboard, navProducts, navOrders, navAnalytics, navProfile;
    private ImageView navDashboardIcon, navProductsIcon, navOrdersIcon, navAnalyticsIcon, navProfileIcon;
    private TextView navDashboardText, navProductsText, navOrdersText, navAnalyticsText, navProfileText;

    // ── Repositories / session ───────────────────────────────────────────
    private AuthRepository authRepository;
    private ShopRepository shopRepository;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Match the teal header colour in the status bar
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.profile_teal_header));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.bg_white));

        setContentView(R.layout.activity_profile);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        initViews();
        setupListeners();
        setupBottomNavigation();
        resetNavSelection();
        setNavActive(navProfileIcon, navProfileText);

        // Load real data from Firebase
        loadProfileData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData(); // Refresh whenever we return to this screen
    }

    // ── Init ─────────────────────────────────────────────────────────────

    private void initViews() {
        btnBack            = findViewById(R.id.btnBack);
        btnEditProfile     = findViewById(R.id.btnEditProfile);
        btnLogout          = findViewById(R.id.btnLogout);
        btnEditShopDetails = findViewById(R.id.btnEditShopDetails);

        // Header
        tvOwnerName        = findViewById(R.id.tvOwnerName);
        tvProfileShopName  = findViewById(R.id.tvProfileShopName);

        // Account info card
        tvEmail            = findViewById(R.id.tvEmail);
        tvPhone            = findViewById(R.id.tvPhone);
        tvShopNameDetail   = findViewById(R.id.tvShopNameDetail);
        tvShopLocation     = findViewById(R.id.tvShopLocation);

        // Shop details card
        tvStoreCategory    = findViewById(R.id.tvStoreCategory);
        tvOpeningHours     = findViewById(R.id.tvOpeningHours);
        tvWebsite          = findViewById(R.id.tvWebsite);

        // Bottom navigation
        navDashboard    = findViewById(R.id.navDashboard);
        navProducts     = findViewById(R.id.navProducts);
        navOrders       = findViewById(R.id.navOrders);
        navAnalytics    = findViewById(R.id.navAnalytics);
        navProfile      = findViewById(R.id.navProfile);

        navDashboardIcon  = findViewById(R.id.navDashboardIcon);
        navProductsIcon   = findViewById(R.id.navProductsIcon);
        navOrdersIcon     = findViewById(R.id.navOrdersIcon);
        navAnalyticsIcon  = findViewById(R.id.navAnalyticsIcon);
        navProfileIcon    = findViewById(R.id.navProfileIcon);

        navDashboardText  = findViewById(R.id.navDashboardText);
        navProductsText   = findViewById(R.id.navProductsText);
        navOrdersText     = findViewById(R.id.navOrdersText);
        navAnalyticsText  = findViewById(R.id.navAnalyticsText);
        navProfileText    = findViewById(R.id.navProfileText);

        // Repositories
        authRepository = new AuthRepository();
        shopRepository = new ShopRepository();
        session        = SessionManager.getInstance(this);
    }

    // ── Load data from Firebase ───────────────────────────────────────────

    /**
     * Load the current user's profile AND their shop details from Firestore,
     * then populate all the TextViews on this screen.
     */
    private void loadProfileData() {
        String uid    = session.getUserId();
        String shopId = session.getShopId();

        // Display whatever we already have in session while we wait for Firestore
        if (tvOwnerName    != null) tvOwnerName.setText(session.getUserName());
        if (tvEmail        != null) tvEmail.setText(session.getUserEmail());
        if (tvPhone        != null) tvPhone.setText(session.getUserPhone());
        if (tvProfileShopName != null) tvProfileShopName.setText(session.getShopName());
        if (tvShopNameDetail  != null) tvShopNameDetail.setText(session.getShopName());

        // ── Load fresh user profile from Firestore ─────────────────────
        if (uid != null && !uid.isEmpty()) {
            authRepository.getUserProfile(uid, new DataCallback<com.example.nearbuyhq.users.User>() {
                @Override
                public void onSuccess(com.example.nearbuyhq.users.User user) {
                    runOnUiThread(() -> {
                        if (tvOwnerName != null) tvOwnerName.setText(user.getName());
                        if (tvEmail     != null) tvEmail.setText(user.getEmail());
                        String phone = getFieldSafe(user.toMap(), "phone");
                        if (tvPhone != null) tvPhone.setText(phone.isEmpty() ? "Not set" : phone);
                        session.saveUserName(user.getName());
                        session.saveUserEmail(user.getEmail());
                        if (!phone.isEmpty()) session.saveUserPhone(phone);
                    });
                }
                @Override
                public void onError(Exception e) { /* use session data */ }
            });
        }

        // ── Load shop details from Firestore ───────────────────────────
        if (shopId != null && !shopId.isEmpty()) {
            shopRepository.getShop(shopId, new DataCallback<Shop>() {
                @Override
                public void onSuccess(Shop shop) { runOnUiThread(() -> populateShopViews(shop)); }
                @Override
                public void onError(Exception e) { /* use defaults */ }
            });
        } else if (uid != null && !uid.isEmpty()) {
            shopRepository.getShopByOwnerUid(uid, new DataCallback<Shop>() {
                @Override
                public void onSuccess(Shop shop) {
                    if (shop != null) {
                        session.saveShopId(shop.getId());
                        session.saveShopName(shop.getName());
                        runOnUiThread(() -> populateShopViews(shop));
                    }
                }
                @Override
                public void onError(Exception e) { /* no shop yet */ }
            });
        }
    }

    /** Push shop data into the UI text views. */
    private void populateShopViews(Shop shop) {
        if (shop == null) return;

        if (tvProfileShopName != null) tvProfileShopName.setText(shop.getName());
        if (tvShopNameDetail  != null) tvShopNameDetail.setText(shop.getName());
        if (tvShopLocation    != null) tvShopLocation.setText(
                shop.getLocation().isEmpty() ? "Not set" : shop.getLocation());
        if (tvStoreCategory   != null) tvStoreCategory.setText(
                shop.getCategory().isEmpty() ? "Not set" : shop.getCategory());
        if (tvOpeningHours    != null) tvOpeningHours.setText(
                shop.getOpeningHours().isEmpty() ? "Not set" : shop.getOpeningHours());
        if (tvWebsite         != null) tvWebsite.setText(
                shop.getWebsite().isEmpty() ? "Not set" : shop.getWebsite());
    }

    // ── Listeners ─────────────────────────────────────────────────────────

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnLogout.setOnClickListener(v -> showLogoutDialog());
        btnEditShopDetails.setOnClickListener(v -> showEditShopDialog());
    }

    // ── Edit Profile dialog ───────────────────────────────────────────────

    private void showEditProfileDialog() {
        LinearLayout container = buildDialogContainer();
        EditText etName  = addDialogField(container, "Owner Name",  safeText(tvOwnerName));
        EditText etEmail = addDialogField(container, "Email",       safeText(tvEmail));
        EditText etPhone = addDialogField(container, "Phone",       safeText(tvPhone));

        new AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setView(container)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName  = etName.getText().toString().trim();
                    String newPhone = etPhone.getText().toString().trim();
                    String uid      = session.getUserId();

                    if (newName.isEmpty()) {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Save to Firestore
                    authRepository.updateUserProfile(uid, newName, newPhone, new OperationCallback() {
                        @Override
                        public void onSuccess() {
                            // Update UI and session
                            if (tvOwnerName != null) tvOwnerName.setText(newName);
                            if (tvPhone     != null) tvPhone.setText(newPhone);
                            session.saveUserName(newName);
                            session.saveUserPhone(newPhone);
                            Toast.makeText(ProfilePage.this, "Profile updated", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(ProfilePage.this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Edit Shop Details dialog ──────────────────────────────────────────

    private void showEditShopDialog() {
        LinearLayout container = buildDialogContainer();
        EditText etShopName = addDialogField(container, "Shop Name",       safeText(tvShopNameDetail));
        EditText etLocation = addDialogField(container, "Store Location",  safeText(tvShopLocation));
        EditText etCategory = addDialogField(container, "Store Category",  safeText(tvStoreCategory));
        EditText etHours    = addDialogField(container, "Opening Hours",   safeText(tvOpeningHours));
        EditText etWebsite  = addDialogField(container, "Website / Social",safeText(tvWebsite));
        EditText etContact  = addDialogField(container, "Contact Number",  safeText(tvPhone));

        new AlertDialog.Builder(this)
                .setTitle("Edit Shop Details")
                .setView(container)
                .setPositiveButton("Save", (dialog, which) -> {
                    String shopId = session.getShopId();

                    if (shopId == null || shopId.isEmpty()) {
                        Toast.makeText(this, "No shop found. Please add a branch first.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String newShopName = etShopName.getText().toString().trim();
                    String newLocation = etLocation.getText().toString().trim();
                    String newCategory = etCategory.getText().toString().trim();
                    String newHours    = etHours.getText().toString().trim();
                    String newWebsite  = etWebsite.getText().toString().trim();
                    String newContact  = etContact.getText().toString().trim();

                    // Save all shop fields to Firestore
                    shopRepository.updateShopProfile(
                            shopId, newShopName, newLocation, newCategory,
                            newHours, newWebsite, newContact,
                            new OperationCallback() {
                                @Override
                                public void onSuccess() {
                                    // Update UI
                                    if (!newShopName.isEmpty()) {
                                        if (tvProfileShopName != null) tvProfileShopName.setText(newShopName);
                                        if (tvShopNameDetail  != null) tvShopNameDetail.setText(newShopName);
                                        session.saveShopName(newShopName);
                                    }
                                    if (!newLocation.isEmpty() && tvShopLocation  != null) tvShopLocation.setText(newLocation);
                                    if (!newCategory.isEmpty() && tvStoreCategory != null) tvStoreCategory.setText(newCategory);
                                    if (!newHours.isEmpty()    && tvOpeningHours  != null) tvOpeningHours.setText(newHours);
                                    if (!newWebsite.isEmpty()  && tvWebsite       != null) tvWebsite.setText(newWebsite);

                                    Toast.makeText(ProfilePage.this, "Shop details updated", Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onError(Exception e) {
                                    Toast.makeText(ProfilePage.this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                    );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Logout ────────────────────────────────────────────────────────────

    private void showLogoutDialog() {
        startActivity(new Intent(ProfilePage.this, LogoutConfirmation.class));
    }

    // ── Bottom navigation ─────────────────────────────────────────────────

    private void setupBottomNavigation() {
        View.OnClickListener navClickListener = v -> {
            resetNavSelection();
            int id = v.getId();
            if (id == R.id.navDashboard) {
                setNavActive(navDashboardIcon, navDashboardText);
                Intent intent = new Intent(ProfilePage.this, Dashboard.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } else if (id == R.id.navProducts) {
                setNavActive(navProductsIcon, navProductsText);
                startActivity(new Intent(ProfilePage.this, Inventory.class));
            } else if (id == R.id.navOrders) {
                setNavActive(navOrdersIcon, navOrdersText);
                startActivity(new Intent(ProfilePage.this, Order_List.class));
            } else if (id == R.id.navAnalytics) {
                setNavActive(navAnalyticsIcon, navAnalyticsText);
                startActivity(new Intent(ProfilePage.this, Analytics.class));
            } else if (id == R.id.navProfile) {
                setNavActive(navProfileIcon, navProfileText);
                // Already here
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
        text.setTypeface(null, Typeface.BOLD);
    }

    // ── Dialog helpers ────────────────────────────────────────────────────

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

    /** Helper: safely get a TextView's text; returns empty string for null. */
    private String safeText(TextView tv) {
        if (tv == null) return "";
        String t = tv.getText().toString().trim();
        return (t.equals("Not set") || t.isEmpty()) ? "" : t;
    }

    /** Helper: read an extra String field from a user toMap() result. */
    private String getFieldSafe(java.util.Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? "" : String.valueOf(v).trim();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}


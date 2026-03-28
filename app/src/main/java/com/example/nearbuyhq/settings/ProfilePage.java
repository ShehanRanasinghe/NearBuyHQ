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
import com.example.nearbuyhq.data.model.User;
import com.example.nearbuyhq.data.repository.AuthRepository;
import com.example.nearbuyhq.data.repository.DataCallback;
import com.example.nearbuyhq.data.repository.OperationCallback;
import com.example.nearbuyhq.orders.Order_List;
import com.example.nearbuyhq.products.Inventory;

import java.util.Locale;

/**
 * ProfilePage – allows the owner to view and update their shop profile; also handles logout.
 *
 * Profile page – shows the logged-in shop owner's personal info and shop details.
 *
 * All data (user + shop) is stored in the single users/{uid} Firestore document.
 * No separate shop collection or shop ID is needed.
 */
public class ProfilePage extends AppCompatActivity {

    private static final int REQ_LOCATION_PICKER = 1001;

    // ── Top header views ──────────────────────────────────────────────────
    private TextView tvOwnerName, tvProfileShopName;

    // ── Account info views ────────────────────────────────────────────────
    private TextView tvEmail, tvPhone, tvShopNameDetail, tvShopLocation, tvOpeningHours;

    // ── Action buttons ────────────────────────────────────────────────────
    private ImageView btnBack;
    private LinearLayout btnEditProfile, btnLogout, btnPickLocation;

    // ── Bottom navigation ─────────────────────────────────────────────────
    private LinearLayout navDashboard, navProducts, navOrders, navAnalytics, navProfile;
    private ImageView navDashboardIcon, navProductsIcon, navOrdersIcon, navAnalyticsIcon, navProfileIcon;
    private TextView navDashboardText, navProductsText, navOrdersText, navAnalyticsText, navProfileText;

    // ── Repository / session ──────────────────────────────────────────────
    private AuthRepository authRepository;
    private SessionManager session;

    // ── Location coordinates (cached for the location picker) ─────────────
    private double shopLat = 0.0;
    private double shopLng = 0.0;

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
        setupBottomNavigation();
        resetNavSelection();
        setNavActive(navProfileIcon, navProfileText);
        loadProfileData(); // pull the latest profile from Firestore on open
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload when returning from LocationPicker or edit dialog.
        // dataLoaded is reset to false by onActivityResult so we always
        // refresh after saving a new location.
        loadProfileData();
    }

    // ── Init ──────────────────────────────────────────────────────────────

    private void initViews() {
        btnBack         = findViewById(R.id.btnBack);
        btnEditProfile  = findViewById(R.id.btnEditProfile);
        btnLogout       = findViewById(R.id.btnLogout);
        btnPickLocation = findViewById(R.id.btnPickLocation);

        tvOwnerName       = findViewById(R.id.tvOwnerName);
        tvProfileShopName = findViewById(R.id.tvProfileShopName);
        tvEmail           = findViewById(R.id.tvEmail);
        tvPhone           = findViewById(R.id.tvPhone);
        tvShopNameDetail  = findViewById(R.id.tvShopNameDetail);
        tvShopLocation    = findViewById(R.id.tvShopLocation);
        tvOpeningHours    = findViewById(R.id.tvOpeningHours);

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

        authRepository = new AuthRepository();
        session        = SessionManager.getInstance(this);
    }

    // ── Load data ─────────────────────────────────────────────────────────

    /**
     * Load all profile data (user + shop) from the single users/{uid} document.
     */
    private void loadProfileData() {
        String uid = session.getUserId();

        // Show cached session values while Firestore loads
        if (tvOwnerName       != null) tvOwnerName.setText(session.getUserName());
        if (tvEmail           != null) tvEmail.setText(session.getUserEmail());
        if (tvPhone           != null) tvPhone.setText(
                session.getUserPhone().isEmpty() ? "Not set" : session.getUserPhone());
        if (tvProfileShopName != null) tvProfileShopName.setText(session.getShopName());
        if (tvShopNameDetail  != null) tvShopNameDetail.setText(session.getShopName());

        if (uid == null || uid.isEmpty()) return;

        // Single Firestore read – users/{uid} now contains everything
        authRepository.getUserProfile(uid, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> populateAllViews(user));
            }
            @Override
            public void onError(Exception e) { /* keep session fallback values */ }
        });
    }

    /** Populate every view on the screen from a fully-loaded User object. */
    private void populateAllViews(User user) {
        if (user == null) return;

        // ── User details ──────────────────────────────────────────────
        if (tvOwnerName != null) tvOwnerName.setText(user.getName());
        if (tvEmail     != null) tvEmail.setText(user.getEmail());
        String phone = user.getPhone() != null ? user.getPhone().trim() : "";
        if (tvPhone != null) tvPhone.setText(phone.isEmpty() ? "Not set" : phone);

        // ── Shop details ──────────────────────────────────────────────
        String shopName = user.getShopName().isEmpty() ? session.getShopName() : user.getShopName();
        if (tvProfileShopName != null) tvProfileShopName.setText(shopName);
        if (tvShopNameDetail  != null) tvShopNameDetail.setText(shopName);

        String locText = user.getShopLocation();
        if (locText.isEmpty() && user.hasLocation()) {
            locText = String.format(Locale.US, "%.5f, %.5f",
                    user.getLatitude(), user.getLongitude());
        }
        if (tvShopLocation != null) tvShopLocation.setText(locText.isEmpty() ? "Not set" : locText);
        if (tvOpeningHours != null) tvOpeningHours.setText(
                user.getOpeningHours().isEmpty() ? "Not set" : user.getOpeningHours());

        // Cache coordinates for location picker
        shopLat = user.getLatitude();
        shopLng = user.getLongitude();

        // Update session cache
        session.saveUserName(user.getName());
        session.saveUserEmail(user.getEmail());
        if (!phone.isEmpty()) session.saveUserPhone(phone);
        if (!user.getShopName().isEmpty()) session.saveShopName(user.getShopName());
    }

    // ── Listeners ─────────────────────────────────────────────────────────

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnLogout.setOnClickListener(v -> showLogoutDialog());
        btnPickLocation.setOnClickListener(v -> openLocationPicker());
    }

    // ── Location Picker ───────────────────────────────────────────────────

    @SuppressWarnings("deprecation")
    private void openLocationPicker() {
        Intent intent = new Intent(this, LocationPickerActivity.class);
        if (shopLat != 0.0 && shopLng != 0.0) {
            intent.putExtra(LocationPickerActivity.EXTRA_LATITUDE,  shopLat);
            intent.putExtra(LocationPickerActivity.EXTRA_LONGITUDE, shopLng);
        }
        if (tvShopLocation != null) {
            String current = tvShopLocation.getText().toString();
            if (!current.equals("Not set") && !current.equals("—")) {
                intent.putExtra(LocationPickerActivity.EXTRA_ADDRESS, current);
            }
        }
        startActivityForResult(intent, REQ_LOCATION_PICKER);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_LOCATION_PICKER && resultCode == RESULT_OK && data != null) {
            double lat     = data.getDoubleExtra(LocationPickerActivity.EXTRA_LATITUDE, 0.0);
            double lng     = data.getDoubleExtra(LocationPickerActivity.EXTRA_LONGITUDE, 0.0);
            String address = data.getStringExtra(LocationPickerActivity.EXTRA_ADDRESS);
            if (address == null) address = "";

            shopLat = lat;
            shopLng = lng;

            String display = address.isEmpty()
                    ? String.format(Locale.US, "%.5f, %.5f", lat, lng)
                    : address;
            if (tvShopLocation != null) tvShopLocation.setText(display);

            // Save lat/lng + address into users/{uid}
            String uid = session.getUserId();
            if (uid != null && !uid.isEmpty()) {
                final String finalAddress = address;
                authRepository.updateUserLocation(uid, lat, lng, finalAddress,
                        new OperationCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(ProfilePage.this,
                                        "Location updated successfully", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(ProfilePage.this,
                                        "Failed to save location", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    // ── Edit Profile dialog ───────────────────────────────────────────────

    private void showEditProfileDialog() {
        LinearLayout container = buildDialogContainer();
        EditText etName     = addDialogField(container, "Owner Name",  safeText(tvOwnerName));
        EditText etEmail    = addDialogField(container, "Email",       safeText(tvEmail));
        EditText etPhone    = addDialogField(container, "Phone",       safeText(tvPhone));
        EditText etShopName = addDialogField(container, "Shop Name",   safeText(tvShopNameDetail));

        // ── Opening Hours: two time pickers ──────────────────────────
        String existingHours = safeText(tvOpeningHours);
        final int[] fromTime = parseHoursTime(existingHours, true);
        final int[] toTime   = parseHoursTime(existingHours, false);

        addSectionLabel(container, "Opening Hours");
        addSmallLabel(container, "Opens At");
        final TextView btnFrom = addTimeButton(container, fromTime[0], fromTime[1]);
        btnFrom.setOnClickListener(v ->
                new android.app.TimePickerDialog(this, (tp, h, m) -> {
                    fromTime[0] = h; fromTime[1] = m;
                    btnFrom.setText(formatTime12h(h, m));
                }, fromTime[0], fromTime[1], false).show());

        addSmallLabel(container, "Closes At");
        final TextView btnTo = addTimeButton(container, toTime[0], toTime[1]);
        btnTo.setOnClickListener(v ->
                new android.app.TimePickerDialog(this, (tp, h, m) -> {
                    toTime[0] = h; toTime[1] = m;
                    btnTo.setText(formatTime12h(h, m));
                }, toTime[0], toTime[1], false).show());

        new AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setView(container)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName     = etName.getText().toString().trim();
                    String newEmail    = etEmail.getText().toString().trim();
                    String newPhone    = etPhone.getText().toString().trim();
                    String newShopName = etShopName.getText().toString().trim();
                    String newHours    = formatTime12h(fromTime[0], fromTime[1])
                            + " \u2013 " + formatTime12h(toTime[0], toTime[1]);
                    String uid         = session.getUserId();

                    if (newName.isEmpty()) {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // ── Immediate UI update ───────────────────────────
                    if (tvOwnerName != null) tvOwnerName.setText(newName);
                    if (tvPhone     != null) tvPhone.setText(newPhone.isEmpty() ? "Not set" : newPhone);
                    if (!newEmail.isEmpty() && tvEmail != null) tvEmail.setText(newEmail);
                    if (!newShopName.isEmpty()) {
                        if (tvProfileShopName != null) tvProfileShopName.setText(newShopName);
                        if (tvShopNameDetail  != null) tvShopNameDetail.setText(newShopName);
                        session.saveShopName(newShopName);
                    }
                    if (tvOpeningHours != null) tvOpeningHours.setText(newHours);
                    session.saveUserName(newName);
                    session.saveUserPhone(newPhone);
                    if (!newEmail.isEmpty()) session.saveUserEmail(newEmail);

                    // ── Persist to Firestore users/{uid} in one call ──
                    String currentLocation = safeText(tvShopLocation);
                    authRepository.updateFullProfile(
                            uid, newName, newPhone, newEmail,
                            newShopName, currentLocation, newHours,
                            new OperationCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(ProfilePage.this,
                                            "Profile updated", Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onError(Exception e) {
                                    Toast.makeText(ProfilePage.this,
                                            "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
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

    // ── Opening-hours helpers ─────────────────────────────────────────────

    private String formatTime12h(int hour, int minute) {
        String amPm = hour < 12 ? "AM" : "PM";
        int h = hour == 0 ? 12 : (hour > 12 ? hour - 12 : hour);
        return String.format(Locale.US, "%02d:%02d %s", h, minute, amPm);
    }

    private int[] parseHoursTime(String hoursStr, boolean isFrom) {
        int[] defaults = isFrom ? new int[]{8, 0} : new int[]{18, 0};
        if (hoursStr == null || hoursStr.isEmpty()) return defaults;
        String[] parts = hoursStr.split("[\u2013\u2014\\-]");
        if (parts.length < 2) return defaults;
        String part = isFrom ? parts[0].trim() : parts[parts.length - 1].trim();
        try {
            boolean isPM = part.toUpperCase().contains("PM");
            boolean isAM = part.toUpperCase().contains("AM");
            part = part.replaceAll("(?i)(am|pm)", "").trim();
            String[] t = part.split(":");
            int h = Integer.parseInt(t[0].trim());
            int m = t.length > 1 ? Integer.parseInt(t[1].trim().replaceAll("[^0-9]", "")) : 0;
            if (isPM && h < 12) h += 12;
            if (isAM && h == 12) h = 0;
            return new int[]{h, m};
        } catch (Exception ignored) {
            return defaults;
        }
    }

    private void addSectionLabel(LinearLayout parent, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(13f);
        tv.setTextColor(ContextCompat.getColor(this, R.color.text_dark_primary));
        tv.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(14);
        parent.addView(tv, lp);
    }

    private void addSmallLabel(LinearLayout parent, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(12f);
        tv.setTextColor(ContextCompat.getColor(this, R.color.text_dark_secondary));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(8);
        parent.addView(tv, lp);
    }

    private TextView addTimeButton(LinearLayout parent, int hour, int minute) {
        TextView tv = new TextView(this);
        tv.setText(formatTime12h(hour, minute));
        tv.setTextSize(15f);
        tv.setTextColor(ContextCompat.getColor(this, R.color.text_dark_primary));
        tv.setBackgroundColor(ContextCompat.getColor(this, R.color.bg_light_grey));
        tv.setPadding(dp(12), dp(12), dp(12), dp(12));
        tv.setClickable(true);
        tv.setFocusable(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(4);
        parent.addView(tv, lp);
        return tv;
    }

    private String safeText(TextView tv) {
        if (tv == null) return "";
        String t = tv.getText().toString().trim();
        return (t.equals("Not set") || t.equals("\u2014") || t.isEmpty()) ? "" : t;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}

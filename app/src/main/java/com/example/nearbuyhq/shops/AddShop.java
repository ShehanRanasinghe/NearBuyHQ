package com.example.nearbuyhq.shops;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.SessionManager;
import com.example.nearbuyhq.core.firebase.FirebaseConfig;
import com.example.nearbuyhq.dashboard.Analytics;
import com.example.nearbuyhq.dashboard.Dashboard;
import com.example.nearbuyhq.data.repository.OperationCallback;
import com.example.nearbuyhq.data.repository.ShopRepository;
import com.example.nearbuyhq.orders.Order_List;
import com.example.nearbuyhq.products.Inventory;
import com.example.nearbuyhq.settings.ProfilePage;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

/**
 * AddShop – lets the shop owner register a new branch.
 *
 * What this screen does:
 *  1. Collects basic shop info (name, owner, address, contact, category)
 *  2. Reads the logged-in user's UID from SessionManager and saves it as ownerUid
 *  3. After Firestore save succeeds, automatically requests GPS coordinates
 *     and updates the shop document with latitude/longitude
 *  4. The customer app reads latitude/longitude to calculate distance
 */
public class AddShop extends AppCompatActivity {

    // Request code for the runtime location permission dialog
    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private EditText shopName, shopOwner, shopAddress, shopContact;
    private Spinner  shopCategory;
    private TextView btnSubmit, btnCancel;
    private ImageView btnBack;

    private ShopRepository shopRepository;
    private FusedLocationProviderClient fusedLocationClient;

    // Saved shopId after Firestore write – used to attach GPS coordinates
    private String savedShopId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_shop);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Google's Fused Location API gives the best available GPS fix
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        shopRepository      = new ShopRepository();

        bindViews();
        setupCategorySpinner();
        setupButtons();
        setupBottomNavigation();
    }

    // ── View Binding ──────────────────────────────────────────────────────

    private void bindViews() {
        shopName     = findViewById(R.id.shopName);
        shopOwner    = findViewById(R.id.shopOwner);
        shopAddress  = findViewById(R.id.shopAddress);
        shopContact  = findViewById(R.id.shopContact);
        shopCategory = findViewById(R.id.shopCategory);
        btnSubmit    = findViewById(R.id.btnSubmit);
        btnCancel    = findViewById(R.id.btnCancel);
        btnBack      = findViewById(R.id.btn_back);
    }

    // ── Category Spinner ──────────────────────────────────────────────────

    private void setupCategorySpinner() {
        String[] categories = {
            "Grocery", "Electronics", "Clothing", "Books",
            "Cafe", "Fitness", "Restaurant", "Pharmacy"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shopCategory.setAdapter(adapter);
    }

    // ── Button Listeners ──────────────────────────────────────────────────

    private void setupButtons() {
        btnCancel.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> {
            if (validateInputs()) saveShop();
        });
    }

    // ── Save Shop to Firestore ─────────────────────────────────────────────

    private void saveShop() {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            Toast.makeText(this, "Firebase is disabled. Set FIREBASE_ENABLED=true in .env", Toast.LENGTH_LONG).show();
            return;
        }

        String name     = shopName.getText().toString().trim();
        String owner    = shopOwner.getText().toString().trim();
        String address  = shopAddress.getText().toString().trim();
        String contact  = shopContact.getText().toString().trim();
        String category = String.valueOf(shopCategory.getSelectedItem());
        long   now      = System.currentTimeMillis();

        // Read the Firebase Auth UID of the current user from session
        String ownerUid = SessionManager.getInstance(this).getUserId();

        // Build the shop object – lat/lng start at 0.0 and are updated after GPS fix
        Shop shop = new Shop(
                "",        // ID – auto-generated by Firestore
                name,
                owner,
                ownerUid,  // links this shop to the logged-in user
                address,
                category,
                "Active",
                contact,
                "",        // openingHours – editable from Profile page later
                "",        // website – editable from Profile page later
                0.0,       // latitude  – will be filled in by requestGpsAndUpdateShop()
                0.0,       // longitude – will be filled in by requestGpsAndUpdateShop()
                now,
                now
        );

        setSaving(true);
        shopRepository.createShop(shop, new OperationCallback() {
            @Override
            public void onSuccess() {
                // Shop saved – remember its ID so we can attach GPS coordinates
                savedShopId = shop.getId();

                // Update SessionManager so other screens see the new shopId immediately
                SessionManager.getInstance(AddShop.this).saveShopId(savedShopId);
                SessionManager.getInstance(AddShop.this).saveUserName(
                        SessionManager.getInstance(AddShop.this).getUserName());

                // Try to capture GPS in the background; this is non-blocking
                requestGpsAndUpdateShop(savedShopId);

                setSaving(false);
                Toast.makeText(AddShop.this, "Branch added successfully!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(Exception exception) {
                setSaving(false);
                Toast.makeText(AddShop.this,
                        "Failed to add branch: " + exception.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // ── GPS Capture ────────────────────────────────────────────────────────

    /**
     * Attempt to get the device's last known GPS position and save it to Firestore.
     * The customer app reads these coordinates to calculate shop distance.
     *
     * Flow:
     *  → If permission already granted → get location → update Firestore
     *  → If not granted → request permission → handled in onRequestPermissionsResult
     */
    private void requestGpsAndUpdateShop(String shopId) {
        boolean hasFine   = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)   == PackageManager.PERMISSION_GRANTED;
        boolean hasCoarse = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (hasFine || hasCoarse) {
            fetchLastLocationAndSave(shopId);
        } else {
            // Ask user for permission; result handled in onRequestPermissionsResult
            ActivityCompat.requestPermissions(this,
                    new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    /**
     * Read the device's last known location (fastest method, no delay) and
     * write lat/lng to the shop document in Firestore.
     */
    private void fetchLastLocationAndSave(String shopId) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        // Save coordinates to Firestore – customer app reads these
                        shopRepository.updateLocation(
                                shopId,
                                location.getLatitude(),
                                location.getLongitude(),
                                new OperationCallback() {
                                    @Override public void onSuccess() {
                                        // Silent success – shop is already saved
                                    }
                                    @Override public void onError(Exception e) {
                                        // Non-critical – owner can update location from Profile
                                    }
                                });
                    }
                    // If location is null, GPS was unavailable – coordinates remain 0.0
                    // Owner can update them later from the Profile page
                });
    }

    // ── Permission Result ─────────────────────────────────────────────────

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && savedShopId != null) {
            boolean granted = grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (granted) {
                // Permission was just granted – save GPS now
                fetchLastLocationAndSave(savedShopId);
            }
            // If denied, location stays 0.0 – can be updated from Profile page
        }
    }

    // ── Validation ────────────────────────────────────────────────────────

    private boolean validateInputs() {
        if (shopName.getText().toString().trim().isEmpty()) {
            shopName.setError("Branch name is required");
            return false;
        }
        if (shopOwner.getText().toString().trim().isEmpty()) {
            shopOwner.setError("Owner name is required");
            return false;
        }
        if (shopAddress.getText().toString().trim().isEmpty()) {
            shopAddress.setError("Address is required");
            return false;
        }
        if (shopContact.getText().toString().trim().isEmpty()) {
            shopContact.setError("Contact number is required");
            return false;
        }
        return true;
    }

    // ── Saving State ──────────────────────────────────────────────────────

    private void setSaving(boolean saving) {
        btnSubmit.setEnabled(!saving);
        btnSubmit.setAlpha(saving ? 0.6f : 1f);
        btnCancel.setEnabled(!saving);
        btnBack.setEnabled(!saving);
        btnSubmit.setText(saving ? "Saving..." : "Add Branch");
    }

    // ── Bottom Navigation ─────────────────────────────────────────────────

    private void setupBottomNavigation() {
        findViewById(R.id.navDashboard).setOnClickListener(v -> {
            startActivity(new Intent(this, Dashboard.class));
            finish();
        });
        findViewById(R.id.navProducts).setOnClickListener(v -> {
            startActivity(new Intent(this, Inventory.class));
            finish();
        });
        findViewById(R.id.navOrders).setOnClickListener(v -> {
            startActivity(new Intent(this, Order_List.class));
            finish();
        });
        findViewById(R.id.navAnalytics).setOnClickListener(v -> {
            startActivity(new Intent(this, Analytics.class));
            finish();
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfilePage.class));
            finish();
        });
    }
}

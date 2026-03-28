package com.example.nearbuyhq.products;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.SessionManager;
import com.example.nearbuyhq.data.repository.DataCallback;
import com.example.nearbuyhq.data.repository.ProductRepository;
import com.example.nearbuyhq.dashboard.Analytics;
import com.example.nearbuyhq.dashboard.Dashboard;
import com.example.nearbuyhq.orders.Order_List;
import com.example.nearbuyhq.settings.ProfilePage;

import java.util.ArrayList;
import java.util.List;

public class Inventory extends AppCompatActivity {

    private static final int LOW_STOCK_THRESHOLD = 10;

    private RecyclerView recyclerInventory;
    private InventoryAdapter adapter;
    private EditText etSearch;
    private ImageView ivClearSearch, btnBack;
    private TextView chipAll, chipLowStock, chipAvailable;
    private LinearLayout layoutLowStockWarning;
    private TextView tvWarningMessage;
    private LinearLayout btnViewProducts, btnAddProduct;
    private LinearLayout navDashboard, navProducts, navOrders, navAnalytics, navProfile;
    private ImageView navDashboardIcon, navProductsIcon, navOrdersIcon, navAnalyticsIcon, navProfileIcon;
    private TextView navDashboardText, navProductsText, navOrdersText, navAnalyticsText, navProfileText;

    private List<InventoryItem> allItems;
    private String currentFilter = "All";
    private ProductRepository productRepository;

    // ── DATA MODEL ─────────────────────────────────────────────────────────
    static class InventoryItem {
        String productId;
        String name;
        String brand;
        String category;
        String unit;
        String description;
        double price;
        long   createdAt;
        int    currentStock;
        int    totalStock;
        int    iconRes;

        InventoryItem(String productId, String name, String brand, String category,
                      String unit, String description, double price, long createdAt,
                      int currentStock, int totalStock, int iconRes) {
            this.productId   = productId;
            this.name        = name;
            this.brand       = brand;
            this.category    = category;
            this.unit        = unit;
            this.description = description;
            this.price       = price;
            this.createdAt   = createdAt;
            this.currentStock = currentStock;
            this.totalStock   = totalStock;
            this.iconRes      = iconRes;
        }

        boolean isLowStock(int threshold) {
            return currentStock > 0 && currentStock <= threshold;
        }

        boolean isOutOfStock() {
            return currentStock == 0;
        }

        int getStockPercentage() {
            if (totalStock == 0) return 0;
            return Math.min(100, (currentStock * 100) / totalStock);
        }
    }

    // ── LIFECYCLE ──────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.deep_blue));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.bg_white));

        setContentView(R.layout.activity_inventory);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        initViews();
        productRepository = new ProductRepository();
        allItems = new ArrayList<>();
        showLowStockWarning();
        setupRecyclerView();
        setupSearch();
        setupFilterChips();
        setupBackButton();
        setupNavigationButtons();
        loadInventory();
    }

    // ── INIT ───────────────────────────────────────────────────────────────
    private void initViews() {
        recyclerInventory      = findViewById(R.id.recyclerInventory);
        etSearch               = findViewById(R.id.etSearch);
        ivClearSearch          = findViewById(R.id.ivClearSearch);
        btnBack                = findViewById(R.id.btn_back);
        chipAll                = findViewById(R.id.chipAll);
        chipLowStock           = findViewById(R.id.chipLowStock);
        chipAvailable          = findViewById(R.id.chipAvailable);
        layoutLowStockWarning  = findViewById(R.id.layoutLowStockWarning);
        tvWarningMessage       = findViewById(R.id.tvWarningMessage);
        btnViewProducts        = findViewById(R.id.btnViewProducts);
        btnAddProduct          = findViewById(R.id.btnAddProduct);

        // Bottom navigation
        navDashboard           = findViewById(R.id.navDashboard);
        navProducts            = findViewById(R.id.navProducts);
        navOrders              = findViewById(R.id.navOrders);
        navAnalytics           = findViewById(R.id.navAnalytics);
        navProfile             = findViewById(R.id.navProfile);

        navDashboardIcon       = findViewById(R.id.navDashboardIcon);
        navProductsIcon        = findViewById(R.id.navProductsIcon);
        navOrdersIcon          = findViewById(R.id.navOrdersIcon);
        navAnalyticsIcon       = findViewById(R.id.navAnalyticsIcon);
        navProfileIcon         = findViewById(R.id.navProfileIcon);

        navDashboardText       = findViewById(R.id.navDashboardText);
        navProductsText        = findViewById(R.id.navProductsText);
        navOrdersText          = findViewById(R.id.navOrdersText);
        navAnalyticsText       = findViewById(R.id.navAnalyticsText);
        navProfileText         = findViewById(R.id.navProfileText);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInventory();
    }

    // ── LOAD FROM FIREBASE ─────────────────────────────────────────────────
    private void loadInventory() {
        String userId = SessionManager.getInstance(this).getUserId();
        productRepository.getProductsByShopId(userId, "All", new DataCallback<List<ProductItem>>() {
            @Override
            public void onSuccess(List<ProductItem> data) {
                allItems.clear();
                for (ProductItem item : data) {
                    int current = item.getQuantity();
                    // Use max of current stock or 50 as total so the progress bar is meaningful
                    int total = Math.max(current, 50);
                    String brand = (item.getDescription() != null && !item.getDescription().isEmpty())
                            ? item.getDescription() : item.getCategory();
                    allItems.add(new InventoryItem(
                            item.getId(),
                            item.getName(),
                            brand,
                            item.getCategory(),
                            item.getUnit(),
                            item.getDescription() != null ? item.getDescription() : "",
                            item.getPrice(),
                            item.getCreatedAt(),
                            current,
                            total,
                            R.drawable.ic_eco_leaf
                    ));
                }
                runOnUiThread(() -> {
                    adapter.updateList(allItems);
                    showLowStockWarning();
                });
            }

            @Override
            public void onError(Exception exception) {
                runOnUiThread(() -> {
                    allItems.clear();
                    adapter.updateList(allItems);
                    showLowStockWarning();
                    Toast.makeText(Inventory.this,
                            "Could not load inventory", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // ── WARNING BANNER ─────────────────────────────────────────────────────
    private void showLowStockWarning() {
        int lowCount = 0;
        for (InventoryItem item : allItems) {
            if (item.isLowStock(LOW_STOCK_THRESHOLD)) lowCount++;
        }
        if (lowCount > 0) {
            layoutLowStockWarning.setVisibility(View.VISIBLE);
            tvWarningMessage.setText(lowCount + " item" + (lowCount > 1 ? "s are" : " is")
                    + " running low on stock. Restock soon!");
        } else {
            layoutLowStockWarning.setVisibility(View.GONE);
        }
    }

    // ── RECYCLERVIEW ───────────────────────────────────────────────────────
    private void setupRecyclerView() {
        adapter = new InventoryAdapter(this, allItems, LOW_STOCK_THRESHOLD);
        adapter.setOnItemClickListener(item -> showProductDetails(item));
        recyclerInventory.setLayoutManager(new LinearLayoutManager(this));
        recyclerInventory.setAdapter(adapter);
    }

    // ── SEARCH ─────────────────────────────────────────────────────────────
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ivClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                applyFilter(s.toString(), currentFilter);
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        ivClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            ivClearSearch.setVisibility(View.GONE);
        });
    }

    // ── FILTER CHIPS ───────────────────────────────────────────────────────
    private void setupFilterChips() {
        chipAll.setOnClickListener(v       -> setFilter("All"));
        chipLowStock.setOnClickListener(v  -> setFilter("Low Stock"));
        chipAvailable.setOnClickListener(v -> setFilter("Out of Stock"));
    }

    private void setFilter(String filter) {
        currentFilter = filter;
        updateChipStyles(filter);
        applyFilter(etSearch.getText().toString(), filter);
    }

    private void updateChipStyles(String active) {
        int inactiveColor = Color.parseColor("#B0CDD5");

        chipAll.setBackgroundResource(R.drawable.bg_chip_inactive);
        chipLowStock.setBackgroundResource(R.drawable.bg_chip_inactive);
        chipAvailable.setBackgroundResource(R.drawable.bg_chip_inactive);
        chipAll.setTextColor(inactiveColor);
        chipLowStock.setTextColor(inactiveColor);
        chipAvailable.setTextColor(inactiveColor);
        chipAll.setTypeface(null);
        chipLowStock.setTypeface(null);
        chipAvailable.setTypeface(null);

        TextView activeChip;
        switch (active) {
            case "Low Stock":    activeChip = chipLowStock; break;
            case "Out of Stock": activeChip = chipAvailable; break;
            default:             activeChip = chipAll;       break;
        }
        activeChip.setBackgroundResource(R.drawable.bg_chip_active);
        activeChip.setTextColor(Color.WHITE);
        activeChip.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    // ── FILTER LOGIC ───────────────────────────────────────────────────────
    private void applyFilter(String query, String statusFilter) {
        String q = query.toLowerCase().trim();
        List<InventoryItem> filtered = new ArrayList<>();

        for (InventoryItem item : allItems) {
            // Search by product name, brand, OR category
            boolean matchSearch = q.isEmpty()
                    || item.name.toLowerCase().contains(q)
                    || item.brand.toLowerCase().contains(q)
                    || item.category.toLowerCase().contains(q);

            boolean matchStatus;
            switch (statusFilter) {
                case "Low Stock":    matchStatus = item.isLowStock(LOW_STOCK_THRESHOLD); break;
                case "Out of Stock": matchStatus = item.isOutOfStock();                  break;
                default:             matchStatus = true;                                  break;
            }

            if (matchSearch && matchStatus) filtered.add(item);
        }
        adapter.updateList(filtered);
    }

    // ── BACK ───────────────────────────────────────────────────────────────
    private void setupBackButton() {
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    // ── NAVIGATION BUTTONS ─────────────────────────────────────────────────
    private void setupNavigationButtons() {
        btnViewProducts.setOnClickListener(v -> {
            Intent intent = new Intent(Inventory.this, Products_List.class);
            startActivity(intent);
        });

        btnAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(Inventory.this, Add_Product.class);
            startActivity(intent);
        });

        navDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(Inventory.this, Dashboard.class);
            startActivity(intent);
            finish();
        });

        // Already on Inventory screen.
        navProducts.setOnClickListener(v -> {
        });

        navOrders.setOnClickListener(v -> {
            Intent intent = new Intent(Inventory.this, Order_List.class);
            startActivity(intent);
            finish();
        });

        navAnalytics.setOnClickListener(v -> {
            Intent intent = new Intent(Inventory.this, Analytics.class);
            startActivity(intent);
            finish();
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Inventory.this, ProfilePage.class);
            startActivity(intent);
            finish();
        });
    }

    // ── PRODUCT DETAILS ────────────────────────────────────────────────────
    private void showProductDetails(InventoryItem item) {
        Intent intent = new Intent(Inventory.this, Product_Details.class);
        intent.putExtra("product_id",          item.productId);
        intent.putExtra("product_name",        item.name);
        intent.putExtra("product_description", item.description);
        intent.putExtra("product_category",    item.category);
        intent.putExtra("product_price",       item.price);
        intent.putExtra("product_unit",        item.unit);
        intent.putExtra("product_quantity",    item.currentStock);
        intent.putExtra("product_created_at",  item.createdAt);
        startActivity(intent);
    }
}

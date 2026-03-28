package com.example.nearbuyhq.products;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.SessionManager;
import com.example.nearbuyhq.dashboard.Analytics;
import com.example.nearbuyhq.dashboard.Dashboard;
import com.example.nearbuyhq.data.repository.OperationCallback;
import com.example.nearbuyhq.data.repository.ProductRepository;
import com.example.nearbuyhq.orders.Order_List;
import com.example.nearbuyhq.settings.ProfilePage;

// Add/Edit product screen – validates form inputs, assigns a category, then saves to NearBuyHQ/{userId}/products.
public class Add_Product extends AppCompatActivity {

    private EditText etProductName;
    private EditText etProductDescription;
    private EditText etPrice;
    private EditText etUnit;
    private EditText etStock;
    private TextView btnSave;
    private TextView btnCancel;
    private TextView catVegetables;
    private TextView catFruits;
    private TextView catGrains;
    private TextView catMeats;
    private TextView catSeafood;
    private TextView catDairy;
    private TextView catBakery;
    private TextView catBeverages;
    private TextView catSnacks;
    private TextView catSpices;
    private TextView catCleaning;
    private TextView catPersonal;

    private ProductRepository productRepository;
    private String selectedCategory = "Vegetables";
    private boolean editMode;
    private String editingProductId;
    private long originalCreatedAt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        productRepository = new ProductRepository();
        bindViews();
        setupCategories();
        setupActions();
        setupBottomNavigation();
        bindEditPayload();
    }

    private void bindViews() {
        etProductName = findViewById(R.id.et_product_name);
        etProductDescription = findViewById(R.id.et_product_description);
        etPrice = findViewById(R.id.et_price);
        etUnit = findViewById(R.id.et_unit);
        etStock = findViewById(R.id.et_stock);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        catVegetables = findViewById(R.id.btn_cat_vegetables);
        catFruits     = findViewById(R.id.btn_cat_fruits);
        catGrains     = findViewById(R.id.btn_cat_grains);
        catMeats      = findViewById(R.id.btn_cat_meats);
        catSeafood    = findViewById(R.id.btn_cat_seafood);
        catDairy      = findViewById(R.id.btn_cat_dairy);
        catBakery     = findViewById(R.id.btn_cat_bakery);
        catBeverages  = findViewById(R.id.btn_cat_beverages);
        catSnacks     = findViewById(R.id.btn_cat_snacks);
        catSpices     = findViewById(R.id.btn_cat_spices);
        catCleaning   = findViewById(R.id.btn_cat_cleaning);
        catPersonal   = findViewById(R.id.btn_cat_personal);
    }

    private void setupActions() {
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProduct());
    }

    private void setupCategories() {
        catVegetables.setOnClickListener(v -> { selectedCategory = "Vegetables";   updateCategoryStyles(); });
        catFruits    .setOnClickListener(v -> { selectedCategory = "Fruits";       updateCategoryStyles(); });
        catGrains    .setOnClickListener(v -> { selectedCategory = "Grains";       updateCategoryStyles(); });
        catMeats     .setOnClickListener(v -> { selectedCategory = "Meat";         updateCategoryStyles(); });
        catSeafood   .setOnClickListener(v -> { selectedCategory = "Seafood";      updateCategoryStyles(); });
        catDairy     .setOnClickListener(v -> { selectedCategory = "Dairy";        updateCategoryStyles(); });
        catBakery    .setOnClickListener(v -> { selectedCategory = "Bakery";       updateCategoryStyles(); });
        catBeverages .setOnClickListener(v -> { selectedCategory = "Beverages";    updateCategoryStyles(); });
        catSnacks    .setOnClickListener(v -> { selectedCategory = "Snacks";       updateCategoryStyles(); });
        catSpices    .setOnClickListener(v -> { selectedCategory = "Spices";       updateCategoryStyles(); });
        catCleaning  .setOnClickListener(v -> { selectedCategory = "Cleaning";     updateCategoryStyles(); });
        catPersonal  .setOnClickListener(v -> { selectedCategory = "Personal Care";updateCategoryStyles(); });
        updateCategoryStyles();
    }

    private void updateCategoryStyles() {
        TextView[] all = { catVegetables, catFruits, catGrains, catMeats,
                           catSeafood, catDairy, catBakery, catBeverages,
                           catSnacks, catSpices, catCleaning, catPersonal };
        String[]   keys = { "Vegetables", "Fruits", "Grains", "Meat",
                             "Seafood", "Dairy", "Bakery", "Beverages",
                             "Snacks", "Spices", "Cleaning", "Personal Care" };
        for (int i = 0; i < all.length; i++) {
            if (all[i] == null) continue;
            all[i].setBackgroundResource(keys[i].equals(selectedCategory)
                    ? R.drawable.bg_status_delivered : R.drawable.bg_cat_inactive);
        }
    }

    private void saveProduct() {

        String name = etProductName.getText().toString().trim();
        String description = etProductDescription.getText().toString().trim();
        String unit = etUnit.getText().toString().trim();
        String priceText = etPrice.getText().toString().trim();
        String stockText = etStock.getText().toString().trim();

        if (name.isEmpty()) {
            etProductName.setError("Product name is required");
            return;
        }
        if (description.isEmpty()) {
            etProductDescription.setError("Description is required");
            return;
        }
        if (unit.isEmpty()) {
            etUnit.setError("Unit is required");
            return;
        }
        if (priceText.isEmpty()) {
            etPrice.setError("Price is required");
            return;
        }
        if (stockText.isEmpty()) {
            etStock.setError("Stock is required");
            return;
        }

        double price;
        int stock;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException exception) {
            etPrice.setError("Invalid price");
            return;
        }
        try {
            stock = Integer.parseInt(stockText);
        } catch (NumberFormatException exception) {
            etStock.setError("Invalid stock");
            return;
        }

        // userId == shopId — one account = one shop
        String userId = SessionManager.getInstance(this).getUserId();

        long now = System.currentTimeMillis();
        long createdAt = editMode ? originalCreatedAt : now;
        ProductItem item = new ProductItem(
                editMode ? editingProductId : "",
                userId,
                name,
                description,
                selectedCategory,
                unit,
                price,
                stock,
                ProductItem.resolveStatus(stock),
                createdAt,
                now
        );

        setSaving(true);
        OperationCallback callback = new OperationCallback() {
            @Override
            public void onSuccess() {
                setSaving(false);
                Toast.makeText(Add_Product.this, editMode ? "Product updated" : "Product saved", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(Exception exception) {
                setSaving(false);
                Toast.makeText(Add_Product.this, "Failed to save: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        if (editMode) {
            productRepository.updateProduct(item, callback);
        } else {
            productRepository.createProduct(item, callback);
        }
    }

    private void setSaving(boolean saving) {
        btnSave.setEnabled(!saving);
        btnSave.setAlpha(saving ? 0.6f : 1f);
        btnCancel.setEnabled(!saving);
        if (saving) {
            btnSave.setText("Saving...");
        } else {
            btnSave.setText(editMode ? "Update Product" : "Save Product");
        }
    }

    private void bindEditPayload() {
        editMode = getIntent().getBooleanExtra("is_edit", false);
        if (!editMode) {
            originalCreatedAt = System.currentTimeMillis();
            return;
        }

        editingProductId = getIntent().getStringExtra("product_id");
        originalCreatedAt = getIntent().getLongExtra("product_created_at", System.currentTimeMillis());

        etProductName.setText(getIntent().getStringExtra("product_name"));
        etProductDescription.setText(getIntent().getStringExtra("product_description"));
        etPrice.setText(String.valueOf(getIntent().getDoubleExtra("product_price", 0d)));
        etUnit.setText(getIntent().getStringExtra("product_unit"));
        etStock.setText(String.valueOf(getIntent().getIntExtra("product_quantity", 0)));

        String incomingCategory = getIntent().getStringExtra("product_category");
        if (incomingCategory != null && !incomingCategory.trim().isEmpty()) {
            selectedCategory = incomingCategory;
        }
        updateCategoryStyles();
        btnSave.setText("Update Product");
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navDashboard).setOnClickListener(v -> {
            startActivity(new Intent(this, Dashboard.class));
            finish();
        });
        findViewById(R.id.navProducts).setOnClickListener(v -> {
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
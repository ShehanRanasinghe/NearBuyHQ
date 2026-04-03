package com.example.nearbuyhq.products;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.SessionManager;
import com.example.nearbuyhq.dashboard.Analytics;
import com.example.nearbuyhq.dashboard.Dashboard;
import com.example.nearbuyhq.data.repository.OperationCallback;
import com.example.nearbuyhq.data.repository.ProductRepository;
import com.example.nearbuyhq.data.repository.SupabaseStorageHelper;
import com.example.nearbuyhq.orders.Order_List;
import com.example.nearbuyhq.settings.ProfilePage;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Add/Edit product screen.
 *
 * Image flow:
 *  1. User taps the 📷 upload box → gallery picker opens
 *  2. Selected image is shown as local preview immediately
 *  3. Image is compressed (JPEG 85%) and uploaded to Supabase Storage in background
 *  4. On success → pendingImageUrl is set to the Supabase public URL
 *  5. When user taps Save → ProductItem is built with the imageUrl
 *  6. ProductRepository saves to Firebase (existing behavior, now includes imageUrl)
 */
public class Add_Product extends AppCompatActivity {

    // ── Form fields ───────────────────────────────────────────────────────────
    private EditText etProductName;
    private EditText etProductDescription;
    private EditText etPrice;
    private EditText etUnit;
    private EditText etStock;
    private TextView btnSave;
    private TextView btnCancel;

    // ── Category TextViews ────────────────────────────────────────────────────
    private TextView catVegetables, catFruits, catGrains, catMeats;
    private TextView catSeafood, catDairy, catBakery, catBeverages;
    private TextView catSnacks, catSpices, catCleaning, catPersonal;

    // ── Image upload views (already in your XML) ──────────────────────────────
    private FrameLayout  layoutImageUpload;   // id: layout_image_upload
    private LinearLayout uploadPlaceholder;   // id: upload_placeholder
    private ImageView    imgProductPreview;   // id: img_product_preview
    private ProgressBar  uploadProgress;      // id: upload_progress

    // ── Repositories / helpers ────────────────────────────────────────────────
    private ProductRepository     productRepository;
    private SupabaseStorageHelper supabaseStorage;

    // ── State ─────────────────────────────────────────────────────────────────
    private final Handler mainHandler    = new Handler(Looper.getMainLooper());
    private String  selectedCategory     = "Vegetables";
    private String  pendingImageUrl      = "";      // Supabase URL after successful upload
    private boolean imageUploadInProgress = false;  // blocks Save while uploading

    // Edit mode state
    private boolean editMode;
    private String  editingProductId;
    private long    originalCreatedAt;

    // ── Image picker (modern Activity Result API) ─────────────────────────────
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null
                                && result.getData().getData() != null) {
                            handlePickedImage(result.getData().getData());
                        }
                    }
            );

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        // Back button
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Init repositories
        productRepository = new ProductRepository();
        supabaseStorage   = new SupabaseStorageHelper();

        // Setup UI
        bindViews();
        setupImageUpload();   // ← NEW
        setupCategories();
        setupActions();
        setupBottomNavigation();
        bindEditPayload();
    }

    // ── 1. Bind all views ─────────────────────────────────────────────────────

    private void bindViews() {
        etProductName        = findViewById(R.id.et_product_name);
        etProductDescription = findViewById(R.id.et_product_description);
        etPrice              = findViewById(R.id.et_price);
        etUnit               = findViewById(R.id.et_unit);
        etStock              = findViewById(R.id.et_stock);
        btnSave              = findViewById(R.id.btn_save);
        btnCancel            = findViewById(R.id.btn_cancel);

        // Image upload views
        layoutImageUpload = findViewById(R.id.layout_image_upload);
        uploadPlaceholder = findViewById(R.id.upload_placeholder);
        imgProductPreview = findViewById(R.id.img_product_preview);
        uploadProgress    = findViewById(R.id.upload_progress);

        // Category buttons
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

    // ── 2. Image upload setup ─────────────────────────────────────────────────

    private void setupImageUpload() {
        // The entire FrameLayout (📷 box) is clickable — opens gallery
        layoutImageUpload.setOnClickListener(v -> openImagePicker());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    /**
     * Called when user selects an image from gallery.
     *
     * Step A: Show local bitmap preview instantly (no network wait)
     * Step B: Compress bitmap to JPEG bytes
     * Step C: Upload bytes to Supabase Storage on background thread
     * Step D: On success → store public URL in pendingImageUrl
     */
    private void handlePickedImage(Uri imageUri) {
        try {
            // ── Step A: Local preview ────────────────────────────────────────
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Show the preview, hide the placeholder
            uploadPlaceholder.setVisibility(View.GONE);
            imgProductPreview.setVisibility(View.VISIBLE);
            imgProductPreview.setImageBitmap(bitmap);

            // ── Step B: Compress to JPEG ─────────────────────────────────────
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            // 85 quality = good visual quality with reasonable file size
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteStream);
            byte[] imageBytes = byteStream.toByteArray();

            // ── Step C: Upload to Supabase ───────────────────────────────────
            // File name: products/{userId}_{timestamp}.jpg
            // This ensures each product gets a unique file path
            String userId   = SessionManager.getInstance(this).getUserId();
            String fileName = "products/" + userId + "_" + System.currentTimeMillis() + ".jpg";

            setImageUploading(true);  // Show spinner, dim preview

            supabaseStorage.uploadImage(imageBytes, fileName, new SupabaseStorageHelper.UploadCallback() {

                @Override
                public void onSuccess(String publicUrl) {
                    // ── Step D: Store the URL ─────────────────────────────────
                    mainHandler.post(() -> {
                        pendingImageUrl = publicUrl;
                        setImageUploading(false);
                        Toast.makeText(Add_Product.this,
                                "✓ Image ready", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onError(Exception e) {
                    mainHandler.post(() -> {
                        setImageUploading(false);
                        // Revert UI — let user try again
                        imgProductPreview.setVisibility(View.GONE);
                        uploadPlaceholder.setVisibility(View.VISIBLE);
                        pendingImageUrl = "";
                        Toast.makeText(Add_Product.this,
                                "Image upload failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Could not open image: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Toggles the uploading state:
     * - Shows/hides the ProgressBar spinner
     * - Dims/restores the preview image alpha
     * - Sets imageUploadInProgress flag (used to block Save)
     */
    private void setImageUploading(boolean uploading) {
        imageUploadInProgress = uploading;
        uploadProgress.setVisibility(uploading ? View.VISIBLE : View.GONE);
        imgProductPreview.setAlpha(uploading ? 0.4f : 1.0f);
    }

    // ── 3. Save product ───────────────────────────────────────────────────────

    private void setupActions() {
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProduct());
    }

    private void saveProduct() {
        // Block save while image upload is still in progress
        if (imageUploadInProgress) {
            Toast.makeText(this,
                    "Please wait — image is still uploading…",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // ── Validate inputs ───────────────────────────────────────────────────
        String name        = etProductName.getText().toString().trim();
        String description = etProductDescription.getText().toString().trim();
        String unit        = etUnit.getText().toString().trim();
        String priceText   = etPrice.getText().toString().trim();
        String stockText   = etStock.getText().toString().trim();

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
        } catch (NumberFormatException e) {
            etPrice.setError("Invalid price");
            return;
        }
        try {
            stock = Integer.parseInt(stockText);
        } catch (NumberFormatException e) {
            etStock.setError("Invalid stock");
            return;
        }

        // ── Build ProductItem ─────────────────────────────────────────────────
        String userId  = SessionManager.getInstance(this).getUserId();
        long   now     = System.currentTimeMillis();
        long   created = editMode ? originalCreatedAt : now;

        // pendingImageUrl will be "" if no image was selected — that's fine
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
                pendingImageUrl,   // ← image URL from Supabase
                created,
                now
        );

        // ── Save via ProductRepository ────────────────────────────────────────
        setSaving(true);

        OperationCallback callback = new OperationCallback() {
            @Override
            public void onSuccess() {
                setSaving(false);
                Toast.makeText(Add_Product.this,
                        editMode ? "Product updated" : "Product saved",
                        Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(Exception e) {
                setSaving(false);
                Toast.makeText(Add_Product.this,
                        "Failed to save: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
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
        btnSave.setText(saving ? "Saving…"
                : (editMode ? "Update Product" : "Save Product"));
    }

    // ── 4. Edit mode — pre-fill form ──────────────────────────────────────────

    private void bindEditPayload() {
        editMode = getIntent().getBooleanExtra("is_edit", false);

        if (!editMode) {
            originalCreatedAt = System.currentTimeMillis();
            return;
        }

        // Pre-fill all text fields
        editingProductId  = getIntent().getStringExtra("product_id");
        originalCreatedAt = getIntent().getLongExtra("product_created_at", System.currentTimeMillis());

        etProductName.setText(getIntent().getStringExtra("product_name"));
        etProductDescription.setText(getIntent().getStringExtra("product_description"));
        etPrice.setText(String.valueOf(getIntent().getDoubleExtra("product_price", 0d)));
        etUnit.setText(getIntent().getStringExtra("product_unit"));
        etStock.setText(String.valueOf(getIntent().getIntExtra("product_quantity", 0)));

        // Pre-select category
        String incomingCategory = getIntent().getStringExtra("product_category");
        if (incomingCategory != null && !incomingCategory.trim().isEmpty()) {
            selectedCategory = incomingCategory;
        }

        // ── Load existing image from Supabase URL ─────────────────────────────
        // Pass "product_image_url" extra when launching Add_Product in edit mode
        String existingImageUrl = getIntent().getStringExtra("product_image_url");
        if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
            pendingImageUrl = existingImageUrl;  // keep the existing URL
            uploadPlaceholder.setVisibility(View.GONE);
            imgProductPreview.setVisibility(View.VISIBLE);

            // Glide loads from URL and caches it
            Glide.with(this)
                    .load(existingImageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .centerCrop()
                    .into(imgProductPreview);
        }

        updateCategoryStyles();
        btnSave.setText("Update Product");
    }

    // ── 5. Category selection ─────────────────────────────────────────────────

    private void setupCategories() {
        catVegetables.setOnClickListener(v -> { selectedCategory = "Vegetables";    updateCategoryStyles(); });
        catFruits    .setOnClickListener(v -> { selectedCategory = "Fruits";        updateCategoryStyles(); });
        catGrains    .setOnClickListener(v -> { selectedCategory = "Grains";        updateCategoryStyles(); });
        catMeats     .setOnClickListener(v -> { selectedCategory = "Meat";          updateCategoryStyles(); });
        catSeafood   .setOnClickListener(v -> { selectedCategory = "Seafood";       updateCategoryStyles(); });
        catDairy     .setOnClickListener(v -> { selectedCategory = "Dairy";         updateCategoryStyles(); });
        catBakery    .setOnClickListener(v -> { selectedCategory = "Bakery";        updateCategoryStyles(); });
        catBeverages .setOnClickListener(v -> { selectedCategory = "Beverages";     updateCategoryStyles(); });
        catSnacks    .setOnClickListener(v -> { selectedCategory = "Snacks";        updateCategoryStyles(); });
        catSpices    .setOnClickListener(v -> { selectedCategory = "Spices";        updateCategoryStyles(); });
        catCleaning  .setOnClickListener(v -> { selectedCategory = "Cleaning";      updateCategoryStyles(); });
        catPersonal  .setOnClickListener(v -> { selectedCategory = "Personal Care"; updateCategoryStyles(); });
        updateCategoryStyles();
    }

    private void updateCategoryStyles() {
        TextView[] all  = { catVegetables, catFruits, catGrains, catMeats,
                catSeafood, catDairy, catBakery, catBeverages,
                catSnacks, catSpices, catCleaning, catPersonal };
        String[]   keys = { "Vegetables", "Fruits", "Grains", "Meat",
                "Seafood", "Dairy", "Bakery", "Beverages",
                "Snacks", "Spices", "Cleaning", "Personal Care" };
        for (int i = 0; i < all.length; i++) {
            if (all[i] == null) continue;
            all[i].setBackgroundResource(
                    keys[i].equals(selectedCategory)
                            ? R.drawable.bg_status_delivered
                            : R.drawable.bg_cat_inactive
            );
        }
    }

    // ── 6. Bottom navigation ──────────────────────────────────────────────────

    private void setupBottomNavigation() {
        findViewById(R.id.navDashboard).setOnClickListener(v -> {
            startActivity(new Intent(this, Dashboard.class));
            finish();
        });
        findViewById(R.id.navProducts).setOnClickListener(v -> {
            // Already on products screen — do nothing
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
package com.example.nearbuyhq.products;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.SessionManager;
import com.example.nearbuyhq.data.repository.DataCallback;
import com.example.nearbuyhq.data.repository.OperationCallback;
import com.example.nearbuyhq.data.repository.ProductRepository;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Products list screen – shows all products for the shop with search, category filter chips, and inline edit/delete support.
public class Products_List extends AppCompatActivity {

    private RecyclerView rvProducts;
    private EditText etSearch;
    private ImageView ivClearSearch;
    private TextView chipAll;
    private TextView chipVegetables;
    private TextView chipFruits;
    private TextView chipRice;
    private TextView chipSpices;
    private TextView chipDairy;
    private TextView tvTotalProducts;
    private TextView tvInStock;
    private TextView tvLowStock;
    private TextView tvOutOfStock;

    private ProductsListAdapter adapter;
    private ProductRepository productRepository;
    private final List<ProductItem> allProducts = new ArrayList<>();
    private String activeCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_products_list);

        bindViews();
        productRepository = new ProductRepository();
        setupList();
        setupHeaderActions();
        setupSearch();
        setupFilters();
        loadProducts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    private void bindViews() {
        rvProducts = findViewById(R.id.rvProducts);
        etSearch = findViewById(R.id.etSearch);
        ivClearSearch = findViewById(R.id.ivClearSearch);
        chipAll = findViewById(R.id.chipAll);
        chipVegetables = findViewById(R.id.chipVegetables);
        chipFruits = findViewById(R.id.chipFruits);
        chipRice = findViewById(R.id.chipRice);
        chipSpices = findViewById(R.id.chipSpices);
        chipDairy = findViewById(R.id.chipDairy);
        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvInStock = findViewById(R.id.tvInStock);
        tvLowStock = findViewById(R.id.tvLowStock);
        tvOutOfStock = findViewById(R.id.tvOutOfStock);
    }

    private void setupList() {
        adapter = new ProductsListAdapter(new ArrayList<>(), new ProductsListAdapter.ProductActionListener() {
            @Override
            public void onOpen(ProductItem item) {
                openDetails(item);
            }

            @Override
            public void onEdit(ProductItem item) {
                openDetails(item);
            }

            @Override
            public void onDelete(ProductItem item) {
                deleteProduct(item);
            }
        });
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(adapter);
    }

    private void setupHeaderActions() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        ExtendedFloatingActionButton fabAddProduct = findViewById(R.id.fabAddProduct);
        fabAddProduct.setOnClickListener(v -> startActivity(new Intent(this, Add_Product.class)));
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ivClearSearch.setVisibility(s.length() > 0 ? ImageView.VISIBLE : ImageView.GONE);
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ivClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            ivClearSearch.setVisibility(ImageView.GONE);
        });
    }

    private void setupFilters() {
        chipAll.setOnClickListener(v -> setCategory("All"));
        chipVegetables.setOnClickListener(v -> setCategory("Vegetables"));
        chipFruits.setOnClickListener(v -> setCategory("Fruits"));
        chipRice.setOnClickListener(v -> setCategory("Rice & Grains"));
        chipSpices.setOnClickListener(v -> setCategory("Spices"));
        chipDairy.setOnClickListener(v -> setCategory("Dairy"));
    }

    private void setCategory(String category) {
        activeCategory = category;
        applyFilters();
    }

    private void loadProducts() {
        String userId = SessionManager.getInstance(this).getUserId();
        productRepository.getProductsByShopId(userId, "All", new DataCallback<List<ProductItem>>() {
            @Override
            public void onSuccess(List<ProductItem> data) {
                allProducts.clear();
                allProducts.addAll(data);
                applyFilters();
            }

            @Override
            public void onError(Exception exception) {
                allProducts.clear();
                applyFilters();
                Toast.makeText(Products_List.this, "Could not load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        String q = etSearch.getText().toString().trim().toLowerCase(Locale.US);
        List<ProductItem> filtered = new ArrayList<>();
        for (ProductItem item : allProducts) {
            boolean categoryMatch = "All".equalsIgnoreCase(activeCategory)
                    || item.getCategory().equalsIgnoreCase(activeCategory);
            boolean searchMatch = q.isEmpty()
                    || item.getName().toLowerCase(Locale.US).contains(q)
                    || item.getCategory().toLowerCase(Locale.US).contains(q)
                    || item.getDescription().toLowerCase(Locale.US).contains(q);
            if (categoryMatch && searchMatch) {
                filtered.add(item);
            }
        }
        adapter.updateItems(filtered);
        updateStats(filtered);
    }

    private void updateStats(List<ProductItem> source) {
        int available = 0;
        int low = 0;
        int out = 0;
        for (ProductItem item : source) {
            if (item.isOutOfStock()) {
                out++;
            } else if (item.isLowStock(10)) {
                low++;
            } else {
                available++;
            }
        }
        tvTotalProducts.setText(String.valueOf(source.size()));
        tvInStock.setText(String.valueOf(available));
        tvLowStock.setText(String.valueOf(low));
        tvOutOfStock.setText(String.valueOf(out));
    }

    private void openDetails(ProductItem item) {
        Intent intent = new Intent(this, Product_Details.class);
        intent.putExtra("product_id",          item.getId());
        intent.putExtra("product_name",        item.getName());
        intent.putExtra("product_description", item.getDescription());
        intent.putExtra("product_category",    item.getCategory());
        intent.putExtra("product_price",       item.getPrice());
        intent.putExtra("product_unit",        item.getUnit());
        intent.putExtra("product_quantity",    item.getQuantity());
        intent.putExtra("product_created_at",  item.getCreatedAt());
        intent.putExtra("product_image_url",   item.getImageUrl());  // ← Step 8
        startActivity(intent);
    }

    private void deleteProduct(ProductItem item) {
        String userId = SessionManager.getInstance(this).getUserId();
        productRepository.deleteProduct(item.getId(), userId, new OperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(Products_List.this, "Product deleted", Toast.LENGTH_SHORT).show();
                loadProducts();
            }

            @Override
            public void onError(Exception exception) {
                Toast.makeText(Products_List.this, "Delete failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
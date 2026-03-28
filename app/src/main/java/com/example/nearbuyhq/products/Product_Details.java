package com.example.nearbuyhq.products;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.SessionManager;
import com.example.nearbuyhq.data.repository.OperationCallback;
import com.example.nearbuyhq.data.repository.ProductRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Product_Details extends AppCompatActivity {

    private ProductRepository productRepository;
    private String productId;
    private String productName;
    private String productDescription;
    private String productCategory;
    private double productPrice;
    private String productUnit;
    private int productQuantity;
    private long productCreatedAt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        productRepository = new ProductRepository();
        bindData();
        setupActions();
    }

    private void bindData() {
        productId = getIntent().getStringExtra("product_id");
        productName = getIntent().getStringExtra("product_name");
        productDescription = getIntent().getStringExtra("product_description");
        productCategory = getIntent().getStringExtra("product_category");
        productPrice = getIntent().getDoubleExtra("product_price", 0d);
        productUnit = getIntent().getStringExtra("product_unit");
        productQuantity = getIntent().getIntExtra("product_quantity", 0);
        productCreatedAt = getIntent().getLongExtra("product_created_at", 0L);

        ((TextView) findViewById(R.id.tv_product_name)).setText(productName == null ? "Product" : productName);
        ((TextView) findViewById(R.id.tv_product_description)).setText(productDescription == null ? "No description" : productDescription);
        ((TextView) findViewById(R.id.tv_category_badge)).setText(productCategory == null ? "General" : productCategory);
        ((TextView) findViewById(R.id.tv_product_price)).setText(String.format(Locale.US, "%.2f", productPrice));
        ((TextView) findViewById(R.id.tv_stock_qty)).setText(String.valueOf(productQuantity));
        ((TextView) findViewById(R.id.tv_sku)).setText(productId == null || productId.isEmpty() ? "N/A" : productId);

        // Product Info section
        ((TextView) findViewById(R.id.tv_product_category)).setText(productCategory == null || productCategory.isEmpty() ? "—" : productCategory);
        ((TextView) findViewById(R.id.tv_product_unit)).setText(productUnit == null || productUnit.isEmpty() ? "—" : productUnit);
        String addedDate = productCreatedAt > 0
                ? new SimpleDateFormat("dd MMM yyyy", Locale.US).format(new Date(productCreatedAt))
                : "—";
        ((TextView) findViewById(R.id.tv_added_date)).setText(addedDate);

        TextView stockStatus = findViewById(R.id.tv_stock_status);
        if (productQuantity <= 0) {
            stockStatus.setText("Out of Stock");
            stockStatus.setTextColor(getColor(R.color.stat_red));
        } else if (productQuantity <= 10) {
            stockStatus.setText("Low Stock");
            stockStatus.setTextColor(getColor(R.color.stat_red));
        } else {
            stockStatus.setText("In Stock");
            stockStatus.setTextColor(getColor(R.color.success_green));
        }

    }

    private void setupActions() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_edit).setOnClickListener(v -> {
            Intent intent = new Intent(this, Add_Product.class);
            intent.putExtra("is_edit", true);
            intent.putExtra("product_id", productId);
            intent.putExtra("product_name", productName);
            intent.putExtra("product_description", productDescription);
            intent.putExtra("product_category", productCategory);
            intent.putExtra("product_price", productPrice);
            intent.putExtra("product_unit", productUnit);
            intent.putExtra("product_quantity", productQuantity);
            intent.putExtra("product_created_at", productCreatedAt);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btn_delete).setOnClickListener(v -> deleteProduct());
        findViewById(R.id.btn_delete_header).setOnClickListener(v -> deleteProduct());
    }

    private void deleteProduct() {
        if (productId == null || productId.trim().isEmpty()) {
            Toast.makeText(this, "Missing product ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = SessionManager.getInstance(this).getUserId();
        productRepository.deleteProduct(productId, userId, new OperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(Product_Details.this, "Product deleted", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception exception) {
                Toast.makeText(Product_Details.this, "Delete failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
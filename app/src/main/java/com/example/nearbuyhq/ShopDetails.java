package com.example.nearbuyhq;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ShopDetails extends AppCompatActivity {

    private TextView shopName, shopOwner, shopLocation, shopCategory, shopStatus;
    private Button btnEdit, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        // Hide ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        shopName = findViewById(R.id.shopName);
        shopOwner = findViewById(R.id.shopOwner);
        shopLocation = findViewById(R.id.shopLocation);
        shopCategory = findViewById(R.id.shopCategory);
        shopStatus = findViewById(R.id.shopStatus);
        btnEdit = findViewById(R.id.btnEdit);
        btnBack = findViewById(R.id.btnBack);

        // Get data from intent
        String name = getIntent().getStringExtra("shop_name");
        String owner = getIntent().getStringExtra("shop_owner");
        String location = getIntent().getStringExtra("shop_location");
        String category = getIntent().getStringExtra("shop_category");
        String status = getIntent().getStringExtra("shop_status");

        // Set data
        shopName.setText(name);
        shopOwner.setText("Owner: " + owner);
        shopLocation.setText("Location: " + location);
        shopCategory.setText("Category: " + category);
        shopStatus.setText("Status: " + status);

        btnEdit.setOnClickListener(v -> {
            Toast.makeText(this, "Edit Shop Details", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to edit shop screen
        });

        btnBack.setOnClickListener(v -> finish());
    }
}


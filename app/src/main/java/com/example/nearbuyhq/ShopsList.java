package com.example.nearbuyhq;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ShopsList extends AppCompatActivity {

    private RecyclerView recyclerViewShops;
    private ShopsAdapter shopsAdapter;
    private List<Shop> shopsList;
    private FloatingActionButton fabAddShop;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shops_list);

        // Hide ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        recyclerViewShops = findViewById(R.id.recyclerViewShops);
        fabAddShop = findViewById(R.id.fabAddShop);
        btnBack = findViewById(R.id.btnBack);

        // Initialize sample data
        initSampleShops();

        // Setup RecyclerView
        shopsAdapter = new ShopsAdapter(shopsList, this::onShopClick);
        recyclerViewShops.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewShops.setAdapter(shopsAdapter);

        fabAddShop.setOnClickListener(v -> {
            Intent intent = new Intent(ShopsList.this, AddShop.class);
            startActivity(intent);
        });

        // Back button click listener
        btnBack.setOnClickListener(v -> finish());
    }

    private void initSampleShops() {
        shopsList = new ArrayList<>();
        shopsList.add(new Shop("1", "Fresh Mart", "John Doe", "123 Main St", "Grocery", "Active"));
        shopsList.add(new Shop("2", "Tech Hub", "Jane Smith", "456 Tech Ave", "Electronics", "Active"));
        shopsList.add(new Shop("3", "Fashion Plaza", "Mike Johnson", "789 Fashion Blvd", "Clothing", "Active"));
        shopsList.add(new Shop("4", "Book Haven", "Sarah Wilson", "321 Book St", "Books", "Active"));
        shopsList.add(new Shop("5", "Coffee Corner", "Tom Brown", "654 Coffee Ln", "Cafe", "Active"));
        shopsList.add(new Shop("6", "Fitness First", "Emma Davis", "987 Gym Rd", "Fitness", "Active"));
    }

    private void onShopClick(Shop shop) {
        Intent intent = new Intent(ShopsList.this, ShopDetails.class);
        intent.putExtra("shop_id", shop.getId());
        intent.putExtra("shop_name", shop.getName());
        intent.putExtra("shop_owner", shop.getOwner());
        intent.putExtra("shop_location", shop.getLocation());
        intent.putExtra("shop_category", shop.getCategory());
        intent.putExtra("shop_status", shop.getStatus());
        startActivity(intent);
    }
}


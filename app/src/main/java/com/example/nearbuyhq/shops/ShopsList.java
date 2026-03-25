package com.example.nearbuyhq.shops;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.firebase.FirebaseConfig;
import com.example.nearbuyhq.data.repository.DataCallback;
import com.example.nearbuyhq.data.repository.ShopRepository;

import java.util.ArrayList;
import java.util.List;

public class ShopsList extends AppCompatActivity {

    private RecyclerView recyclerViewShops;
    private ShopsAdapter shopsAdapter;
    private List<Shop> shopsList;
    private FloatingActionButton fabAddShop;
    private ImageView btnBack;
    private ShopRepository shopRepository;

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
        shopsList = new ArrayList<>();
        shopRepository = new ShopRepository();

        // Setup RecyclerView
        shopsAdapter = new ShopsAdapter(shopsList, this::onShopClick);
        recyclerViewShops.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewShops.setAdapter(shopsAdapter);
        loadShops();

        fabAddShop.setOnClickListener(v -> {
            Intent intent = new Intent(ShopsList.this, AddShop.class);
            startActivity(intent);
        });

        // Back button click listener
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadShops();
    }

    private void loadShops() {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            loadFallbackShops();
            Toast.makeText(this, "Firebase is disabled. Showing local data.", Toast.LENGTH_SHORT).show();
            return;
        }

        shopRepository.getAllShops(new DataCallback<List<Shop>>() {
            @Override
            public void onSuccess(List<Shop> data) {
                shopsList.clear();
                shopsList.addAll(data);
                shopsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception exception) {
                Toast.makeText(ShopsList.this, "Failed to load branches: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                loadFallbackShops();
            }
        });
    }

    private void loadFallbackShops() {
        shopsList.clear();
        shopsList.add(new Shop("1", "Fresh Mart", "John Doe", "123 Main St", "Grocery", "Active"));
        shopsList.add(new Shop("2", "Tech Hub", "Jane Smith", "456 Tech Ave", "Electronics", "Active"));
        shopsList.add(new Shop("3", "Fashion Plaza", "Mike Johnson", "789 Fashion Blvd", "Clothing", "Active"));
        shopsList.add(new Shop("4", "Book Haven", "Sarah Wilson", "321 Book St", "Books", "Active"));
        shopsList.add(new Shop("5", "Coffee Corner", "Tom Brown", "654 Coffee Ln", "Cafe", "Active"));
        shopsList.add(new Shop("6", "Fitness First", "Emma Davis", "987 Gym Rd", "Fitness", "Active"));
        shopsAdapter.notifyDataSetChanged();
    }

    private void onShopClick(Shop shop) {
        Intent intent = new Intent(ShopsList.this, ShopDetails.class);
        intent.putExtra("shop_id", shop.getId());
        intent.putExtra("shop_name", shop.getName());
        intent.putExtra("shop_owner", shop.getOwner());
        intent.putExtra("shop_location", shop.getLocation());
        intent.putExtra("shop_category", shop.getCategory());
        intent.putExtra("shop_status", shop.getStatus());
        intent.putExtra("shop_contact", shop.getContact());
        startActivity(intent);
    }
}


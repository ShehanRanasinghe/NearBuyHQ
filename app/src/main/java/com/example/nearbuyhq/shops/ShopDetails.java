package com.example.nearbuyhq.shops;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.firebase.FirebaseConfig;
import com.example.nearbuyhq.data.repository.DataCallback;
import com.example.nearbuyhq.data.repository.OperationCallback;
import com.example.nearbuyhq.data.repository.ShopRepository;

public class ShopDetails extends AppCompatActivity {

    private TextView shopName, shopOwner, shopLocation, shopCategory, shopStatus;
    private Button btnEdit, btnBack;
    private ShopRepository shopRepository;
    private String shopId;
    private Shop currentShop;

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
        shopRepository = new ShopRepository();

        shopId = getIntent().getStringExtra("shop_id");
        Shop intentShop = new Shop(
                shopId,
                getIntent().getStringExtra("shop_name"),
                getIntent().getStringExtra("shop_owner"),
                getIntent().getStringExtra("shop_location"),
                getIntent().getStringExtra("shop_category"),
                getIntent().getStringExtra("shop_status")
        );
        renderShop(intentShop);
        currentShop = intentShop;

        if (FirebaseConfig.isFirebaseEnabled() && shopId != null && !shopId.trim().isEmpty()) {
            refreshShop();
        }

        btnEdit.setOnClickListener(v -> {
            if (currentShop == null || currentShop.getId() == null || currentShop.getId().trim().isEmpty()) {
                Toast.makeText(this, "Shop cannot be edited", Toast.LENGTH_SHORT).show();
                return;
            }
            String nextStatus = "Active".equalsIgnoreCase(currentShop.getStatus()) ? "Inactive" : "Active";
            btnEdit.setEnabled(false);
            shopRepository.updateStatus(currentShop.getId(), nextStatus, new OperationCallback() {
                @Override
                public void onSuccess() {
                    btnEdit.setEnabled(true);
                    currentShop.setStatus(nextStatus);
                    renderShop(currentShop);
                    Toast.makeText(ShopDetails.this, "Status updated", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Exception exception) {
                    btnEdit.setEnabled(true);
                    Toast.makeText(ShopDetails.this, "Failed to update: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void refreshShop() {
        shopRepository.getShop(shopId, new DataCallback<Shop>() {
            @Override
            public void onSuccess(Shop data) {
                if (data != null) {
                    currentShop = data;
                    renderShop(data);
                }
            }

            @Override
            public void onError(Exception exception) {
                Toast.makeText(ShopDetails.this, "Using cached branch details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderShop(Shop shop) {
        if (shop == null) {
            return;
        }
        shopName.setText(shop.getName());
        shopOwner.setText("Owner: " + shop.getOwner());
        shopLocation.setText("Location: " + shop.getLocation());
        shopCategory.setText("Category: " + shop.getCategory());
        shopStatus.setText("Status: " + shop.getStatus());
    }
}


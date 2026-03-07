package com.example.nearbuyhq;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DealDetails extends AppCompatActivity {

    private TextView dealTitle, dealShop, dealDiscount, dealValidity;
    private Button btnEdit, btnDelete, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal_details);

        // Hide ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        dealTitle = findViewById(R.id.dealTitle);
        dealShop = findViewById(R.id.dealShop);
        dealDiscount = findViewById(R.id.dealDiscount);
        dealValidity = findViewById(R.id.dealValidity);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        btnBack = findViewById(R.id.btnBack);

        // Get data from intent
        String title = getIntent().getStringExtra("deal_title");
        String shop = getIntent().getStringExtra("deal_shop");
        String discount = getIntent().getStringExtra("deal_discount");
        String validity = getIntent().getStringExtra("deal_validity");

        // Set data
        dealTitle.setText(title);
        dealShop.setText("Shop: " + shop);
        dealDiscount.setText("Discount: " + discount);
        dealValidity.setText(validity);

        btnEdit.setOnClickListener(v -> {
            Toast.makeText(this, "Edit Deal", Toast.LENGTH_SHORT).show();
        });

        btnDelete.setOnClickListener(v -> {
            Toast.makeText(this, "Deal Deleted", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnBack.setOnClickListener(v -> finish());
    }
}


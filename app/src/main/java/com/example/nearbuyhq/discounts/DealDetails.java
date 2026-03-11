package com.example.nearbuyhq.discounts;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nearbuyhq.R;

public class DealDetails extends AppCompatActivity {

    private TextView dealTitle, dealShop, dealDiscount, dealValidity;
    private TextView btnEdit, btnDelete;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        dealTitle    = findViewById(R.id.dealTitle);
        dealShop     = findViewById(R.id.dealShop);
        dealDiscount = findViewById(R.id.dealDiscount);
        dealValidity = findViewById(R.id.dealValidity);
        btnEdit      = findViewById(R.id.btnEdit);
        btnDelete    = findViewById(R.id.btnDelete);
        btnBack      = findViewById(R.id.btn_back);

        // Get data from intent
        String title    = getIntent().getStringExtra("deal_title");
        String shop     = getIntent().getStringExtra("deal_shop");
        String discount = getIntent().getStringExtra("deal_discount");
        String validity = getIntent().getStringExtra("deal_validity");

        if (title    != null) dealTitle.setText(title);
        if (shop     != null) dealShop.setText("Shop: " + shop);
        if (discount != null) dealDiscount.setText(discount + " OFF");
        if (validity != null) dealValidity.setText(validity);

        btnEdit.setOnClickListener(v ->
            Toast.makeText(this, "Edit Deal", Toast.LENGTH_SHORT).show());

        btnDelete.setOnClickListener(v -> {
            Toast.makeText(this, "Deal Deleted", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnBack.setOnClickListener(v -> finish());
    }
}


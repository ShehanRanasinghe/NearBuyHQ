package com.example.nearbuyhq.discounts;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.data.repository.DataCallback;
import com.example.nearbuyhq.data.repository.DiscountRepository;
import com.example.nearbuyhq.data.repository.OperationCallback;

import android.content.Intent;

public class DealDetails extends AppCompatActivity {

    private TextView dealTitle, dealShop, dealDiscount, dealValidity;
    private TextView btnEdit, btnDelete;
    private ImageView btnBack;
    private DiscountRepository discountRepository;
    private String dealId;
    private String dealDescription;

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
        discountRepository = new DiscountRepository();

        // Get data from intent
        dealId = getIntent().getStringExtra("deal_id");
        String title = getIntent().getStringExtra("deal_title");
        String shop = getIntent().getStringExtra("deal_shop");
        String discount = getIntent().getStringExtra("deal_discount");
        String validity = getIntent().getStringExtra("deal_validity");
        dealDescription = getIntent().getStringExtra("deal_description");

        if (title    != null) dealTitle.setText(title);
        if (shop     != null) dealShop.setText("Shop: " + shop);
        if (discount != null) dealDiscount.setText(discount + " OFF");
        if (validity != null) dealValidity.setText(validity);

        if (dealId != null && !dealId.trim().isEmpty()) {
            discountRepository.getDeal(dealId, new DataCallback<Deal>() {
                @Override
                public void onSuccess(Deal data) {
                    if (data == null) {
                        return;
                    }
                    dealTitle.setText(data.getTitle());
                    dealShop.setText("Shop: " + data.getShopName());
                    dealDiscount.setText(data.getDiscount() + " OFF");
                    dealValidity.setText(data.getValidity());
                    dealDescription = data.getDescription();
                }

                @Override
                public void onError(Exception exception) {
                    Toast.makeText(DealDetails.this, "Using local deal data", Toast.LENGTH_SHORT).show();
                }
            });
        }

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(DealDetails.this, AddDeal.class);
            intent.putExtra("is_edit", true);
            intent.putExtra("deal_id", dealId);
            intent.putExtra("deal_title", dealTitle.getText().toString());
            intent.putExtra("deal_shop", dealShop.getText().toString().replace("Shop: ", ""));
            intent.putExtra("deal_discount", dealDiscount.getText().toString().replace(" OFF", ""));
            intent.putExtra("deal_validity", dealValidity.getText().toString());
            intent.putExtra("deal_description", dealDescription == null ? "" : dealDescription);
            intent.putExtra("deal_created_at", System.currentTimeMillis());
            startActivity(intent);
            finish();
        });

        btnDelete.setOnClickListener(v -> {
            if (dealId == null || dealId.trim().isEmpty()) {
                Toast.makeText(this, "Missing deal ID", Toast.LENGTH_SHORT).show();
                return;
            }
            discountRepository.deleteDeal(dealId, new OperationCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(DealDetails.this, "Deal Deleted", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(Exception exception) {
                    Toast.makeText(DealDetails.this, "Delete failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        btnBack.setOnClickListener(v -> finish());
    }
}


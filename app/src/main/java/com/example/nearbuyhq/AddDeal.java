package com.example.nearbuyhq;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddDeal extends AppCompatActivity {

    private EditText dealTitle, dealDescription, dealDiscount, dealValidity;
    private Spinner shopSelection;
    private Button btnSubmit, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_deal);

        // Hide ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        dealTitle = findViewById(R.id.dealTitle);
        dealDescription = findViewById(R.id.dealDescription);
        dealDiscount = findViewById(R.id.dealDiscount);
        dealValidity = findViewById(R.id.dealValidity);
        shopSelection = findViewById(R.id.shopSelection);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnCancel = findViewById(R.id.btnCancel);

        // Setup shop spinner
        String[] shops = {"Fresh Mart", "Tech Hub", "Fashion Plaza", "Book Haven", "Coffee Corner", "Fitness First"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, shops);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shopSelection.setAdapter(adapter);

        btnSubmit.setOnClickListener(v -> {
            if (validateInputs()) {
                Toast.makeText(this, "Deal created successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    private boolean validateInputs() {
        if (dealTitle.getText().toString().trim().isEmpty()) {
            dealTitle.setError("Deal title is required");
            return false;
        }
        if (dealDescription.getText().toString().trim().isEmpty()) {
            dealDescription.setError("Description is required");
            return false;
        }
        if (dealDiscount.getText().toString().trim().isEmpty()) {
            dealDiscount.setError("Discount is required");
            return false;
        }
        if (dealValidity.getText().toString().trim().isEmpty()) {
            dealValidity.setError("Validity date is required");
            return false;
        }
        return true;
    }
}


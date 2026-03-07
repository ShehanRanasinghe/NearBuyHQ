package com.example.nearbuyhq;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddShop extends AppCompatActivity {

    private EditText shopName, shopOwner, shopAddress, shopContact;
    private Spinner shopCategory;
    private Button btnSubmit, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_shop);

        // Hide ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        shopName = findViewById(R.id.shopName);
        shopOwner = findViewById(R.id.shopOwner);
        shopAddress = findViewById(R.id.shopAddress);
        shopContact = findViewById(R.id.shopContact);
        shopCategory = findViewById(R.id.shopCategory);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnCancel = findViewById(R.id.btnCancel);

        // Setup category spinner
        String[] categories = {"Grocery", "Electronics", "Clothing", "Books", "Cafe", "Fitness", "Restaurant", "Pharmacy"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shopCategory.setAdapter(adapter);

        btnSubmit.setOnClickListener(v -> {
            if (validateInputs()) {
                Toast.makeText(this, "Branch added successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    private boolean validateInputs() {
        if (shopName.getText().toString().trim().isEmpty()) {
            shopName.setError("Shop name is required");
            return false;
        }
        if (shopOwner.getText().toString().trim().isEmpty()) {
            shopOwner.setError("Owner name is required");
            return false;
        }
        if (shopAddress.getText().toString().trim().isEmpty()) {
            shopAddress.setError("Address is required");
            return false;
        }
        if (shopContact.getText().toString().trim().isEmpty()) {
            shopContact.setError("Contact is required");
            return false;
        }
        return true;
    }
}


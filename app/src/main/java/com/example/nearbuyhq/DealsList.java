package com.example.nearbuyhq;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class DealsList extends AppCompatActivity {

    private RecyclerView recyclerViewDeals;
    private DealsAdapter dealsAdapter;
    private List<Deal> dealsList;
    private FloatingActionButton fabAddDeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deals_list);

        // Hide ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        recyclerViewDeals = findViewById(R.id.recyclerViewDeals);
        fabAddDeal = findViewById(R.id.fabAddDeal);

        // Initialize sample data
        initSampleDeals();

        // Setup RecyclerView
        dealsAdapter = new DealsAdapter(dealsList, this::onDealClick);
        recyclerViewDeals.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDeals.setAdapter(dealsAdapter);

        fabAddDeal.setOnClickListener(v -> {
            Intent intent = new Intent(DealsList.this, AddDeal.class);
            startActivity(intent);
        });
    }

    private void initSampleDeals() {
        dealsList = new ArrayList<>();
        dealsList.add(new Deal("1", "50% Off on Groceries", "Fresh Mart", "50%", "Valid till Mar 31, 2026"));
        dealsList.add(new Deal("2", "Buy 1 Get 1 on Electronics", "Tech Hub", "BOGO", "Valid till Apr 15, 2026"));
        dealsList.add(new Deal("3", "30% Off on Clothing", "Fashion Plaza", "30%", "Valid till Mar 20, 2026"));
        dealsList.add(new Deal("4", "Free Coffee with Purchase", "Coffee Corner", "Free", "Valid till Mar 10, 2026"));
        dealsList.add(new Deal("5", "20% Off on Books", "Book Haven", "20%", "Valid till Apr 30, 2026"));
    }

    private void onDealClick(Deal deal) {
        Intent intent = new Intent(DealsList.this, DealDetails.class);
        intent.putExtra("deal_id", deal.getId());
        intent.putExtra("deal_title", deal.getTitle());
        intent.putExtra("deal_shop", deal.getShopName());
        intent.putExtra("deal_discount", deal.getDiscount());
        intent.putExtra("deal_validity", deal.getValidity());
        startActivity(intent);
    }
}


package com.example.nearbuyhq.discounts;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.data.repository.DataCallback;
import com.example.nearbuyhq.data.repository.DiscountRepository;

import java.util.ArrayList;
import java.util.List;

public class DealsList extends AppCompatActivity {

    private RecyclerView recyclerViewDeals;
    private DealsAdapter dealsAdapter;
    private List<Deal> allDeals = new ArrayList<>();
    private List<Deal> dealsList = new ArrayList<>();
    private FloatingActionButton fabAddDeal;
    private DiscountRepository discountRepository;
    private String searchQuery = "";

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
        discountRepository = new DiscountRepository();

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        dealsAdapter = new DealsAdapter(dealsList, this::onDealClick);
        recyclerViewDeals.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDeals.setAdapter(dealsAdapter);

        // Wire search
        EditText etSearch = findViewById(R.id.etSearchDeals);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int st, int c, int a) {
                }

                @Override
                public void onTextChanged(CharSequence s, int st, int b, int c) {
                    searchQuery = s.toString().trim().toLowerCase();
                    applySearch();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        fabAddDeal.setOnClickListener(v -> {
            Intent intent = new Intent(DealsList.this, AddDeal.class);
            startActivity(intent);
        });

        loadDeals();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDeals();
    }

    private void loadDeals() {
        String userId = com.example.nearbuyhq.core.SessionManager.getInstance(this).getUserId();
        discountRepository.getDealsByUserId(userId, new DataCallback<List<Deal>>() {
            @Override
            public void onSuccess(List<Deal> data) {
                allDeals.clear();
                allDeals.addAll(data);
                applySearch();
            }

            @Override
            public void onError(Exception exception) {
                Toast.makeText(DealsList.this, "Could not load deals", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applySearch() {
        dealsList.clear();
        for (Deal d : allDeals) {
            if (searchQuery.isEmpty()
                    || (d.getTitle() != null && d.getTitle().toLowerCase().contains(searchQuery))
                    || (d.getShopName() != null && d.getShopName().toLowerCase().contains(searchQuery))) {
                dealsList.add(d);
            }
        }
        dealsAdapter.notifyDataSetChanged();
    }

    private void onDealClick(Deal deal) {
        Intent intent = new Intent(DealsList.this, DealDetails.class);
        intent.putExtra("deal_id", deal.getId());
        intent.putExtra("deal_title", deal.getTitle());
        intent.putExtra("deal_shop", deal.getShopName());
        intent.putExtra("deal_discount", deal.getDiscount());
        intent.putExtra("deal_validity", deal.getValidity());
        intent.putExtra("deal_description", deal.getDescription());
        startActivity(intent);
    }
}

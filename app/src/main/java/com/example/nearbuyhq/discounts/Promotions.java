package com.example.nearbuyhq.discounts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.nearbuyhq.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class Promotions extends AppCompatActivity {

    static final String PREFS_NAME = "NearBuyHQ_Promos";
    static final String PREFS_KEY  = "promotions_json";

    private RecyclerView      recyclerView;
    private PromotionAdapter  adapter;
    private List<Promotion>   promotionList;
    private LinearLayout      llEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.deep_blue));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.bg_white));

        setContentView(R.layout.activity_promotions);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        recyclerView = findViewById(R.id.rvPromotions);
        llEmptyState = findViewById(R.id.llEmptyState);
        FloatingActionButton fab = findViewById(R.id.fabAddPromotion);

        promotionList = new ArrayList<>();
        adapter = new PromotionAdapter(promotionList, new PromotionAdapter.OnPromotionActionListener() {
            @Override
            public void onEdit(int position) {
                Promotion p = promotionList.get(position);
                Intent intent = new Intent(Promotions.this, AddEditPromotion.class);
                intent.putExtra(AddEditPromotion.EXTRA_PROMO_ID, p.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(int position) {
                Promotion p = promotionList.get(position);
                new AlertDialog.Builder(Promotions.this)
                        .setTitle("Delete Promotion")
                        .setMessage("Remove \"" + p.getTitle() + "\"?\nThis cannot be undone.")
                        .setPositiveButton("Delete", (d, w) -> {
                            promotionList.remove(position);
                            adapter.notifyItemRemoved(position);
                            savePromotions();
                            toggleEmptyState();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditPromotion.class)));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPromotions();
        adapter.notifyDataSetChanged();
        toggleEmptyState();
    }

    // ── Data helpers ────────────────────────────────────────────────────────

    private void loadPromotions() {
        promotionList.clear();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = prefs.getString(PREFS_KEY, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                promotionList.add(Promotion.fromJson(arr.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void savePromotions() {
        JSONArray arr = new JSONArray();
        for (Promotion p : promotionList) {
            try { arr.put(p.toJson()); } catch (JSONException ignored) {}
        }
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit().putString(PREFS_KEY, arr.toString()).apply();
    }

    private void toggleEmptyState() {
        boolean empty = promotionList.isEmpty();
        llEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}

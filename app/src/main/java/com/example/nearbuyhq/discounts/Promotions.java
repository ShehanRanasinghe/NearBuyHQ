package com.example.nearbuyhq.discounts;

import android.content.Intent;
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
import com.example.nearbuyhq.core.SessionManager;
import com.example.nearbuyhq.core.firebase.FirebaseConfig;
import com.example.nearbuyhq.data.repository.DataCallback;
import com.example.nearbuyhq.data.repository.DiscountRepository;
import com.example.nearbuyhq.data.repository.OperationCallback;

import java.util.ArrayList;
import java.util.List;

// Promotions screen – shows all promotions for the shop in a RecyclerView with add/edit/delete support.
public class Promotions extends AppCompatActivity {

    private RecyclerView      recyclerView;
    private PromotionAdapter  adapter;
    private List<Promotion>   promotionList;
    private LinearLayout      llEmptyState;
    private DiscountRepository discountRepository;

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
        discountRepository = new DiscountRepository();
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
                            if (!FirebaseConfig.isFirebaseEnabled()) {
                                return;
                            }
                            String userId = SessionManager.getInstance(Promotions.this).getUserId();
                            discountRepository.deletePromotion(p.getId(), userId, new OperationCallback() {
                                @Override
                                public void onSuccess() {
                                    promotionList.remove(position);
                                    adapter.notifyItemRemoved(position);
                                    toggleEmptyState();
                                }

                                @Override
                                public void onError(Exception exception) {
                                }
                            });
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
        loadPromotions(); // refresh list when returning from add/edit screen
    }

    // ── Firestore load ────────────────────────────────────────────────────

    private void loadPromotions() {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            promotionList.clear();
            adapter.notifyDataSetChanged();
            toggleEmptyState();
            return;
        }

        String userId = SessionManager.getInstance(this).getUserId();
        discountRepository.getPromotionsByUserId(userId, new DataCallback<List<Promotion>>() {
            @Override
            public void onSuccess(List<Promotion> data) {
                promotionList.clear();
                promotionList.addAll(data);
                adapter.notifyDataSetChanged();
                toggleEmptyState();
            }

            @Override
            public void onError(Exception exception) {
                promotionList.clear();
                adapter.notifyDataSetChanged();
                toggleEmptyState();
            }
        });
    }

    // ── Empty state toggle ────────────────────────────────────────────────

    // Show empty-state placeholder when the list has no promotions; hide it otherwise
    private void toggleEmptyState() {
        boolean empty = promotionList.isEmpty();
        llEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}

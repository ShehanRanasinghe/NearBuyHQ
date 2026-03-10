package com.example.nearbuyhq.discounts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nearbuyhq.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class PromotionsFragment extends Fragment {

    private RecyclerView recyclerView;
    private PromotionAdapter adapter;
    private List<Promotion> promotionList;
    private LinearLayout llEmptyState;
    private FloatingActionButton fabAddPromotion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_promotions, container, false);

        recyclerView = view.findViewById(R.id.rvPromotions);
        llEmptyState = view.findViewById(R.id.llEmptyState);
        fabAddPromotion = view.findViewById(R.id.fabAddPromotion);

        promotionList = new ArrayList<>();
        adapter = new PromotionAdapter(promotionList, new PromotionAdapter.OnPromotionActionListener() {
            @Override
            public void onEdit(int position) {
                Promotion p = promotionList.get(position);
                Intent intent = new Intent(getActivity(), AddEditPromotion.class);
                intent.putExtra(AddEditPromotion.EXTRA_PROMO_ID, p.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(int position) {
                Promotion p = promotionList.get(position);
                new AlertDialog.Builder(requireContext())
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

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        fabAddPromotion.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AddEditPromotion.class)));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPromotions();
        adapter.notifyDataSetChanged();
        toggleEmptyState();
    }

    private void loadPromotions() {
        promotionList.clear();
        SharedPreferences prefs = requireActivity().getSharedPreferences(Promotions.PREFS_NAME, MODE_PRIVATE);
        String json = prefs.getString(Promotions.PREFS_KEY, "[]");
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
        requireActivity().getSharedPreferences(Promotions.PREFS_NAME, MODE_PRIVATE)
                .edit().putString(Promotions.PREFS_KEY, arr.toString()).apply();
    }

    private void toggleEmptyState() {
        boolean empty = promotionList.isEmpty();
        llEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}


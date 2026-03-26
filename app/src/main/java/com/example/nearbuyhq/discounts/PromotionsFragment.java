package com.example.nearbuyhq.discounts;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.data.repository.DataCallback;
import com.example.nearbuyhq.data.repository.DiscountRepository;
import com.example.nearbuyhq.data.repository.OperationCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class PromotionsFragment extends Fragment {

    private RecyclerView recyclerView;
    private PromotionAdapter adapter;
    private List<Promotion> promotionList;
    private LinearLayout llEmptyState;
    private FloatingActionButton fabAddPromotion;
    private DiscountRepository discountRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_promotions, container, false);

        recyclerView = view.findViewById(R.id.rvPromotions);
        llEmptyState = view.findViewById(R.id.llEmptyState);
        fabAddPromotion = view.findViewById(R.id.fabAddPromotion);

        promotionList = new ArrayList<>();
        discountRepository = new DiscountRepository();
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
                            discountRepository.deletePromotion(p.getId(), new OperationCallback() {
                                @Override
                                public void onSuccess() {
                                    promotionList.remove(position);
                                    adapter.notifyItemRemoved(position);
                                    toggleEmptyState();
                                }

                                @Override
                                public void onError(Exception exception) {
                                    Toast.makeText(requireContext(), "Delete failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
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
    }

    private void loadPromotions() {
        discountRepository.getPromotions(new DataCallback<List<Promotion>>() {
            @Override
            public void onSuccess(List<Promotion> data) {
                promotionList.clear();
                promotionList.addAll(data);
                adapter.notifyDataSetChanged();
                toggleEmptyState();
            }

            @Override
            public void onError(Exception exception) {
                Toast.makeText(requireContext(), "Failed to load promotions", Toast.LENGTH_SHORT).show();
                promotionList.clear();
                adapter.notifyDataSetChanged();
                toggleEmptyState();
            }
        });
    }

    private void toggleEmptyState() {
        boolean empty = promotionList.isEmpty();
        llEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}


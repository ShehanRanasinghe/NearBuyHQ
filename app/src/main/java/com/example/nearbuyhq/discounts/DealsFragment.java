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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.SessionManager;
import com.example.nearbuyhq.data.repository.DataCallback;
import com.example.nearbuyhq.data.repository.DiscountRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

// Fragment shown on the Deals tab – loads all deals from Firestore and allows adding new ones via the FAB.
public class DealsFragment extends Fragment {

    private RecyclerView recyclerView;
    private DealsAdapter adapter;
    private List<Deal> dealsList;
    private LinearLayout llEmptyState;
    private FloatingActionButton fabAddDeal;
    private DiscountRepository discountRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deals, container, false);

        recyclerView = view.findViewById(R.id.rvDeals);
        llEmptyState = view.findViewById(R.id.llEmptyState);
        fabAddDeal = view.findViewById(R.id.fabAddDeal);

        dealsList = new ArrayList<>();
        discountRepository = new DiscountRepository();

        adapter = new DealsAdapter(dealsList, deal -> {
            Intent intent = new Intent(getActivity(), DealDetails.class);
            intent.putExtra("deal_id", deal.getId());
            intent.putExtra("deal_title", deal.getTitle());
            intent.putExtra("deal_shop", deal.getShopName());
            intent.putExtra("deal_discount", deal.getDiscount());
            intent.putExtra("deal_validity", deal.getValidity());
            intent.putExtra("deal_description", deal.getDescription());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        fabAddDeal.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AddDeal.class)));

        loadDeals();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDeals();
    }

    private void loadDeals() {
        String userId = SessionManager.getInstance(requireContext()).getUserId();
        discountRepository.getDealsByUserId(userId, new DataCallback<List<Deal>>() {
            @Override
            public void onSuccess(List<Deal> data) {
                dealsList.clear();
                dealsList.addAll(data);
                adapter.notifyDataSetChanged();
                toggleEmptyState();
            }

            @Override
            public void onError(Exception exception) {
                Toast.makeText(requireContext(), "Failed to load deals", Toast.LENGTH_SHORT).show();
                dealsList.clear();
                adapter.notifyDataSetChanged();
                toggleEmptyState();
            }
        });
    }

    private void toggleEmptyState() {
        boolean empty = dealsList.isEmpty();
        llEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}


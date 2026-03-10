package com.example.nearbuyhq.discounts;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nearbuyhq.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class DealsFragment extends Fragment {

    private RecyclerView recyclerView;
    private DealsAdapter adapter;
    private List<Deal> dealsList;
    private LinearLayout llEmptyState;
    private FloatingActionButton fabAddDeal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deals, container, false);

        recyclerView = view.findViewById(R.id.rvDeals);
        llEmptyState = view.findViewById(R.id.llEmptyState);
        fabAddDeal = view.findViewById(R.id.fabAddDeal);

        dealsList = new ArrayList<>();
        loadSampleDeals();

        adapter = new DealsAdapter(dealsList, deal -> {
            // Handle deal click
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        fabAddDeal.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AddDeal.class)));

        toggleEmptyState();

        return view;
    }

    private void loadSampleDeals() {
        // Add sample deals
        dealsList.add(new Deal("1", "Buy 2 Get 1 Free", "All dairy products", "20%", "March 15, 2026"));
        dealsList.add(new Deal("2", "Weekend Special", "Fresh fruits", "15%", "March 12, 2026"));
        dealsList.add(new Deal("3", "Spring Sale", "All vegetables", "25%", "March 20, 2026"));
    }

    private void toggleEmptyState() {
        boolean empty = dealsList.isEmpty();
        llEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}


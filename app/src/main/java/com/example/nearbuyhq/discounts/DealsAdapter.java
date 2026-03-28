package com.example.nearbuyhq.discounts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nearbuyhq.R;

import java.util.List;

// RecyclerView adapter for the deals list – binds each Deal to the item_deal layout and fires click events.
public class DealsAdapter extends RecyclerView.Adapter<DealsAdapter.DealViewHolder> {

    private List<Deal> dealsList;
    private OnDealClickListener listener;

    public interface OnDealClickListener {
        void onDealClick(Deal deal);
    }

    public DealsAdapter(List<Deal> dealsList, OnDealClickListener listener) {
        this.dealsList = dealsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deal, parent, false);
        return new DealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {
        Deal deal = dealsList.get(position);
        holder.dealTitle.setText(deal.getTitle());
        holder.dealShop.setText(deal.getShopName());
        holder.dealDescription.setText(deal.getDescription());
        holder.dealValidity.setText(deal.getValidity());

        holder.itemView.setOnClickListener(v -> listener.onDealClick(deal));
    }

    @Override
    public int getItemCount() {
        return dealsList.size();
    }

    static class DealViewHolder extends RecyclerView.ViewHolder {
        TextView dealTitle, dealShop, dealDescription, dealValidity;

        public DealViewHolder(@NonNull View itemView) {
            super(itemView);
            dealTitle       = itemView.findViewById(R.id.dealTitle);
            dealShop        = itemView.findViewById(R.id.dealShop);
            dealDescription = itemView.findViewById(R.id.dealDescription);
            dealValidity    = itemView.findViewById(R.id.dealValidity);
        }
    }
}


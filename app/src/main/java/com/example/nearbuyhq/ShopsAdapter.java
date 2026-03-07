package com.example.nearbuyhq;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ShopsAdapter extends RecyclerView.Adapter<ShopsAdapter.ShopViewHolder> {

    private List<Shop> shopsList;
    private OnShopClickListener listener;

    public interface OnShopClickListener {
        void onShopClick(Shop shop);
    }

    public ShopsAdapter(List<Shop> shopsList, OnShopClickListener listener) {
        this.shopsList = shopsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop, parent, false);
        return new ShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        Shop shop = shopsList.get(position);
        holder.shopName.setText(shop.getName());
        holder.shopOwner.setText("Owner: " + shop.getOwner());
        holder.shopLocation.setText(shop.getLocation());
        holder.shopCategory.setText(shop.getCategory());
        holder.shopStatus.setText(shop.getStatus());

        // Set status color
        if ("Active".equals(shop.getStatus())) {
            holder.shopStatus.setTextColor(holder.itemView.getContext().getColor(R.color.success_green));
        } else {
            holder.shopStatus.setTextColor(holder.itemView.getContext().getColor(R.color.warning_orange));
        }

        holder.itemView.setOnClickListener(v -> listener.onShopClick(shop));
    }

    @Override
    public int getItemCount() {
        return shopsList.size();
    }

    static class ShopViewHolder extends RecyclerView.ViewHolder {
        TextView shopName, shopOwner, shopLocation, shopCategory, shopStatus;

        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            shopName = itemView.findViewById(R.id.shopName);
            shopOwner = itemView.findViewById(R.id.shopOwner);
            shopLocation = itemView.findViewById(R.id.shopLocation);
            shopCategory = itemView.findViewById(R.id.shopCategory);
            shopStatus = itemView.findViewById(R.id.shopStatus);
        }
    }
}


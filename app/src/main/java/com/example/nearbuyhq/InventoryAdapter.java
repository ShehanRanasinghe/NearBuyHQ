package com.example.nearbuyhq;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private List<Inventory.InventoryItem> items;
    private final Context context;
    private final int lowStockThreshold;

    InventoryAdapter(Context context, List<Inventory.InventoryItem> items, int lowStockThreshold) {
        this.context = context;
        this.items = new ArrayList<>(items);
        this.lowStockThreshold = lowStockThreshold;
    }

    void updateList(List<Inventory.InventoryItem> newList) {
        this.items = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_inventory, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        Inventory.InventoryItem item = items.get(position);

        holder.tvProductName.setText(item.name);
        holder.tvBrand.setText(item.brand);
        holder.tvCategory.setText(item.category);
        holder.tvStockCount.setText("Stock: " + item.currentStock + " " + item.unit);

        int pct = item.getStockPercentage();
        holder.tvStockPercent.setText(pct + "%");
        holder.progressStock.setProgress(pct);

        if (item.isOutOfStock()) {
            // Grey / red-darker badge for out of stock
            holder.tvStatusBadge.setText("Out of Stock");
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.stat_red));
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_stock_badge_low);
            holder.progressStock.setProgressTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.stat_red)));
            holder.viewLowStockAccent.setVisibility(View.VISIBLE);
        } else if (item.isLowStock(lowStockThreshold)) {
            // Red status badge
            holder.tvStatusBadge.setText("Low Stock");
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.stat_red));
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_stock_badge_low);
            // Red progress bar fill
            holder.progressStock.setProgressTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.stat_red)));
            // Show left accent stripe
            holder.viewLowStockAccent.setVisibility(View.VISIBLE);
        } else {
            // Green status badge
            holder.tvStatusBadge.setText("Available");
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.stat_green));
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_stock_badge_green);
            // Green progress bar fill
            holder.progressStock.setProgressTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.stat_green)));
            // Hide left accent stripe
            holder.viewLowStockAccent.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvBrand, tvCategory, tvStatusBadge, tvStockCount, tvStockPercent;
        ProgressBar progressStock;
        View viewLowStockAccent;

        InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName      = itemView.findViewById(R.id.tvProductName);
            tvBrand            = itemView.findViewById(R.id.tvBrand);
            tvCategory         = itemView.findViewById(R.id.tvCategory);
            tvStatusBadge      = itemView.findViewById(R.id.tvStatusBadge);
            tvStockCount       = itemView.findViewById(R.id.tvStockCount);
            tvStockPercent     = itemView.findViewById(R.id.tvStockPercent);
            progressStock      = itemView.findViewById(R.id.progressStock);
            viewLowStockAccent = itemView.findViewById(R.id.viewLowStockAccent);
        }
    }
}

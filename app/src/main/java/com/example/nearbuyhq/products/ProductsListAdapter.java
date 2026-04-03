package com.example.nearbuyhq.products;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nearbuyhq.R;

import java.util.ArrayList;
import java.util.List;

// RecyclerView adapter for the Products List screen – renders each product card with stock badge and wires open/edit/delete actions.
public class ProductsListAdapter extends RecyclerView.Adapter<ProductsListAdapter.ProductViewHolder> {

    public interface ProductActionListener {
        void onOpen(ProductItem item);
        void onEdit(ProductItem item);
        void onDelete(ProductItem item);
    }

    private List<ProductItem> items;
    private final ProductActionListener listener;
    private Context context;

    public ProductsListAdapter(List<ProductItem> items, ProductActionListener listener) {
        this.items = new ArrayList<>(items);
        this.listener = listener;
    }

    public void updateItems(List<ProductItem> updatedItems) {
        this.items = new ArrayList<>(updatedItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductItem item = items.get(position);
        holder.tvProductName.setText(item.getName());
        holder.tvCategory.setText(item.getCategory());
        holder.tvPrice.setText(item.formattedPrice());
        holder.tvUnit.setText(" /" + item.getUnit());
        holder.tvStock.setText(item.getQuantity() + " units");

        if (item.isOutOfStock()) {
            holder.llStockBadge.setBackgroundResource(R.drawable.bg_stock_badge_low);
            holder.vStockDot.setBackgroundResource(R.drawable.bg_dot_red);
            holder.tvStock.setTextColor(Color.parseColor("#E03B2F"));
        } else if (item.isLowStock(10)) {
            holder.llStockBadge.setBackgroundResource(R.drawable.bg_stock_badge_low);
            holder.vStockDot.setBackgroundResource(R.drawable.bg_dot_red);
            holder.tvStock.setTextColor(Color.parseColor("#E03B2F"));
        } else {
            holder.llStockBadge.setBackgroundResource(R.drawable.bg_stock_badge_green);
            holder.vStockDot.setBackgroundResource(R.drawable.bg_dot_green);
            holder.tvStock.setTextColor(Color.parseColor("#1A7A5E"));
        }

        holder.cardRoot.setOnClickListener(v -> listener.onOpen(item));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));

        // Load product image with Glide
        String imageUrl = item.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_eco_leaf)
                    .error(R.drawable.ic_eco_leaf)
                    .centerCrop()
                    .into(holder.ivProductImage);
        } else {
            holder.ivProductImage.setImageResource(R.drawable.ic_eco_leaf);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        View cardRoot;
        TextView tvProductName;
        TextView tvCategory;
        TextView tvPrice;
        TextView tvUnit;
        TextView tvStock;
        LinearLayout llStockBadge;
        View vStockDot;
        View btnEdit;
        View btnDelete;
        ImageView ivProductImage;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot       = itemView.findViewById(R.id.cardRoot);
            tvProductName  = itemView.findViewById(R.id.tvProductName);
            tvCategory     = itemView.findViewById(R.id.tvCategory);
            tvPrice        = itemView.findViewById(R.id.tvPrice);
            tvUnit         = itemView.findViewById(R.id.tvUnit);
            tvStock        = itemView.findViewById(R.id.tvStock);
            llStockBadge   = itemView.findViewById(R.id.llStockBadge);
            vStockDot      = itemView.findViewById(R.id.vStockDot);
            btnEdit        = itemView.findViewById(R.id.btnEdit);
            btnDelete      = itemView.findViewById(R.id.btnDelete);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
        }
    }
}

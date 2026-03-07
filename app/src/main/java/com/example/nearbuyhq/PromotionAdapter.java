package com.example.nearbuyhq;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.ViewHolder> {

    private final List<Promotion> promotions;
    private final OnPromotionActionListener listener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displayFmt = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public interface OnPromotionActionListener {
        void onEdit(int position);
        void onDelete(int position);
    }

    public PromotionAdapter(List<Promotion> promotions, OnPromotionActionListener listener) {
        this.promotions = promotions;
        this.listener   = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_promotion, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(promotions.get(position));
    }

    @Override
    public int getItemCount() { return promotions.size(); }

    // ── ViewHolder ─────────────────────────────────────────────────────────

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView    tvEmoji, tvTitle, tvType, tvDiscount, tvProduct,
                    tvDateRange, tvOriginalPrice, tvDiscountedPrice, tvStatus;
        ImageButton btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvEmoji           = itemView.findViewById(R.id.tvPromoEmoji);
            tvTitle           = itemView.findViewById(R.id.tvPromoTitle);
            tvType            = itemView.findViewById(R.id.tvPromoType);
            tvDiscount        = itemView.findViewById(R.id.tvPromoDiscount);
            tvProduct         = itemView.findViewById(R.id.tvPromoProduct);
            tvDateRange       = itemView.findViewById(R.id.tvPromoDateRange);
            tvOriginalPrice   = itemView.findViewById(R.id.tvPromoOriginalPrice);
            tvDiscountedPrice = itemView.findViewById(R.id.tvPromoDiscountedPrice);
            tvStatus          = itemView.findViewById(R.id.tvPromoStatus);
            btnEdit           = itemView.findViewById(R.id.btnPromoEdit);
            btnDelete         = itemView.findViewById(R.id.btnPromoDelete);
        }

        void bind(Promotion p) {
            tvEmoji.setText(p.getTypeEmoji());
            tvTitle.setText(p.getTitle());
            tvType.setText(p.getType());
            tvDiscount.setText(p.getDiscountPercentage() + "% OFF");

            String product = (p.getProductName() == null || p.getProductName().isEmpty())
                    ? "All Products" : p.getProductName();
            tvProduct.setText(product);
            tvDateRange.setText(fmt(p.getStartDate()) + "  –  " + fmt(p.getEndDate()));

            if (p.getOriginalPrice() > 0) {
                // Strike-through original price
                String origTxt = String.format(Locale.getDefault(), "$%.2f", p.getOriginalPrice());
                SpannableString ss = new SpannableString(origTxt);
                ss.setSpan(new StrikethroughSpan(), 0, origTxt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvOriginalPrice.setText(ss);
                tvDiscountedPrice.setText(
                        String.format(Locale.getDefault(), "$%.2f", p.getDiscountedPrice()));
                tvOriginalPrice.setVisibility(View.VISIBLE);
                tvDiscountedPrice.setVisibility(View.VISIBLE);
            } else {
                tvOriginalPrice.setVisibility(View.GONE);
                tvDiscountedPrice.setVisibility(View.GONE);
            }

            // Status badge
            String status = computeStatus(p);
            tvStatus.setText(status);
            switch (status) {
                case "Active":
                    tvStatus.setBackgroundResource(R.drawable.bg_promo_badge_active);
                    break;
                case "Upcoming":
                    tvStatus.setBackgroundResource(R.drawable.bg_promo_badge_upcoming);
                    break;
                default:   // Expired / Inactive
                    tvStatus.setBackgroundResource(R.drawable.bg_promo_badge_expired);
            }

            btnEdit.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_ID) listener.onEdit(pos);
            });
            btnDelete.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_ID) listener.onDelete(pos);
            });
        }

        private String computeStatus(Promotion p) {
            if (!p.isActive()) return "Inactive";
            try {
                Date today = new Date();
                Date start = sdf.parse(p.getStartDate());
                Date end   = sdf.parse(p.getEndDate());
                if (start != null && today.before(start)) return "Upcoming";
                if (end   != null && today.after(end))    return "Expired";
                return "Active";
            } catch (ParseException e) {
                return "Active";
            }
        }

        private String fmt(String date) {
            try {
                Date d = sdf.parse(date);
                return d != null ? displayFmt.format(d) : date;
            } catch (ParseException e) {
                return date;
            }
        }
    }
}

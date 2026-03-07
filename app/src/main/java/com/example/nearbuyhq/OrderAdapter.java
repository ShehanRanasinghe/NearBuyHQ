package com.example.nearbuyhq;

import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    private final List<Order> orderList;
    private final Context context;
    private OnOrderClickListener clickListener;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    public void setOnOrderClickListener(OnOrderClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.tvOrderId.setText("ORD-" + order.getOrderId());
        holder.tvCustomerName.setText(order.getCustomerName());
        holder.tvOrderTotal.setText(String.format("$%.2f", order.getOrderTotal()));
        holder.tvOrderDate.setText(order.getOrderDate());
        holder.tvStatus.setText(order.getStatus());

        int color;
        switch (order.getStatus()) {
            case "Delivered":  color = Color.parseColor("#27AE60"); break;
            case "Processing": color = Color.parseColor("#2980B9"); break;
            case "Cancelled":  color = Color.parseColor("#C0392B"); break;
            default:           color = Color.parseColor("#F5A623"); break; // Pending
        }
        holder.tvStatus.getBackground().setTint(color);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onOrderClick(order);
        });
    }

    @Override public int getItemCount() { return orderList.size(); }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvCustomerName, tvStatus, tvOrderTotal, tvOrderDate;
        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId      = itemView.findViewById(R.id.tv_order_id);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvStatus       = itemView.findViewById(R.id.tv_order_status);
            tvOrderTotal   = itemView.findViewById(R.id.tv_order_total);
            tvOrderDate    = itemView.findViewById(R.id.tv_order_date);
        }
    }
}
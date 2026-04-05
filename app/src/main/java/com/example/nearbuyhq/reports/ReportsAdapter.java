package com.example.nearbuyhq.reports;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nearbuyhq.R;

import java.util.List;

public class  ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportViewHolder> {

    private List<Report> reportsList;
    private OnReportClickListener listener;

    public interface OnReportClickListener {
        void onReportClick(Report report);
    }

    public ReportsAdapter(List<Report> reportsList, OnReportClickListener listener) {
        this.reportsList = reportsList;
        this.listener    = listener;
    }

    /** Replace the full list and refresh the RecyclerView (called after Firestore load). */
    public void updateList(List<Report> newList) {
        this.reportsList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportsList.get(position);

        // Type badge
        String type = report.getType();
        holder.reportType.setText(type == null || type.isEmpty() ? "Feedback" : type);

        // Report text (main content from Firestore "reportText")
        String desc = report.getDescription();
        holder.reportDescription.setText(desc == null || desc.isEmpty() ? "—" : desc);

        // Customer name
        String customer = report.getCustomerName();
        holder.tvCustomerName.setText(customer == null || customer.isEmpty() ? "—" : customer);

        // Order reference (show short form)
        String orderRef = report.getOrderRef();
        if (orderRef == null || orderRef.isEmpty()) {
            holder.tvOrderRef.setText("—");
        } else {
            // Show first 16 chars of the order ID to keep it compact
            holder.tvOrderRef.setText(orderRef.length() > 16 ? orderRef.substring(0, 16) + "…" : orderRef);
        }

        // Status badge
        String status = report.getStatus();
        if (status == null || status.isEmpty()) status = "Open";
        holder.reportStatus.setText(status);

        if ("Resolved".equalsIgnoreCase(status) || "Closed".equalsIgnoreCase(status)) {
            holder.reportStatus.setTextColor(0xFF27AE60);
            holder.reportStatus.setBackgroundResource(R.drawable.bg_status_delivered);
        } else if ("Processing".equalsIgnoreCase(status)) {
            holder.reportStatus.setTextColor(0xFF2980B9);
            holder.reportStatus.setBackgroundResource(R.drawable.bg_status_processing);
        } else {
            holder.reportStatus.setTextColor(0xFFF5A623);
            holder.reportStatus.setBackgroundResource(R.drawable.bg_status_pending);
        }

        holder.itemView.setOnClickListener(v -> listener.onReportClick(report));
    }

    @Override
    public int getItemCount() {
        return reportsList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView reportType, reportDescription, reportStatus, tvCustomerName, tvOrderRef;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            reportType        = itemView.findViewById(R.id.reportType);
            reportDescription = itemView.findViewById(R.id.reportDescription);
            reportStatus      = itemView.findViewById(R.id.reportStatus);
            tvCustomerName    = itemView.findViewById(R.id.tv_customer_name);
            tvOrderRef        = itemView.findViewById(R.id.tv_order_ref);
        }
    }
}

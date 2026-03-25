package com.example.nearbuyhq.reports;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nearbuyhq.R;

import java.util.List;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportViewHolder> {

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
        holder.reportType.setText(report.getType());
        holder.reportSubject.setText(report.getSubject());
        holder.reportDescription.setText(report.getDescription());
        holder.reportStatus.setText(report.getStatus());

        // Set status color
        if ("Pending".equals(report.getStatus())) {
            holder.reportStatus.setTextColor(holder.itemView.getContext().getColor(R.color.warning_orange));
        } else {
            holder.reportStatus.setTextColor(holder.itemView.getContext().getColor(R.color.success_green));
        }

        holder.itemView.setOnClickListener(v -> listener.onReportClick(report));
    }

    @Override
    public int getItemCount() {
        return reportsList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView reportType, reportSubject, reportDescription, reportStatus;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            reportType = itemView.findViewById(R.id.reportType);
            reportSubject = itemView.findViewById(R.id.reportSubject);
            reportDescription = itemView.findViewById(R.id.reportDescription);
            reportStatus = itemView.findViewById(R.id.reportStatus);
        }
    }
}


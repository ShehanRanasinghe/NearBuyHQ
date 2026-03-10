package com.example.nearbuyhq.reports;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nearbuyhq.R;

import java.util.ArrayList;
import java.util.List;

public class Reports extends AppCompatActivity {

    private RecyclerView recyclerViewReports;
    private ReportsAdapter reportsAdapter;
    private List<Report> reportsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        // Hide ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        recyclerViewReports = findViewById(R.id.recyclerViewReports);

        // Initialize sample data
        initSampleReports();

        // Setup RecyclerView
        reportsAdapter = new ReportsAdapter(reportsList, this::onReportClick);
        recyclerViewReports.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReports.setAdapter(reportsAdapter);
    }

    private void initSampleReports() {
        reportsList = new ArrayList<>();
        reportsList.add(new Report("1", "Shop Violation", "Fresh Mart", "Inappropriate content", "Pending"));
        reportsList.add(new Report("2", "Deal Expired", "Tech Hub", "Deal not valid anymore", "Reviewed"));
        reportsList.add(new Report("3", "False Advertisement", "Fashion Plaza", "Misleading discount", "Pending"));
        reportsList.add(new Report("4", "User Complaint", "Coffee Corner", "Poor service", "Reviewed"));
    }

    private void onReportClick(Report report) {
        // You can add a detailed report view here
        Intent intent = new Intent(Reports.this, ReportDetails.class);
        intent.putExtra("report_id", report.getId());
        intent.putExtra("report_type", report.getType());
        intent.putExtra("report_subject", report.getSubject());
        intent.putExtra("report_description", report.getDescription());
        intent.putExtra("report_status", report.getStatus());
        startActivity(intent);
    }
}


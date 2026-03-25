package com.example.nearbuyhq.reports;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.firebase.FirebaseConfig;
import com.example.nearbuyhq.data.repository.DataCallback;
import com.example.nearbuyhq.data.repository.ReportRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reports screen – lists all shop/user reports submitted through the customer app.
 *
 * Reports are stored in the 'reports' Firestore collection.
 * Structure of each report document:
 *  - type        → "Shop Violation", "False Advertisement", etc.
 *  - subject     → the shop name involved
 *  - description → details from the reporter
 *  - shopId      → which shop the report is about
 *  - status      → "Pending" | "Reviewed" | "Resolved"
 *
 * Clicking a report opens ReportDetails where the admin can update its status.
 */
public class Reports extends AppCompatActivity {

    private RecyclerView recyclerViewReports;
    private ReportsAdapter reportsAdapter;
    private List<Report> reportsList;

    private ReportRepository reportRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        reportRepository = new ReportRepository();
        recyclerViewReports = findViewById(R.id.recyclerViewReports);

        // Start with an empty list – filled after Firestore returns data
        reportsList   = new ArrayList<>();
        reportsAdapter = new ReportsAdapter(reportsList, this::onReportClick);
        recyclerViewReports.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReports.setAdapter(reportsAdapter);

        // Load reports from Firestore
        loadReports();
    }

    // ── Load from Firestore ──────────────────────────────────────────────

    /**
     * Fetch all reports from Firestore, newest first.
     * Falls back to sample data if Firebase is disabled or unavailable.
     */
    private void loadReports() {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            showSampleReports();
            return;
        }

        reportRepository.getAllReports(new DataCallback<List<Map<String, Object>>>() {
            @Override
            public void onSuccess(List<Map<String, Object>> data) {
                List<Report> loaded = new ArrayList<>();
                for (Map<String, Object> map : data) {
                    // Convert each Firestore map into a Report object
                    loaded.add(new Report(
                            stringFrom(map, "id"),
                            stringFrom(map, "type"),
                            stringFrom(map, "subject"),
                            stringFrom(map, "description"),
                            stringFrom(map, "status")
                    ));
                }

                if (loaded.isEmpty()) {
                    showSampleReports();
                } else {
                    reportsAdapter.updateList(loaded);
                }
            }

            @Override
            public void onError(Exception exception) {
                Toast.makeText(Reports.this,
                        "Could not load reports", Toast.LENGTH_SHORT).show();
                showSampleReports();
            }
        });
    }

    // ── Report Click ─────────────────────────────────────────────────────

    /**
     * Open the ReportDetails screen, passing the clicked report's data as extras.
     * The details screen lets the admin update the status (Pending → Reviewed → Resolved).
     */
    private void onReportClick(Report report) {
        Intent intent = new Intent(this, ReportDetails.class);
        intent.putExtra("report_id",          report.getId());
        intent.putExtra("report_type",        report.getType());
        intent.putExtra("report_subject",     report.getSubject());
        intent.putExtra("report_description", report.getDescription());
        intent.putExtra("report_status",      report.getStatus());
        startActivity(intent);
    }

    // ── Sample / Fallback Data ───────────────────────────────────────────

    /** Show placeholder reports when Firestore is unavailable. */
    private void showSampleReports() {
        List<Report> samples = new ArrayList<>();
        samples.add(new Report("1", "Shop Violation",     "Fresh Mart",    "Inappropriate content",   "Pending"));
        samples.add(new Report("2", "Deal Expired",       "Tech Hub",      "Deal not valid anymore",  "Reviewed"));
        samples.add(new Report("3", "False Advertisement","Fashion Plaza", "Misleading discount",     "Pending"));
        samples.add(new Report("4", "User Complaint",     "Coffee Corner", "Poor service",            "Reviewed"));
        reportsAdapter.updateList(samples);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private String stringFrom(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val == null ? "" : String.valueOf(val).trim();
    }
}

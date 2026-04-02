package com.example.nearbuyhq.reports;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.SessionManager;
import com.example.nearbuyhq.data.repository.DataCallback;
import com.example.nearbuyhq.data.repository.ReportRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reports screen – lists all shop/user reports submitted through the customer app.
 * Reports are stored in the 'reports' Firestore collection.
 */
public class Reports extends AppCompatActivity {

    private RecyclerView recyclerViewReports;
    private ReportsAdapter reportsAdapter;
    private List<Report> allReports = new ArrayList<>();
    private TextView txtEmptyReports;
    private ImageView btnBack;
    private String searchQuery = "";

    private ReportRepository reportRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        reportRepository = new ReportRepository();

        recyclerViewReports = findViewById(R.id.recyclerViewReports);
        txtEmptyReports     = findViewById(R.id.txtEmptyReports);
        btnBack             = findViewById(R.id.btn_back);

        // Wire back button
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        reportsAdapter = new ReportsAdapter(new ArrayList<>(), this::onReportClick);
        recyclerViewReports.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReports.setAdapter(reportsAdapter);

        // Wire search
        EditText etSearch = findViewById(R.id.etSearchReports);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                    searchQuery = s.toString().trim().toLowerCase();
                    applySearch();
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        loadReports();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReports();
    }

    // ── Load from Firestore ───────────────────────────────────────────────

    private void loadReports() {
        String userId = SessionManager.getInstance(this).getUserId();
        reportRepository.getReportsByShop(userId, new DataCallback<List<Map<String, Object>>>() {
            @Override
            public void onSuccess(List<Map<String, Object>> data) {
                allReports.clear();
                for (Map<String, Object> map : data) {
                    // Support multiple field-naming conventions used by different apps
                    String id   = firstFrom(map, "id", "reportId", "report_id");

                    // Main report text – Firestore stores it as "reportText"
                    String desc = firstFrom(map, "reportText", "description", "reportDescription",
                            "reportMessage", "details", "message", "body", "report_description");

                    // No dedicated subject field in Firestore – derive from reportText
                    String subj = firstFrom(map, "subject", "reportSubject", "reportTitle", "title");
                    if (subj.isEmpty() && !desc.isEmpty()) {
                        subj = desc.length() > 45 ? desc.substring(0, 45) + "…" : desc;
                    }

                    // No type field in Firestore – default to "Feedback"
                    String type = firstFrom(map, "type", "reportType", "report_type", "category");
                    if (type.isEmpty()) type = "Feedback";

                    // No status field in Firestore – default to "Open"
                    String status = firstFrom(map, "status", "state", "reportStatus");
                    if (status.isEmpty()) status = "Open";

                    String customerName = firstFrom(map, "customerName", "customer_name", "name");
                    String orderRef     = firstFrom(map, "orderId", "order_id", "orderRef");

                    allReports.add(new Report(id, type, subj, desc, status, customerName, orderRef));
                }
                applySearch();
            }

            @Override
            public void onError(Exception exception) {
                Toast.makeText(Reports.this, "Could not load reports", Toast.LENGTH_SHORT).show();
                showEmptyState(true);
            }
        });
    }

    private void applySearch() {
        if (searchQuery.isEmpty()) {
            reportsAdapter.updateList(new ArrayList<>(allReports));
            showEmptyState(allReports.isEmpty());
            return;
        }
        List<Report> filtered = new ArrayList<>();
        for (Report r : allReports) {
            if ((r.getType()    != null && r.getType().toLowerCase().contains(searchQuery))
             || (r.getSubject() != null && r.getSubject().toLowerCase().contains(searchQuery))
             || (r.getStatus()  != null && r.getStatus().toLowerCase().contains(searchQuery))) {
                filtered.add(r);
            }
        }
        reportsAdapter.updateList(filtered);
        showEmptyState(filtered.isEmpty());
    }

    private void showEmptyState(boolean empty) {
        if (txtEmptyReports != null) {
            txtEmptyReports.setVisibility(empty ? View.VISIBLE : View.GONE);
        }
    }

    // ── Report click ──────────────────────────────────────────────────────

    private void onReportClick(Report report) {
        Intent intent = new Intent(this, ReportDetails.class);
        intent.putExtra("report_id",            report.getId());
        intent.putExtra("report_type",          report.getType());
        intent.putExtra("report_subject",       report.getSubject());
        intent.putExtra("report_description",   report.getDescription());
        intent.putExtra("report_status",        report.getStatus());
        intent.putExtra("report_customer_name", report.getCustomerName());
        intent.putExtra("report_order_ref",     report.getOrderRef());
        startActivity(intent);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /** Return the first non-empty value found under any of the given keys. */
    private String firstFrom(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object val = map.get(key);
            if (val != null) {
                String s = String.valueOf(val).trim();
                if (!s.isEmpty()) return s;
            }
        }
        return "";
    }

    private String stringFrom(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val == null ? "" : String.valueOf(val).trim();
    }
}

package com.example.nearbuyhq.reports;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.SessionManager;
import com.example.nearbuyhq.data.repository.OperationCallback;
import com.example.nearbuyhq.data.repository.ReportRepository;

public class ReportDetails extends AppCompatActivity {

    private TextView reportType, reportSubject, reportDescription, reportStatus;
    private Button btnResolve, btnBack;
    private ReportRepository reportRepository;
    private String reportId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details);

        // Hide ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        reportRepository = new ReportRepository();

        reportType = findViewById(R.id.reportType);
        reportSubject = findViewById(R.id.reportSubject);
        reportDescription = findViewById(R.id.reportDescription);
        reportStatus = findViewById(R.id.reportStatus);
        btnResolve = findViewById(R.id.btnResolve);
        btnBack = findViewById(R.id.btnBack);

        reportId = getIntent().getStringExtra("report_id");
        String type = getIntent().getStringExtra("report_type");
        String subject = getIntent().getStringExtra("report_subject");
        String description = getIntent().getStringExtra("report_description");
        String status = getIntent().getStringExtra("report_status");

        // Set data
        reportType.setText(type != null ? "Type: " + type : "Type: —");
        reportSubject.setText(subject != null ? "Subject: " + subject : "Subject: —");
        reportDescription.setText(description != null ? "Description: " + description : "Description: —");
        reportStatus.setText(status != null ? "Status: " + status : "Status: —");

        // Disable Resolve button if already resolved
        if ("Resolved".equalsIgnoreCase(status)) {
            btnResolve.setEnabled(false);
            btnResolve.setAlpha(0.5f);
        }

        btnResolve.setOnClickListener(v -> resolveReport());
        btnBack.setOnClickListener(v -> finish());
    }

    private void resolveReport() {
        if (reportId == null || reportId.isEmpty()) {
            Toast.makeText(this, "Report Resolved (local)", Toast.LENGTH_SHORT).show();
            reportStatus.setText("Status: Resolved");
            btnResolve.setEnabled(false);
            btnResolve.setAlpha(0.5f);
            return;
        }

        btnResolve.setEnabled(false);
        String userId = SessionManager.getInstance(this).getUserId();
        reportRepository.updateStatus(reportId, userId, "Resolved", new OperationCallback() {
            @Override
            public void onSuccess() {
                reportStatus.setText("Status: Resolved");
                btnResolve.setAlpha(0.5f);
                Toast.makeText(ReportDetails.this, "Report marked as Resolved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                btnResolve.setEnabled(true);
                Toast.makeText(ReportDetails.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

package com.example.nearbuyhq.reports;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.SessionManager;
import com.example.nearbuyhq.data.repository.OperationCallback;
import com.example.nearbuyhq.data.repository.ReportRepository;

public class ReportDetails extends AppCompatActivity {

    private TextView reportType, reportDescription, reportStatus;
    private TextView tvCustomerName, tvOrderRef;
    private Button btnResolve, btnBack;
    private ReportRepository reportRepository;
    private String reportId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        reportRepository = new ReportRepository();

        reportType        = findViewById(R.id.reportType);
        reportDescription = findViewById(R.id.reportDescription);
        reportStatus      = findViewById(R.id.reportStatus);
        tvCustomerName    = findViewById(R.id.tv_customer_name);
        tvOrderRef        = findViewById(R.id.tv_order_ref);
        btnResolve        = findViewById(R.id.btnResolve);

        // Header back arrow (ImageView) + footer Back button (Button) both finish the activity
        ImageView imgBack = findViewById(R.id.btn_back);
        if (imgBack != null) imgBack.setOnClickListener(v -> finish());
        btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        reportId = getIntent().getStringExtra("report_id");

        String type         = getIntent().getStringExtra("report_type");
        String description  = getIntent().getStringExtra("report_description");
        String status       = getIntent().getStringExtra("report_status");
        String customerName = getIntent().getStringExtra("report_customer_name");
        String orderRef     = getIntent().getStringExtra("report_order_ref");

        // Type badge
        reportType.setText((type != null && !type.isEmpty()) ? type : "Feedback");

        // Report message (the actual reportText from Firestore)
        reportDescription.setText((description != null && !description.isEmpty()) ? description : "—");

        // Customer name
        tvCustomerName.setText((customerName != null && !customerName.isEmpty()) ? customerName : "—");

        // Order reference
        tvOrderRef.setText((orderRef != null && !orderRef.isEmpty()) ? orderRef : "—");

        // Status badge
        applyStatus(status != null && !status.isEmpty() ? status : "Open");

        btnResolve.setOnClickListener(v -> resolveReport());
    }

    private void applyStatus(String status) {
        reportStatus.setText(status);
        if ("Resolved".equalsIgnoreCase(status) || "Closed".equalsIgnoreCase(status)) {
            reportStatus.setTextColor(0xFF27AE60);
            reportStatus.setBackgroundResource(R.drawable.bg_status_delivered);
            btnResolve.setEnabled(false);
            btnResolve.setAlpha(0.5f);
        } else {
            reportStatus.setTextColor(0xFFF5A623);
            reportStatus.setBackgroundResource(R.drawable.bg_status_pending);
            btnResolve.setEnabled(true);
            btnResolve.setAlpha(1f);
        }
    }

    private void resolveReport() {
        if (reportId == null || reportId.isEmpty()) {
            applyStatus("Resolved");
            Toast.makeText(this, "Report marked as Resolved", Toast.LENGTH_SHORT).show();
            return;
        }
        btnResolve.setEnabled(false);
        String userId = SessionManager.getInstance(this).getUserId();
        reportRepository.updateStatus(reportId, userId, "Resolved", new OperationCallback() {
            @Override public void onSuccess() {
                applyStatus("Resolved");
                Toast.makeText(ReportDetails.this, "Report marked as Resolved", Toast.LENGTH_SHORT).show();
            }
            @Override public void onError(Exception e) {
                btnResolve.setEnabled(true);
                Toast.makeText(ReportDetails.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

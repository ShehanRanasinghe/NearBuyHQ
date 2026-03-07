package com.example.nearbuyhq;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ReportDetails extends AppCompatActivity {

    private TextView reportType, reportSubject, reportDescription, reportStatus;
    private Button btnResolve, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details);

        // Hide ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        reportType = findViewById(R.id.reportType);
        reportSubject = findViewById(R.id.reportSubject);
        reportDescription = findViewById(R.id.reportDescription);
        reportStatus = findViewById(R.id.reportStatus);
        btnResolve = findViewById(R.id.btnResolve);
        btnBack = findViewById(R.id.btnBack);

        // Get data from intent
        String type = getIntent().getStringExtra("report_type");
        String subject = getIntent().getStringExtra("report_subject");
        String description = getIntent().getStringExtra("report_description");
        String status = getIntent().getStringExtra("report_status");

        // Set data
        reportType.setText("Type: " + type);
        reportSubject.setText("Subject: " + subject);
        reportDescription.setText("Description: " + description);
        reportStatus.setText("Status: " + status);

        btnResolve.setOnClickListener(v -> {
            Toast.makeText(this, "Report Resolved", Toast.LENGTH_SHORT).show();
            reportStatus.setText("Status: Resolved");
        });

        btnBack.setOnClickListener(v -> finish());
    }
}


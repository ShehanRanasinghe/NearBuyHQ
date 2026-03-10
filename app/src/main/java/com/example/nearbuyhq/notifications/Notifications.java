package com.example.nearbuyhq.notifications;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nearbuyhq.R;

import java.util.ArrayList;
import java.util.List;

public class Notifications extends AppCompatActivity {

    private RecyclerView recyclerViewNotifications;
    private NotificationsAdapter notificationsAdapter;
    private List<Notification> notificationsList;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Hide ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);
        btnBack = findViewById(R.id.btnBack);

        // Initialize sample data
        initSampleNotifications();

        // Setup RecyclerView
        notificationsAdapter = new NotificationsAdapter(notificationsList);
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNotifications.setAdapter(notificationsAdapter);

        // Back button click listener
        btnBack.setOnClickListener(v -> finish());
    }

    private void initSampleNotifications() {
        notificationsList = new ArrayList<>();
        notificationsList.add(new Notification("New Shop Registration", "Fresh Mart has registered on the platform", "2 hours ago"));
        notificationsList.add(new Notification("Deal Expiring Soon", "50% Off on Groceries expires today", "5 hours ago"));
        notificationsList.add(new Notification("Shop Approval Needed", "Tech Hub is waiting for approval", "1 day ago"));
        notificationsList.add(new Notification("New Report", "User reported Fashion Plaza", "2 days ago"));
        notificationsList.add(new Notification("System Alert", "Database backup completed successfully", "3 days ago"));
    }
}


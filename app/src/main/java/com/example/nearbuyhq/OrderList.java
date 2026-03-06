package com.example.nearbuyhq;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;
import java.util.stream.Collectors;

public class OrderList extends AppCompatActivity {

    private RecyclerView recyclerOrders;
    private OrderAdapter adapter;
    private List<Order> allOrders = new ArrayList<>();
    private TextView tabAll, tabPending, tabProcessing, tabDelivered;
    private TextView tvTotalOrders, tvPendingCount, tvDeliveredCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        loadSampleData();
        setupRecyclerView();
        setupFilterTabs();
        updateSummaryChips();
    }

    private void initViews() {
        recyclerOrders   = findViewById(R.id.recycler_orders);
        tabAll           = findViewById(R.id.tab_all);
        tabPending       = findViewById(R.id.tab_pending);
        tabProcessing    = findViewById(R.id.tab_processing);
        tabDelivered     = findViewById(R.id.tab_delivered);
        tvTotalOrders    = findViewById(R.id.tv_total_orders);
        tvPendingCount   = findViewById(R.id.tv_pending_count);
        tvDeliveredCount = findViewById(R.id.tv_delivered_count);
    }

    private void loadSampleData() {
        allOrders.add(new Order("00123", "Sanduni Saumya",     "Pending",    24.50, "Mar 04, 2026"));
        allOrders.add(new Order("00124", "Hiruni Pramodya", "Processing", 18.00, "Mar 04, 2026"));
        allOrders.add(new Order("00125", "Sithara Kavindi",  "Delivered",  45.75, "Mar 03, 2026"));
        allOrders.add(new Order("00126", "Thiloka Indiwari",   "Pending",    12.30, "Mar 03, 2026"));
        allOrders.add(new Order("00127", "Vihangi",  "Delivered",  33.00, "Mar 02, 2026"));
        allOrders.add(new Order("00128", "Shehan",   "Cancelled",   8.90, "Mar 02, 2026"));
        allOrders.add(new Order("00129", "Mahi",  "Processing", 55.20, "Mar 01, 2026"));
        allOrders.add(new Order("00130", "Lakeesha",  "Delivered",  29.40, "Mar 01, 2026"));
        allOrders.add(new Order("00131", "Abriru",     "Pending",    16.60, "Feb 28, 2026"));
        allOrders.add(new Order("00132", "Senuka",  "Delivered",  22.10, "Feb 28, 2026"));
        allOrders.add(new Order("00133", "Wathsal",  "Pending",    37.80, "Feb 27, 2026"));
        allOrders.add(new Order("00134", "Ayuka",    "Delivered",  14.25, "Feb 27, 2026"));
    }

    private void setupRecyclerView() {
        adapter = new OrderAdapter(this, new ArrayList<>(allOrders));
        adapter.setOnOrderClickListener(order ->
                Toast.makeText(this, "Opened: Order " + order.getOrderId(), Toast.LENGTH_SHORT).show());
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setAdapter(adapter);
    }

    private void setupFilterTabs() {
        tabAll.setOnClickListener(v        -> filterOrders(null,         tabAll));
        tabPending.setOnClickListener(v    -> filterOrders("Pending",    tabPending));
        tabProcessing.setOnClickListener(v -> filterOrders("Processing", tabProcessing));
        tabDelivered.setOnClickListener(v  -> filterOrders("Delivered",  tabDelivered));
    }

    private void filterOrders(String status, TextView selected) {
        for (TextView t : new TextView[]{tabAll, tabPending, tabProcessing, tabDelivered}) resetTab(t);
        activateTab(selected);
        List<Order> filtered = status == null ? new ArrayList<>(allOrders)
                : allOrders.stream().filter(o -> o.getStatus().equals(status)).collect(Collectors.toList());
        adapter = new OrderAdapter(this, filtered);
        adapter.setOnOrderClickListener(order ->
                Toast.makeText(this, "Opened: Order " + order.getOrderId(), Toast.LENGTH_SHORT).show());
        recyclerOrders.setAdapter(adapter);
    }

    private void activateTab(TextView tab) {
        tab.setBackground(getDrawable(R.drawable.bg_tab_active));
        tab.setTextColor(Color.WHITE);
        tab.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void resetTab(TextView tab) {
        tab.setBackground(getDrawable(R.drawable.bg_tab_inactive));
        tab.setTextColor(Color.parseColor("#9AA0AC"));
        tab.setTypeface(null, android.graphics.Typeface.NORMAL);
    }

    private void updateSummaryChips() {
        tvTotalOrders.setText(String.valueOf(allOrders.size()));
        tvPendingCount.setText(String.valueOf(
                allOrders.stream().filter(o -> o.getStatus().equals("Pending")).count()));
        tvDeliveredCount.setText(String.valueOf(
                allOrders.stream().filter(o -> o.getStatus().equals("Delivered")).count()));
    }
}
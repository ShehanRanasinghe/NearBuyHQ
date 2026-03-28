package com.example.nearbuyhq.dashboard;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.SessionManager;
import com.example.nearbuyhq.data.repository.DataCallback;
import com.example.nearbuyhq.data.repository.OrderRepository;
import com.example.nearbuyhq.orders.Order;
import com.example.nearbuyhq.orders.Order_List;
import com.example.nearbuyhq.products.Inventory;
import com.example.nearbuyhq.settings.ProfilePage;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Analytics screen – shows business overview statistics loaded live from Firestore.
 *
 * Metrics shown:
 *  - Total Revenue (sum of all Delivered order totals)
 *  - Total Sales   (count of Delivered orders)
 */
public class Analytics extends AppCompatActivity {

    // ── Stat TextViews ────────────────────────────────────────────────────
    private TextView txtTotalRevenue, txtTotalSales;

    // ── Sales Bar Chart ───────────────────────────────────────────────────
    private BarChart salesBarChart;

    // ── Bottom navigation ─────────────────────────────────────────────────
    private LinearLayout navDashboard, navProducts, navOrders, navAnalytics, navProfile;
    private ImageView navDashboardIcon, navProductsIcon, navOrdersIcon, navAnalyticsIcon, navProfileIcon;
    private TextView navDashboardText, navProductsText, navOrdersText, navAnalyticsText, navProfileText;

    // ── Repositories ──────────────────────────────────────────────────────
    private OrderRepository orderRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        orderRepository = new OrderRepository();

        // Stat cards
        txtTotalRevenue = findViewById(R.id.txtTotalRevenue);
        txtTotalSales   = findViewById(R.id.txtTotalSales);

        // Bar chart
        salesBarChart = findViewById(R.id.salesBarChart);

        // Bottom navigation
        navDashboard = findViewById(R.id.navDashboard);
        navProducts  = findViewById(R.id.navProducts);
        navOrders    = findViewById(R.id.navOrders);
        navAnalytics = findViewById(R.id.navAnalytics);
        navProfile   = findViewById(R.id.navProfile);

        navDashboardIcon = findViewById(R.id.navDashboardIcon);
        navProductsIcon  = findViewById(R.id.navProductsIcon);
        navOrdersIcon    = findViewById(R.id.navOrdersIcon);
        navAnalyticsIcon = findViewById(R.id.navAnalyticsIcon);
        navProfileIcon   = findViewById(R.id.navProfileIcon);

        navDashboardText = findViewById(R.id.navDashboardText);
        navProductsText  = findViewById(R.id.navProductsText);
        navOrdersText    = findViewById(R.id.navOrdersText);
        navAnalyticsText = findViewById(R.id.navAnalyticsText);
        navProfileText   = findViewById(R.id.navProfileText);

        // Back button
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Highlight Analytics as active
        setNavActive(navAnalyticsIcon, navAnalyticsText);

        setupBottomNavigation();
        loadAnalyticsData();
    }

    // ── Load from Firebase ────────────────────────────────────────────────

    /**
     * Load orders from Firestore and compute all four metrics.
     * Also loads the total customer count from the users collection.
     */
    private void loadAnalyticsData() {
        // ── Order-based stats ─────────────────────────────────────────
        String userId = SessionManager.getInstance(this).getUserId();
        orderRepository.getOrdersByShopId(userId, new DataCallback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> orders) {
                double totalRevenue = 0;
                int    deliveredCount = 0;

                // --- compute start-of-week (Monday 00:00:00) in millis ---
                Calendar weekCal = Calendar.getInstance();
                weekCal.setFirstDayOfWeek(Calendar.MONDAY);
                int dow = weekCal.get(Calendar.DAY_OF_WEEK);
                int daysFromMon = (dow == Calendar.SUNDAY) ? 6 : dow - Calendar.MONDAY;
                weekCal.add(Calendar.DAY_OF_MONTH, -daysFromMon);
                weekCal.set(Calendar.HOUR_OF_DAY, 0);
                weekCal.set(Calendar.MINUTE, 0);
                weekCal.set(Calendar.SECOND, 0);
                weekCal.set(Calendar.MILLISECOND, 0);
                long weekStart = weekCal.getTimeInMillis();
                long weekEnd   = weekStart + 7L * 24 * 60 * 60 * 1000;

                // dailyRevenue[0]=Mon … dailyRevenue[6]=Sun
                float[] dailyRevenue = new float[7];

                for (Order o : orders) {
                    if ("Delivered".equalsIgnoreCase(o.getStatus())) {
                        totalRevenue += o.getOrderTotal();
                        deliveredCount++;
                    }
                    long ts = o.getCreatedAt();
                    if (ts > 0 && ts >= weekStart && ts < weekEnd) {
                        Calendar oc = Calendar.getInstance();
                        oc.setTimeInMillis(ts);
                        // Calendar.SUNDAY=1, MONDAY=2 … SATURDAY=7
                        // index: (dayOfWeek + 5) % 7  →  Mon=0 … Sun=6
                        int idx = (oc.get(Calendar.DAY_OF_WEEK) + 5) % 7;
                        dailyRevenue[idx] += (float) o.getOrderTotal();
                    }
                }

                final double revenue = totalRevenue;
                final int    sales   = deliveredCount;
                final float[] weeklyData = dailyRevenue;

                runOnUiThread(() -> {
                    if (txtTotalRevenue != null)
                        txtTotalRevenue.setText(String.format(Locale.US, "Rs. %.0f", revenue));
                    if (txtTotalSales != null)
                        txtTotalSales.setText(String.valueOf(sales));
                    renderBarChart(weeklyData);
                });
            }
            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> renderBarChart(new float[7]));
            }
        });
    }

    // ── Bar Chart ─────────────────────────────────────────────────────────

    private void renderBarChart(float[] dailyRevenue) {
        if (salesBarChart == null) return;

        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < dailyRevenue.length; i++) {
            entries.add(new BarEntry(i, dailyRevenue[i]));
        }

        int barColor = ContextCompat.getColor(this, R.color.stat_green);
        int textColor = ContextCompat.getColor(this, R.color.text_dark_secondary);

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(barColor);
        dataSet.setValueTextColor(textColor);
        dataSet.setValueTextSize(9f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value == 0f ? "" : String.format(Locale.US, "%.0f", value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.55f);

        // X-axis
        XAxis xAxis = salesBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(textColor);
        xAxis.setTextSize(11f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(
                new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}));

        // Left Y-axis
        YAxis leftAxis = salesBarChart.getAxisLeft();
        leftAxis.setTextColor(textColor);
        leftAxis.setTextSize(10f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#1A000000"));
        leftAxis.setAxisMinimum(0f);

        // Disable right Y-axis
        salesBarChart.getAxisRight().setEnabled(false);

        // Chart appearance
        salesBarChart.setData(barData);
        salesBarChart.getDescription().setEnabled(false);
        salesBarChart.getLegend().setEnabled(false);
        salesBarChart.setDrawGridBackground(false);
        salesBarChart.setDrawBorders(false);
        salesBarChart.setFitBars(true);
        salesBarChart.setExtraBottomOffset(6f);
        salesBarChart.animateY(800);
        salesBarChart.invalidate();
    }

    // ── Navigation ────────────────────────────────────────────────────────

    private void setupBottomNavigation() {
        View.OnClickListener navClickListener = v -> {
            resetNavSelection();
            int id = v.getId();
            if (id == R.id.navDashboard) {
                setNavActive(navDashboardIcon, navDashboardText);
                Intent intent = new Intent(Analytics.this, Dashboard.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } else if (id == R.id.navProducts) {
                setNavActive(navProductsIcon, navProductsText);
                startActivity(new Intent(Analytics.this, Inventory.class));
            } else if (id == R.id.navOrders) {
                setNavActive(navOrdersIcon, navOrdersText);
                startActivity(new Intent(Analytics.this, Order_List.class));
            } else if (id == R.id.navAnalytics) {
                setNavActive(navAnalyticsIcon, navAnalyticsText);
            } else if (id == R.id.navProfile) {
                setNavActive(navProfileIcon, navProfileText);
                startActivity(new Intent(Analytics.this, ProfilePage.class));
            }
        };

        navDashboard.setOnClickListener(navClickListener);
        navProducts.setOnClickListener(navClickListener);
        navOrders.setOnClickListener(navClickListener);
        navAnalytics.setOnClickListener(navClickListener);
        navProfile.setOnClickListener(navClickListener);
    }

    private void resetNavSelection() {
        int inactive = ContextCompat.getColor(this, R.color.nav_inactive);
        navDashboardIcon.setColorFilter(inactive); navProductsIcon.setColorFilter(inactive);
        navOrdersIcon.setColorFilter(inactive);    navAnalyticsIcon.setColorFilter(inactive);
        navProfileIcon.setColorFilter(inactive);
        navDashboardText.setTextColor(inactive);   navProductsText.setTextColor(inactive);
        navOrdersText.setTextColor(inactive);      navAnalyticsText.setTextColor(inactive);
        navProfileText.setTextColor(inactive);
        navDashboardText.setTypeface(null); navProductsText.setTypeface(null);
        navOrdersText.setTypeface(null);    navAnalyticsText.setTypeface(null);
        navProfileText.setTypeface(null);
    }

    private void setNavActive(ImageView icon, TextView text) {
        int active = ContextCompat.getColor(this, R.color.coral_primary);
        icon.setColorFilter(active);
        text.setTextColor(active);
        text.setTypeface(null, Typeface.BOLD);
    }
}

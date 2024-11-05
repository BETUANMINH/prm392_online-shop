package com.example.prm392_project.adminActivities;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_project.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticActivity extends AppCompatActivity {

    private PieChart orderChart;
    private LineChart saleChart;

    private int cancelledOrder;
    private int pendingOrder;
    private int processingOrder;
    private int shippingOrder;
    private int deliveredOrder;

    private Map<String, Float> sales = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        orderChart = findViewById(R.id.orderChart);
        saleChart = findViewById(R.id.saleChart);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference orderRef = database.getReference("Orders");

        // setup and load data for Order chart, Sale chart
        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cancelledOrder = 0;
                pendingOrder = 0;
                processingOrder = 0;
                shippingOrder = 0;
                deliveredOrder = 0;

                sales.clear();

                for (DataSnapshot order : snapshot.getChildren()) {
                    // Count order's status for OrderChart
                    String status = order.child("status").getValue(String.class);
                    Log.d("ORDER_STATUS", status);
                    if ("Pending".equals(status)) {
                        ++pendingOrder;
                    } else if ("Processing".equals(status)) {
                        ++processingOrder;
                    } else if ("Shipping".equals(status)) {
                        ++shippingOrder;
                    } else if ("Delivered".equals(status)) {
                        ++deliveredOrder;
                    } else if ("Cancelled".equals(status)) {
                        ++cancelledOrder;
                    }

                    // Prepare data for SaleChart (separate for readability)
                    if ("Delivered".equals(status)) {
                        String date = order.child("date").getValue(String.class);
                        Float totalAmount = order.child("totalAmount").getValue(Float.class);

                        // Accumulate total sales per day
                        if (!sales.containsKey(date)) {
                            sales.put(date, totalAmount);
                        } else {
                            sales.put(date, totalAmount + sales.get(date));
                        }
                    }
                }

                setupOrderChart();
                loadOrderChartData();

                setupSaleChart();
                loadSaleChartData();

                Log.d("ORDER_STATUS", "Pending " + pendingOrder + ", processing " + processingOrder + ", shipped " + shippingOrder + ", delivered " + deliveredOrder + ", cancelled orders " + cancelledOrder);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        setupOrderChart();
//        loadOrderChartData();
    }

    private void setupSaleChart() {
        saleChart.getDescription().setEnabled(false);
        saleChart.setDrawGridBackground(false);
        saleChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
    }

    private void loadSaleChartData() {
        // Convert dailySalesMap to chart data
        List<Entry> entries = new ArrayList<>();
        List<String> dateLabels = new ArrayList<>();

        SimpleDateFormat originalFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());

        // Sort the date labels and format them as "dd/MM"
        List<String> sortedDates = new ArrayList<>(sales.keySet());
        sortedDates.sort((d1, d2) -> {
            try {
                return originalFormat.parse(d1).compareTo(originalFormat.parse(d2));
            } catch (ParseException e) {
                return 0;
            }
        });

        // Populate entries with x = index and y = total amount for each date
        for (int i = 0; i < sortedDates.size(); i++) {
            String originalDate = sortedDates.get(i);
            String formattedDate;

            try {
                // Format date to "dd/MM"
                formattedDate = displayFormat.format(originalFormat.parse(originalDate));
            } catch (ParseException e) {
                formattedDate = originalDate; // Use original if parsing fails
            }

            dateLabels.add(formattedDate);
            entries.add(new Entry(i, sales.get(originalDate)));
        }

        // Update Line Chart
        LineDataSet dataSet = new LineDataSet(entries, "Total Sales (Delivered)");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleColor(Color.RED);

        LineData lineData = new LineData(dataSet);
        saleChart.setData(lineData);

        // Set x-axis labels as dates
        XAxis xAxis = saleChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dateLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f); // one label per entry
        xAxis.setLabelRotationAngle(-45); // Rotate labels for better readability

        // Refresh chart
        saleChart.invalidate();
    }

    // Helper method to get the last 7 days as dates in "MM/dd" format
    private String[] getLast7Dates() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
        String[] dates = new String[7];
        Calendar calendar = Calendar.getInstance();

        // Populate the array with formatted date strings, starting from today and going backwards
        for (int i = 6; i >= 0; i--) {
            dates[i] = dateFormat.format(calendar.getTime());
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }
        return dates;
    }

    private void setupOrderChart() {
        orderChart.setUsePercentValues(true);
        orderChart.getDescription().setEnabled(false);
        orderChart.setCenterText("Order Status");
        orderChart.setCenterTextSize(22);
        orderChart.setDrawHoleEnabled(true);
        orderChart.setHoleColor(Color.WHITE);
        orderChart.setTransparentCircleColor(Color.WHITE);
        orderChart.setTransparentCircleAlpha(110);
        orderChart.setHoleRadius(58f);
        orderChart.setTransparentCircleRadius(61f);
        orderChart.setRotationEnabled(true);
        orderChart.setHighlightPerTapEnabled(true);
        orderChart.setEntryLabelTextSize(12);
    }

    private void loadOrderChartData() {
        List<PieEntry> entries = new ArrayList<>();
        if (pendingOrder > 0) entries.add(new PieEntry(pendingOrder, "Pending"));
        if (processingOrder > 0) entries.add(new PieEntry(processingOrder, "Processing"));
        if (shippingOrder > 0) entries.add(new PieEntry(shippingOrder, "Shipping"));
        if (deliveredOrder > 0) entries.add(new PieEntry(deliveredOrder, "Delivered"));
        if (cancelledOrder > 0) entries.add(new PieEntry(cancelledOrder, "Cancelled"));

        PieDataSet dataSet = new PieDataSet(entries, "Order Status");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);

        orderChart.setData(data);
        orderChart.invalidate(); // Refresh the chart
    }
}
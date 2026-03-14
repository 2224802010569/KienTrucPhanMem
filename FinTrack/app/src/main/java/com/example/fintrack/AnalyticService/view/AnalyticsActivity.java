package com.example.fintrack.AnalyticService.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fintrack.AnalyticService.data.AnalyticsRepository;
import com.example.fintrack.AnalyticService.entity.AnalyticsData;
import com.example.fintrack.AnalyticService.service.AnalyticsDomainService;
import com.example.fintrack.R;
import com.example.fintrack.TransactionService.data.db.FintrackDatabase;
import com.example.fintrack.TransactionService.data.entity.TransactionEntity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.*;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsActivity extends AppCompatActivity {

    PieChart pieChart;
    BarChart barChart;
    RecyclerView recyclerView;

    TextView txtInsight, txtBalance;
    Button btnLimit;

    // ⭐ NÚT BACK
    ImageButton btnBack;

    AnalyticsRepository repo;
    AnalyticsDomainService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
        recyclerView = findViewById(R.id.recyclerCategory);
        txtInsight = findViewById(R.id.txtInsight);
        txtBalance = findViewById(R.id.txtBalance);
        btnLimit = findViewById(R.id.btnLimit);

        // ⭐ ÁNH XẠ NÚT BACK
        btnBack = findViewById(R.id.btnBack);

        // ⭐ SỰ KIỆN BACK
        btnBack.setOnClickListener(v -> {
            finish(); // quay lại BudgetActivity
        });

        repo = new AnalyticsRepository(
                FintrackDatabase
                        .getInstance(this)
                        .getOpenHelper()
                        .getWritableDatabase()
        );

        service = new AnalyticsDomainService(repo);

        loadAnalytics();
        calculateBalance();

        btnLimit.setOnClickListener(v -> {

            Intent intent = new Intent(
                    AnalyticsActivity.this,
                    TrendReportActivity.class
            );

            startActivity(intent);
        });
    }

    private void loadAnalytics(){

        new Thread(() -> {

            List<AnalyticsData> list = service.getAnalytics();

            runOnUiThread(() -> {

                setupPieChart(list);
                setupBarChart(list);

                recyclerView.setLayoutManager(
                        new LinearLayoutManager(this)
                );

                recyclerView.setAdapter(
                        new CategoryAdapter(list)
                );

                txtInsight.setText(
                        service.generateInsight(list)
                );

            });

        }).start();
    }

    private void setupPieChart(List<AnalyticsData> list){

        List<PieEntry> entries = new ArrayList<>();

        double total = 0;

        for(AnalyticsData d : list){
            entries.add(new PieEntry((float)d.getAmount(), d.getCategory()));
            total += d.getAmount();
        }

        PieDataSet set = new PieDataSet(entries,"Expenses");

        set.setColors(
                Color.parseColor("#2ECC71"),
                Color.parseColor("#F39C12"),
                Color.parseColor("#3498DB"),
                Color.parseColor("#9B59B6"),
                Color.parseColor("#E74C3C")
        );

        set.setSliceSpace(3f);
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(12f);

        PieData data = new PieData(set);

        pieChart.setData(data);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(70f);
        pieChart.setTransparentCircleRadius(75f);

        pieChart.setCenterText(
                "TOTAL SPENT\n" +
                        NumberFormat.getInstance().format(total)
        );

        pieChart.setCenterTextSize(16f);
        pieChart.setCenterTextColor(Color.BLACK);

        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(true);

        pieChart.animateY(1200);

        pieChart.invalidate();
    }

    private void setupBarChart(List<AnalyticsData> list){

        List<BarEntry> entries = new ArrayList<>();

        int index = 0;

        for(AnalyticsData d : list){
            entries.add(new BarEntry(index++, (float)d.getAmount()));
        }

        BarDataSet set = new BarDataSet(entries,"Expenses");

        set.setColor(Color.parseColor("#1ABC9C"));

        BarData data = new BarData(set);

        barChart.setData(data);
        barChart.getDescription().setEnabled(false);

        barChart.animateY(1000);

        barChart.invalidate();
    }

    private void calculateBalance(){

        new Thread(() -> {

            FintrackDatabase db =
                    FintrackDatabase.getInstance(getApplicationContext());

            List<TransactionEntity> list =
                    db.transactionDao().getAll();

            double income = 0;
            double expense = 0;

            for(TransactionEntity t : list){

                if("INCOME".equals(t.tx_type_id)){
                    income += t.amount;
                }
                else{
                    expense += t.amount;
                }
            }

            double balance = income - expense;

            runOnUiThread(() ->
                    txtBalance.setText(
                            "Balance: "
                                    + NumberFormat.getInstance().format(balance)
                                    + " VND"
                    )
            );

        }).start();
    }
}
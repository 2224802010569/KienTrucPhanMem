package com.example.fintrack.AnalyticService.view;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fintrack.R;
import com.example.fintrack.TransactionService.data.db.FintrackDatabase;
import com.example.fintrack.TransactionService.data.entity.TransactionEntity;
import com.example.fintrack.UserService.data.UserRepository;
import com.example.fintrack.UserService.data.entity.UserEntity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class TrendReportActivity extends AppCompatActivity {

    BarChart chart;
    TextView txtSaving;
    LinearLayout layoutBreakdown;
    ImageButton btnBack;

    SimpleDateFormat dbFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trend_report);

        chart = findViewById(R.id.chartIncome);
        txtSaving = findViewById(R.id.txtSaving);
        layoutBreakdown = findViewById(R.id.layoutBreakdown);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        loadChart();
        loadBreakdown();
    }

    private void loadChart(){

        new Thread(() -> {

            FintrackDatabase db =
                    FintrackDatabase.getInstance(getApplicationContext());

            UserRepository userRepo = new UserRepository(this);
            UserEntity user = userRepo.getCurrentUser();

            if(user == null) return;

            List<TransactionEntity> list =
                    db.transactionDao().getAllByUser(user.user_id);

            List<BarEntry> incomeEntries = new ArrayList<>();
            List<BarEntry> expenseEntries = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            double totalIncome = 0;
            double totalExpense = 0;

            Calendar cal = Calendar.getInstance();

            for(int i = 5; i >= 0; i--){

                Calendar temp = (Calendar) cal.clone();
                temp.add(Calendar.MONTH,-i);

                int month = temp.get(Calendar.MONTH);
                int year = temp.get(Calendar.YEAR);

                double income = 0;
                double expense = 0;

                for(TransactionEntity t : list){

                    try{

                        Date date = dbFormat.parse(t.tx_date);

                        Calendar txCal = Calendar.getInstance();
                        txCal.setTime(date);

                        if(txCal.get(Calendar.MONTH)==month &&
                                txCal.get(Calendar.YEAR)==year){

                            if("INCOME".equals(t.tx_type_id)){
                                income += t.amount;
                            }
                            else{
                                expense += t.amount;
                            }
                        }

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                totalIncome += income;
                totalExpense += expense;

                int x = 5 - i;

                incomeEntries.add(new BarEntry(x,(float)income));
                expenseEntries.add(new BarEntry(x,(float)expense));

                labels.add(
                        new SimpleDateFormat("MMM",Locale.getDefault())
                                .format(temp.getTime())
                );
            }

            double saving = totalIncome - totalExpense;

            runOnUiThread(() -> {

                txtSaving.setText(
                        "VND " + NumberFormat.getInstance().format(saving)
                );

                BarDataSet incomeSet =
                        new BarDataSet(incomeEntries,"Income");
                incomeSet.setColor(0xFF2ECC71);

                BarDataSet expenseSet =
                        new BarDataSet(expenseEntries,"Expense");
                expenseSet.setColor(0xFFE74C3C);

                BarData data = new BarData(incomeSet,expenseSet);

                float groupSpace = 0.2f;
                float barSpace = 0.02f;
                float barWidth = 0.38f;

                data.setBarWidth(barWidth);

                chart.setData(data);

                chart.getXAxis().setAxisMinimum(0);
                chart.getXAxis().setAxisMaximum(
                        0 + data.getGroupWidth(groupSpace, barSpace) * labels.size()
                );

                chart.groupBars(0f,groupSpace,barSpace);

                XAxis xAxis = chart.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                xAxis.setGranularity(1f);
                xAxis.setCenterAxisLabels(true);
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawGridLines(false);

                chart.getAxisRight().setEnabled(false);
                chart.getDescription().setEnabled(false);

                chart.setFitBars(true);

                chart.animateY(1000);
                chart.invalidate();
            });

        }).start();
    }

    private void loadBreakdown(){

        new Thread(() -> {

            FintrackDatabase db =
                    FintrackDatabase.getInstance(getApplicationContext());

            UserRepository userRepo = new UserRepository(this);
            UserEntity user = userRepo.getCurrentUser();

            if(user == null) return;

            List<TransactionEntity> list =
                    db.transactionDao().getAllByUser(user.user_id);

            Map<String,double[]> map =
                    new LinkedHashMap<>();

            Calendar cal = Calendar.getInstance();

            for(int i = 5; i >= 0; i--){

                Calendar temp = (Calendar) cal.clone();
                temp.add(Calendar.MONTH,-i);

                int month = temp.get(Calendar.MONTH);
                int year = temp.get(Calendar.YEAR);

                double income = 0;
                double expense = 0;

                for(TransactionEntity t : list){

                    try{

                        Date date = dbFormat.parse(t.tx_date);

                        Calendar txCal = Calendar.getInstance();
                        txCal.setTime(date);

                        if(txCal.get(Calendar.MONTH)==month &&
                                txCal.get(Calendar.YEAR)==year){

                            if("INCOME".equals(t.tx_type_id)){
                                income += t.amount;
                            }
                            else{
                                expense += t.amount;
                            }
                        }

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                String monthName =
                        new SimpleDateFormat("MMM yyyy",Locale.getDefault())
                                .format(temp.getTime());

                map.put(monthName,new double[]{income,expense});
            }

            runOnUiThread(() -> {

                layoutBreakdown.removeAllViews();

                for(String m : map.keySet()){

                    double income = map.get(m)[0];
                    double expense = map.get(m)[1];

                    TextView tv = new TextView(this);

                    tv.setText(
                            m +
                                    "    Income: " +
                                    NumberFormat.getInstance().format(income) +
                                    "    Expense: " +
                                    NumberFormat.getInstance().format(expense)
                    );

                    tv.setPadding(12,12,12,12);
                    tv.setTextSize(14);

                    layoutBreakdown.addView(tv);
                }

            });

        }).start();
    }
}
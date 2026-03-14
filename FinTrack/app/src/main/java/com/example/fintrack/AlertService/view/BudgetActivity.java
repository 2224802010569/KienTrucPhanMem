package com.example.fintrack.AlertService.view;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fintrack.R;
import com.example.fintrack.AlertService.data.AlertRepository;
import com.example.fintrack.AlertService.entity.BudgetAlert;
import com.example.fintrack.AlertService.usecase.CreateBudgetUseCase;
import com.example.fintrack.AlertService.usecase.CheckBudgetWarningUseCase;
import com.example.fintrack.AnalyticService.view.AnalyticsActivity;
import com.example.fintrack.TransactionService.data.db.FintrackDatabase;
import com.example.fintrack.TransactionService.data.entity.CategoryEntity;

import java.util.ArrayList;
import java.util.List;
import com.example.fintrack.UserService.data.UserRepository;
import com.example.fintrack.UserService.data.entity.UserEntity;
public class BudgetActivity extends AppCompatActivity {
    private String currentUserId;
    private Spinner spCategory, spPeriod;
    private EditText etAmount;
    private CheckBox cbNotify;

    private Button btnCreate, btnAnalytics;

    private TextView tvTotalSpent;

    private List<CategoryEntity> categoryList = new ArrayList<>();

    RecyclerView rvBudgets;
    BudgetAdapter adapter;

    AlertRepository repo = AlertRepository.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        // ⭐ xin quyền notification Android 13+
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != getPackageManager().PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1
                );
            }
        }
        UserRepository userRepo = new UserRepository(this);
        UserEntity currentUser = userRepo.getCurrentUser();

        if (currentUser == null) return;

        String currentUserId = currentUser.user_id;
        initViews();
        initSpinners();
        setupCreateButton();
        setupAnalyticsButton();
        loadBudgets();
    }

    private void initViews() {

        spCategory = findViewById(R.id.spCategory);
        spPeriod = findViewById(R.id.spPeriod);
        etAmount = findViewById(R.id.etAmount);
        cbNotify = findViewById(R.id.cbNotify);

        btnCreate = findViewById(R.id.btnCreate);
        btnAnalytics = findViewById(R.id.btnAnalytics);

        rvBudgets = findViewById(R.id.rvBudgets);
        rvBudgets.setLayoutManager(new LinearLayoutManager(this));

        tvTotalSpent = findViewById(R.id.tvTotalSpent);
    }

    private void initSpinners() {

        new Thread(() -> {

            FintrackDatabase db =
                    FintrackDatabase.getInstance(getApplicationContext());

            categoryList = db.categoryDao().getAll(currentUserId);

            List<String> names = new ArrayList<>();

            for (CategoryEntity c : categoryList) {
                names.add(c.name);
            }

            runOnUiThread(() -> {

                ArrayAdapter<String> adapter =
                        new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_dropdown_item,
                                names);

                spCategory.setAdapter(adapter);

            });

        }).start();

        String[] periods = {"Daily", "Weekly", "Monthly"};

        ArrayAdapter<String> periodAdapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        periods);

        spPeriod.setAdapter(periodAdapter);
    }

    private void setupCreateButton() {

        btnCreate.setOnClickListener(v -> {

            String amountStr = etAmount.getText().toString().trim();

            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show();
                return;
            }

            double limit = Double.parseDouble(amountStr);

            int pos = spCategory.getSelectedItemPosition();

            if (pos < 0 || pos >= categoryList.size()) return;

            CategoryEntity c = categoryList.get(pos);

            new CreateBudgetUseCase(repo)
                    .execute(
                            c.category_id,
                            c.name,
                            limit,
                            spPeriod.getSelectedItem().toString()
                    );

            loadBudgets();

            Toast.makeText(this, "Budget created!", Toast.LENGTH_SHORT).show();

            if (cbNotify.isChecked()) {

                new CheckBudgetWarningUseCase(repo)
                        .execute(BudgetActivity.this);
            }

        });
    }

    // ⭐ BUTTON CHUYỂN SANG ANALYTICS
    private void setupAnalyticsButton(){

        btnAnalytics.setOnClickListener(v -> {

            Intent intent = new Intent(
                    BudgetActivity.this,
                    AnalyticsActivity.class
            );

            startActivity(intent);
        });
    }

    private void loadBudgets(){

        new Thread(() -> {

            FintrackDatabase db =
                    FintrackDatabase.getInstance(getApplicationContext());

            List<BudgetAlert> list = repo.findAll();

            double total = 0;

            for(BudgetAlert alert : list){

                alert.spent = 0;

                Double spent =
                        db.transactionDao().getTotalExpenseByCategory(
                                currentUserId,
                                alert.categoryId,
                                getCurrentMonth()
                        );

                alert.spent = spent == null ? 0 : spent;

                total += alert.spent;
            }

            double finalTotal = total;

            runOnUiThread(() -> {

                tvTotalSpent.setText(
                        String.format("%,.0f VND", finalTotal)
                );

                if(adapter == null){
                    adapter = new BudgetAdapter(list);
                    rvBudgets.setAdapter(adapter);
                }else{
                    adapter.notifyDataSetChanged();
                }

                if (cbNotify.isChecked()) {

                    new CheckBudgetWarningUseCase(repo)
                            .execute(BudgetActivity.this);
                }

            });

        }).start();
    }

    private String getCurrentMonth(){

        java.time.LocalDate now = java.time.LocalDate.now();

        return now.getYear() + "-" +
                String.format("%02d", now.getMonthValue());
    }
}
package com.example.fintrack.TransactionService.view;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fintrack.R;
import com.example.fintrack.TransactionService.data.db.FintrackDatabase;
import com.example.fintrack.TransactionService.data.entity.CategoryEntity;
import com.example.fintrack.TransactionService.data.entity.TransactionEntity;
import com.example.fintrack.TransactionService.domain.usecase.SearchTransactionUseCase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.content.Intent;
import com.example.fintrack.TransactionService.view.CategoryManagementActivity;
import android.widget.ImageButton;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import java.time.format.DateTimeFormatter;
import com.example.fintrack.UserService.data.UserRepository;
import com.example.fintrack.UserService.data.entity.UserEntity;
public class HistoryActivity extends AppCompatActivity {
    private String currentUserId;
    private TransactionAdapter adapter;
    private Button btnSearch;

    /* ================= CATEGORY OPTION ================= */
    static class CategoryOption {
        String id;
        String name;

        CategoryOption(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name; // 👈 Spinner chỉ hiển thị TÊN
        }
    }
    /* =================================================== */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // ===== VIEW =====
        EditText edtKeyword = findViewById(R.id.edtKeyword);
        btnSearch = findViewById(R.id.btnSearch);
        ImageButton btnBackHistory = findViewById(R.id.btnBackHistory);
        btnBackHistory.setOnClickListener(v -> finish());

        Spinner spinnerDate = findViewById(R.id.spinnerDate);
        Spinner spinnerWallet = findViewById(R.id.spinnerWallet);
        Spinner spinnerCategory = findViewById(R.id.spinnerCategory);

        RecyclerView rv = findViewById(R.id.rvTransactions);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        ImageButton btnFilter = findViewById(R.id.btnCategoryManager);
        UserRepository userRepo = new UserRepository(this);
        UserEntity currentUser = userRepo.getCurrentUser();


        if (currentUser == null) return;

        currentUserId = currentUser.user_id;
        btnFilter.setOnClickListener(v -> {

            Intent intent = new Intent(
                    HistoryActivity.this,
                    CategoryManagementActivity.class
            );

            startActivity(intent);
        });

        // ===== DATE FILTER =====
        spinnerDate.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"ALL", "TODAY", "YESTERDAY", "THIS_MONTH"}
        ));

        // ===== WALLET FILTER =====
        spinnerWallet.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"ALL", "acc001", "acc002"}
        ));

        // ===== LOAD CATEGORY FROM DB (ĐÚNG) =====
        loadCategorySpinner(spinnerCategory);

        // ===== ADAPTER =====
        adapter = new TransactionAdapter(
                new ArrayList<>(),
                tx -> TransactionOptionsBottomSheet
                        .newInstance(tx.tx_id)
                        .show(getSupportFragmentManager(), "tx_options")
        );

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // ===== FAB (+) =====
        fabAdd.setOnClickListener(v ->
                startActivity(
                        new android.content.Intent(
                                HistoryActivity.this,
                                AddTransactionActivity.class
                        )
                )
        );

        // ===== SEARCH + FILTER =====
        btnSearch.setOnClickListener(v -> {

            final String keywordFinal =
                    edtKeyword.getText().toString().trim();

            final String walletRaw =
                    spinnerWallet.getSelectedItem().toString();

            final String dateRaw =
                    spinnerDate.getSelectedItem().toString();

            CategoryOption selectedCategory =
                    (CategoryOption) spinnerCategory.getSelectedItem();

            final String categoryIdFinal =
                    (selectedCategory == null || selectedCategory.id == null)
                            ? null
                            : selectedCategory.id;

            // ===== MAP DATE =====
            final String fromDateFinal;
            final String toDateFinal;

            final LocalDate today = LocalDate.now();

            switch (dateRaw) {
                case "TODAY":
                    fromDateFinal = today.toString();
                    toDateFinal = today.toString();
                    break;

                case "YESTERDAY":
                    fromDateFinal = today.minusDays(1).toString();
                    toDateFinal = fromDateFinal;
                    break;

                case "THIS_MONTH":
                    fromDateFinal = today.withDayOfMonth(1).toString();
                    toDateFinal = today.withDayOfMonth(today.lengthOfMonth()).toString();
                    break;

                default:
                    fromDateFinal = null;
                    toDateFinal = null;
            }

            final String accountFinal =
                    "ALL".equals(walletRaw) ? null : walletRaw;

            new Thread(() -> {
                FintrackDatabase db =
                        FintrackDatabase.getInstance(getApplicationContext());

                SearchTransactionUseCase useCase =
                        new SearchTransactionUseCase(db.transactionDao());

                List<TransactionEntity> result = useCase.execute(
                        currentUserId,
                        null,
                        accountFinal,
                        keywordFinal.isEmpty() ? null : keywordFinal,
                        fromDateFinal,
                        toDateFinal
                );

                // ===== FILTER CATEGORY (CLIENT SIDE) =====
                if (categoryIdFinal != null) {
                    List<TransactionEntity> filtered = new ArrayList<>();
                    for (TransactionEntity tx : result) {
                        if (categoryIdFinal.equals(tx.category_id)) {
                            filtered.add(tx);
                        }
                    }
                    result = filtered;
                }

                // ===== GROUP BY DATE =====
                List<HistoryItem> uiList = new ArrayList<>();
                LocalDate yesterday = today.minusDays(1);
                String currentHeader = "";

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");

                for (TransactionEntity tx : result) {

                    LocalDate d;

                    try {
                        d = LocalDate.parse(tx.tx_date); // yyyy-MM-dd
                    } catch (Exception e) {
                        d = LocalDate.parse(tx.tx_date, formatter); // d/M/yyyy
                    }
                    String header;

                    if (d.equals(today)) header = "Today";
                    else if (d.equals(yesterday)) header = "Yesterday";
                    else header = tx.tx_date;

                    if (!header.equals(currentHeader)) {
                        uiList.add(HistoryItem.header(header));
                        currentHeader = header;
                    }
                    uiList.add(HistoryItem.tx(tx));
                }

                final List<HistoryItem> finalUiList = uiList;
                runOnUiThread(() -> adapter.updateData(finalUiList));

            }).start();
        });
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (btnSearch != null) {
            btnSearch.performClick();
        }
    }

    // ===== LOAD CATEGORY SPINNER (HIỂN THỊ NAME, DÙNG ID) =====
    private void loadCategorySpinner(Spinner spinnerCategory) {
        new Thread(() -> {
            FintrackDatabase db =
                    FintrackDatabase.getInstance(getApplicationContext());

            List<CategoryEntity> categories =
                    db.categoryDao().getAll(currentUserId);

            List<CategoryOption> options = new ArrayList<>();
            options.add(new CategoryOption(null, "ALL"));

            for (CategoryEntity c : categories) {
                options.add(new CategoryOption(c.category_id, c.name));
            }

            runOnUiThread(() -> {
                spinnerCategory.setAdapter(
                        new ArrayAdapter<>(
                                this,
                                android.R.layout.simple_spinner_item,
                                options
                        )
                );

                // load list sau khi category sẵn sàng
                btnSearch.performClick();
            });
        }).start();
    }
}

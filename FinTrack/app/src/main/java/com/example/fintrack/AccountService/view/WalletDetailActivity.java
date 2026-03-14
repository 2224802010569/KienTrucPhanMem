package com.example.fintrack.AccountService.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fintrack.AccountService.data.AccountRepository;
import com.example.fintrack.AccountService.model.AccountEntity;
import com.example.fintrack.AccountService.usecase.DeleteAccountUseCase;
import com.example.fintrack.R;
import com.example.fintrack.TransactionService.data.db.FintrackDatabase;
import com.example.fintrack.TransactionService.data.entity.TransactionEntity;
import com.example.fintrack.TransactionService.view.HistoryItem;
import com.example.fintrack.TransactionService.view.TransactionAdapter;
import com.google.android.material.button.MaterialButton;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.example.fintrack.TransactionService.view.TransferActivity;
import com.example.fintrack.TransactionService.view.HistoryActivity;
import com.example.fintrack.TransactionService.view.AddTransactionActivity;

public class WalletDetailActivity extends AppCompatActivity {
    private TextView txtName, txtId, txtBalance;
    private RecyclerView rvRecentTransactions;
    private TransactionAdapter transactionAdapter;
    private AccountRepository repo;
    private String walletId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_wallet);

        repo = AccountRepository.getInstance(this);

        initViews();
        setupListeners();

        walletId = getIntent().getStringExtra("WALLET_ID");
        if (walletId == null) {
            handleNoWalletId();
        }

        if (walletId != null) {
            displayWalletInfo(walletId);
            setupRecyclerView();
            loadRecentTransactions();
        }
    }

    private void initViews() {
        txtName = findViewById(R.id.txtDetailWalletName);
        txtId = findViewById(R.id.txtDetailWalletId);
        txtBalance = findViewById(R.id.txtDetailBalance);
        rvRecentTransactions = findViewById(R.id.rvRecentTransactions);
    }

    private void setupListeners() {
        findViewById(R.id.btnBackDetail).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddMoney).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            intent.putExtra("TX_TYPE", "INCOME");
            intent.putExtra("ACCOUNT_ID", walletId);
            startActivity(intent);
        });
        findViewById(R.id.btnTransfer).setOnClickListener(v -> {
            Intent intent = new Intent(this, TransferActivity.class);
            intent.putExtra("WALLET_ID", walletId);
            startActivity(intent);
        });
        findViewById(R.id.btnStatements).setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            intent.putExtra("WALLET_ID", walletId);
            startActivity(intent);
        });
        findViewById(R.id.btnMenuMore).setOnClickListener(v -> showArchiveDialog());
        findViewById(R.id.btnEditWallet).setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageWalletActivity.class);
            intent.putExtra("EDIT_MODE", true);
            intent.putExtra("WALLET_ID", walletId);
            startActivity(intent);
            finish();
        });
        findViewById(R.id.btnDeleteWallet).setOnClickListener(v -> confirmDelete());
    }

    private void setupRecyclerView() {
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(new ArrayList<>(), tx -> {
            // Logic khi nhấn vào transaction nếu cần
        });
        rvRecentTransactions.setAdapter(transactionAdapter);
    }

    private void loadRecentTransactions() {
        new Thread(() -> {
            com.example.fintrack.UserService.data.UserRepository userRepo = new com.example.fintrack.UserService.data.UserRepository(this);
            com.example.fintrack.UserService.data.entity.UserEntity currentUser = userRepo.getCurrentUser();
            if (currentUser == null) return;

            FintrackDatabase db = FintrackDatabase.getInstance(this);
            // Lấy 5 giao dịch gần nhất của ví này
            List<TransactionEntity> transactions = db.transactionDao().getByAccount(currentUser.user_id, walletId);
            
            List<HistoryItem> historyItems = new ArrayList<>();
            // Lấy tối đa 5 giao dịch
            int limit = Math.min(transactions.size(), 5);
            for (int i = 0; i < limit; i++) {
                historyItems.add(HistoryItem.tx(transactions.get(i)));
            }

            runOnUiThread(() -> transactionAdapter.updateData(historyItems));
        }).start();
    }

    private void handleNoWalletId() {
        com.example.fintrack.UserService.data.UserRepository userRepo = new com.example.fintrack.UserService.data.UserRepository(this);
        com.example.fintrack.UserService.data.entity.UserEntity currentUser = userRepo.getCurrentUser();
        if (currentUser != null) {
            List<AccountEntity> userAccounts = repo.getAccountsByUser(currentUser.user_id);
            if (!userAccounts.isEmpty()) {
                walletId = userAccounts.get(0).accountId;
            }
        }
        if (walletId == null) {
            Toast.makeText(this, "Wallet not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void confirmDelete() {
        boolean hasTransactions = repo.hasTransactions(walletId);
        new AlertDialog.Builder(this)
                .setTitle(!hasTransactions ? "Delete Confirmation" : "Archive Confirmation")
                .setMessage(!hasTransactions
                        ? "This wallet has no transactions. Do you want to permanently DELETE it?"
                        : "This wallet already has transactions. The system will ARCHIVE it from the main list.")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    new DeleteAccountUseCase(this).execute(walletId);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void displayWalletInfo(String id) {
        AccountEntity account = repo.getAccountById(id);
        if (account != null) {
            txtName.setText(account.name);
            txtId.setText("WALLET ID: " + account.accountId);
            DecimalFormat df = new DecimalFormat("#,###");
            txtBalance.setText(df.format(account.balance) + " VND");
        }
    }

    private void showArchiveDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Archive Wallet Confirmation")
                .setMessage("Do you want to archive this wallet?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    repo.updateStatus(walletId, AccountEntity.STATUS_HIDDEN);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (walletId != null) {
            displayWalletInfo(walletId);
            loadRecentTransactions();
        }
    }
}
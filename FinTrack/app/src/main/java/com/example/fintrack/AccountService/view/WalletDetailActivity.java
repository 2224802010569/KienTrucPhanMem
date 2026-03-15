package com.example.fintrack.AccountService.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fintrack.AccountService.data.AccountRepository;
import com.example.fintrack.AccountService.model.AccountEntity;
import com.example.fintrack.AccountService.usecase.DeleteAccountUseCase;
import com.example.fintrack.R;
import com.google.android.material.button.MaterialButton;
import java.text.DecimalFormat;
import java.util.List;

import com.example.fintrack.TransactionService.view.TransferActivity;
import com.example.fintrack.TransactionService.view.HistoryActivity;
import com.example.fintrack.TransactionService.view.AddTransactionActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import com.example.fintrack.TransactionService.view.TransactionAdapter;
import com.example.fintrack.TransactionService.view.HistoryItem;
import com.example.fintrack.TransactionService.api.TransactionApiImpl;
import com.example.fintrack.TransactionService.data.entity.TransactionEntity;
public class WalletDetailActivity extends AppCompatActivity {
    private TransactionApiImpl transactionApi;
    private TextView txtName, txtId, txtBalance;
    private AccountRepository repo;
    private String walletId;
    RecyclerView rvRecent;
    TransactionAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_wallet);

        // Khởi tạo Repo
        repo = AccountRepository.getInstance(this);

        txtName = findViewById(R.id.txtDetailWalletName);
        txtId = findViewById(R.id.txtDetailWalletId);
        txtBalance = findViewById(R.id.txtDetailBalance);
        ImageButton btnBack = findViewById(R.id.btnBackDetail);
        ImageButton btnMenuMore = findViewById(R.id.btnMenuMore);
        MaterialButton btnEdit = findViewById(R.id.btnEditWallet);
        MaterialButton btnDelete = findViewById(R.id.btnDeleteWallet);
        ImageButton btnTransfer = findViewById(R.id.btnTransfer);
        ImageButton btnStatements = findViewById(R.id.btnStatements);
        ImageButton btnAddMoney = findViewById(R.id.btnAddMoney);

        walletId = getIntent().getStringExtra("WALLET_ID");

        if (walletId == null) {
            com.example.fintrack.UserService.data.UserRepository userRepo = new com.example.fintrack.UserService.data.UserRepository(this);
            com.example.fintrack.UserService.data.entity.UserEntity currentUser = userRepo.getCurrentUser();

            if (currentUser != null) {
                List<AccountEntity> userAccounts = repo.getAccountsByUser(currentUser.user_id);
                AccountEntity first = userAccounts.isEmpty() ? null : userAccounts.get(0);
                if (first != null) {
                    walletId = first.accountId;
                }
            }

            if (walletId == null) {
                Toast.makeText(this, "Wallet not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        displayWalletInfo(walletId);

        btnBack.setOnClickListener(v -> finish());
        btnAddMoney.setOnClickListener(v -> {
            Intent intent = new Intent(WalletDetailActivity.this, AddTransactionActivity.class);
            intent.putExtra("TX_TYPE", "INCOME");
            intent.putExtra("ACCOUNT_ID", walletId);
            startActivity(intent);
        });
        btnTransfer.setOnClickListener(v -> {
            Intent intent = new Intent(WalletDetailActivity.this, TransferActivity.class);
            intent.putExtra("WALLET_ID", walletId);
            startActivity(intent);
        });

        btnStatements.setOnClickListener(v -> {
            Intent intent = new Intent(WalletDetailActivity.this, HistoryActivity.class);
            intent.putExtra("WALLET_ID", walletId);
            startActivity(intent);
        });
        btnMenuMore.setOnClickListener(v -> showArchiveDialog());

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageWalletActivity.class);
            intent.putExtra("EDIT_MODE", true);
            intent.putExtra("WALLET_ID", walletId);
            startActivity(intent);
            finish();
        });

        btnDelete.setOnClickListener(v -> {
            boolean hasTransactions = repo.hasTransactions(walletId);
            new AlertDialog.Builder(this)
                    .setTitle(!hasTransactions ? "Delete Confirmation" : "Archive Confirmation")
                    .setMessage(!hasTransactions
                            ? "This wallet has no transactions. Do you want to permanently DELETE it?"
                            : "This wallet already has transactions. The system will ARCHIVE it from the main list.")
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        // Thêm Context vào đây
                        new DeleteAccountUseCase(WalletDetailActivity.this).execute(walletId);
                        Toast.makeText(this, "Operation completed successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        transactionApi = new TransactionApiImpl(getApplicationContext());
        rvRecent = findViewById(R.id.rvRecentTransactions);
        rvRecent.setLayoutManager(new LinearLayoutManager(this));

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
                    Toast.makeText(this, "Wallet archived successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(walletId != null){
            displayWalletInfo(walletId);
            loadRecentTransactions();
        }
    }
    private void loadRecentTransactions(){

        new Thread(() -> {

            List<TransactionEntity> txList =
                    transactionApi.getRecentByAccount(walletId);

            List<HistoryItem> historyList = new ArrayList<>();

            for(TransactionEntity tx : txList){
                historyList.add(new HistoryItem(tx));
            }

            runOnUiThread(() -> {

                adapter = new TransactionAdapter(
                        historyList,
                        null
                );

                rvRecent.setAdapter(adapter);

            });

        }).start();
    }
}
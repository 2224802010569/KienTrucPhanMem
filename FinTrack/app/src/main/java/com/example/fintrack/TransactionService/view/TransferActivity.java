package com.example.fintrack.TransactionService.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fintrack.R;
import com.example.fintrack.TransactionService.data.db.FintrackDatabase;
import com.example.fintrack.TransactionService.domain.usecase.TransferMoneyUseCase;
import com.example.fintrack.AccountService.api.AccountApiImpl;
import com.example.fintrack.AccountService.model.AccountEntity;
import com.example.fintrack.UserService.data.UserRepository;
import com.example.fintrack.UserService.data.entity.UserEntity;

import java.text.DecimalFormat;
import java.util.List;
import android.widget.ImageButton;

public class TransferActivity extends AppCompatActivity {
    private ImageButton btnBackTransfer;
    private final DecimalFormat df = new DecimalFormat("#,###");
    private String currentUserId;
    private TextView tvSourceAccount, tvSourceBalance;
    private TextView tvTargetAccount, tvTargetBalance;

    private Button btnChangeSource, btnChangeTarget;
    private Button btnTransfer, btn500k, btn1m, btn5m;

    private EditText edtAmount, edtNote;

    private String selectedSourceId;
    private String selectedTargetId;

    private List<AccountEntity> accounts;

    private AccountApiImpl accountApi;
    private UserRepository userRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        accountApi = new AccountApiImpl(getApplicationContext());
        userRepo = new UserRepository(this);

        initViews();
        btnBackTransfer.setOnClickListener(v -> finish());

        UserRepository userRepo = new UserRepository(this);
        UserEntity currentUser = userRepo.getCurrentUser();

        if (currentUser == null) return;

        currentUserId = currentUser.user_id;

        loadAccounts();
        setupPresetButtons();
        setupTransfer();

        if (currentUser != null) {
            currentUserId = currentUser.user_id;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAccounts();
    }

    private void initViews() {
        tvSourceAccount = findViewById(R.id.tvSourceAccount);
        tvSourceBalance = findViewById(R.id.tvSourceBalance);
        tvTargetAccount = findViewById(R.id.tvTargetAccount);
        tvTargetBalance = findViewById(R.id.tvTargetBalance);
        btnChangeSource = findViewById(R.id.btnChangeSource);
        btnChangeTarget = findViewById(R.id.btnChangeTarget);
        btnTransfer = findViewById(R.id.btnTransfer);
        btn500k = findViewById(R.id.btn500k);
        btn1m = findViewById(R.id.btn1m);
        btn5m = findViewById(R.id.btn5m);
        edtAmount = findViewById(R.id.edtAmount);
        edtNote = findViewById(R.id.edtNote);
        btnBackTransfer = findViewById(R.id.btnBackTransfer);
    }

    private void loadAccounts() {
        new Thread(() -> {
            UserEntity currentUser = userRepo.getCurrentUser();
            if (currentUser == null) return;

            List<AccountEntity> result = accountApi.getAccountsByUser(currentUser.user_id);

            runOnUiThread(() -> {
                accounts = result;

                if (accounts == null || accounts.size() < 2) {
                    Toast.makeText(this,
                            "Bạn cần ít nhất 2 ví để thực hiện chuyển khoản",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                AccountEntity source = null;
                AccountEntity target = null;

                for (AccountEntity acc : accounts) {
                    if (acc.accountId.equals(selectedSourceId))
                        source = acc;
                    if (acc.accountId.equals(selectedTargetId))
                        target = acc;
                }

                if (source == null) source = accounts.get(0);
                if (target == null) {
                    target = (accounts.size() > 1) ? accounts.get(1) : accounts.get(0);
                }

                selectedSourceId = source.accountId;
                selectedTargetId = target.accountId;

                tvSourceAccount.setText(source.name);
                tvSourceBalance.setText("Balance: " + df.format(source.balance) + " VND");

                tvTargetAccount.setText(target.name);
                tvTargetBalance.setText("Balance: " + df.format(target.balance) + " VND");

                setupWalletSelectors();
            });
        }).start();
    }

    private void setupWalletSelectors() {
        btnChangeSource.setOnClickListener(v ->
                AccountSelectDialog.show(
                        this,
                        accounts,
                        account -> {
                            selectedSourceId = account.accountId;
                            tvSourceAccount.setText(account.name);
                            tvSourceBalance.setText("Balance: " + df.format(account.balance) + " VND");
                        }
                )
        );

        btnChangeTarget.setOnClickListener(v ->
                AccountSelectDialog.show(
                        this,
                        accounts,
                        account -> {
                            selectedTargetId = account.accountId;
                            tvTargetAccount.setText(account.name);
                            tvTargetBalance.setText("Balance: " + df.format(account.balance) + " VND");
                        }
                )
        );
    }

    private void setupPresetButtons() {
        btn500k.setOnClickListener(v -> addAmount(500_000));
        btn1m.setOnClickListener(v -> addAmount(1_000_000));
        btn5m.setOnClickListener(v -> addAmount(5_000_000));
    }

    private void setupTransfer() {
        btnTransfer.setOnClickListener(v -> {
            if (selectedSourceId == null || selectedTargetId == null) {
                Toast.makeText(this, "Vui lòng chọn ví", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedSourceId.equals(selectedTargetId)) {
                Toast.makeText(this, "Ví gửi và ví nhận không được trùng nhau", Toast.LENGTH_LONG).show();
                return;
            }

            String amountStr = edtAmount.getText().toString().replaceAll("[^0-9]", "");
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            String note = edtNote.getText().toString().trim();

            executeTransfer(amount, note);
        });
    }

    private void executeTransfer(double amount, String note) {
        new Thread(() -> {
            try {
                UserEntity currentUser = userRepo.getCurrentUser();
                if (currentUser == null) return;

                FintrackDatabase db = FintrackDatabase.getInstance(getApplicationContext());
                TransferMoneyUseCase useCase = new TransferMoneyUseCase(db.transactionDao(), accountApi);

                useCase.execute(

                        currentUser.user_id,
                        selectedSourceId,
                        selectedTargetId,
                        amount,
                        note
                );

                runOnUiThread(() -> {
                    Toast.makeText(this, "Chuyển tiền thành công", Toast.LENGTH_SHORT).show();
                    loadAccounts();
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void addAmount(int add) {
        String cur = edtAmount.getText().toString().replaceAll("[^0-9]", "");
        long val = cur.isEmpty() ? 0 : Long.parseLong(cur);
        val += add;
        edtAmount.setText(String.valueOf(val));
    }
}
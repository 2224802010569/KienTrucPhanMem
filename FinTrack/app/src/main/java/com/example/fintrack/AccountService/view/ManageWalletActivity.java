package com.example.fintrack.AccountService.view;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fintrack.AccountService.data.AccountRepository;
import com.example.fintrack.AccountService.model.AccountEntity;
import com.example.fintrack.AccountService.model.AccountTypeEntity;
import com.example.fintrack.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class ManageWalletActivity extends AppCompatActivity {
    private boolean isEditMode = false;
    private String walletIdToEdit;
    private AccountRepository repo;

    private AutoCompleteTextView spinnerType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_wallet);
        repo = AccountRepository.getInstance(this);
        ImageButton btnBack = findViewById(R.id.btnBack);
        TextInputEditText edtName = findViewById(R.id.edtName);
        TextInputEditText edtBalance = findViewById(R.id.edtBalance);
        spinnerType = findViewById(R.id.spinnerType);
        MaterialButton btnSave = findViewById(R.id.btnSave);
        View btnAddType = findViewById(R.id.btnAddType);

        loadAccountTypesToSpinner();

        isEditMode = getIntent().getBooleanExtra("EDIT_MODE", false);
        if (isEditMode) {
            walletIdToEdit = getIntent().getStringExtra("WALLET_ID");
            AccountEntity acc = repo.getAccountById(walletIdToEdit);
            if (acc != null) {
                edtName.setText(acc.name);
                edtBalance.setText(String.valueOf((long) acc.balance));
                spinnerType.setText(acc.typeId, false);
            }
            btnSave.setText("Update Wallet");
        }

        btnBack.setOnClickListener(v -> finish());
        spinnerType.setOnClickListener(v -> spinnerType.showDropDown());
        spinnerType.setOnItemClickListener((parent, view, position, id) ->
                spinnerType.setText((String) parent.getItemAtPosition(position), false));

        if (btnAddType != null) btnAddType.setOnClickListener(v -> showAddTypeDialog());

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String balanceStr = edtBalance.getText().toString().trim();
            String typeId = spinnerType.getText().toString().trim();

            if (name.isEmpty() || balanceStr.isEmpty() || typeId.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                double balance = Double.parseDouble(balanceStr);

                // Lấy thông tin user hiện tại để gán vào ví
                com.example.fintrack.UserService.data.UserRepository userRepo =
                        new com.example.fintrack.UserService.data.UserRepository(this);
                String currentUserId = userRepo.getCurrentUser().user_id;

                if (isEditMode) {
                    repo.updateAccount(new AccountEntity(walletIdToEdit, currentUserId, name, typeId, balance));
                } else {
                    repo.addAccount(new AccountEntity(repo.generateNewId(), currentUserId, name, typeId, balance));
                }
                finish();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid balance", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAccountTypesToSpinner() {
        List<String> displayList = new ArrayList<>();
        for (AccountTypeEntity type : repo.getAccountTypes()) displayList.add(type.typeid);
        spinnerType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList));
    }

    private void showAddTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Wallet Type");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 10);

        final TextInputEditText inputId = new TextInputEditText(this);
        inputId.setHint("Type ID (e.g., MOMO)");
        inputId.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> source.toString().matches("[A-Za-z0-9_]+") ? null : ""});
        final TextInputEditText inputName = new TextInputEditText(this);
        inputName.setHint("Display Name");
        final TextInputEditText inputDesc = new TextInputEditText(this);
        inputDesc.setHint("Description");

        layout.addView(inputId); layout.addView(inputName); layout.addView(inputDesc);
        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String id = inputId.getText().toString().trim().toUpperCase();
            String name = inputName.getText().toString().trim();
            if (id.isEmpty() || name.isEmpty() || repo.isAccountTypeExists(id)) {
                Toast.makeText(this, "Invalid or existing ID", Toast.LENGTH_SHORT).show();
                return;
            }
            repo.addAccountType(id, name, inputDesc.getText().toString().trim());
            loadAccountTypesToSpinner();
            spinnerType.setText(id, false);
        });
        builder.setNegativeButton("Cancel", null).show();
    }
}
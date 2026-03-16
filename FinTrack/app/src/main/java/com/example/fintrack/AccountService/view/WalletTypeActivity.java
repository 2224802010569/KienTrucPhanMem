package com.example.fintrack.AccountService.view;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fintrack.AccountService.data.AccountRepository;
import com.example.fintrack.AccountService.model.AccountEntity;
import com.example.fintrack.R;

public class WalletTypeActivity extends AppCompatActivity {

    private RecyclerView rvTypes;
    private AccountRepository repo;
    private WalletTypeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_type_list);
        repo = AccountRepository.getInstance(this);
        rvTypes = findViewById(R.id.rvWalletTypes);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        setupRecyclerView();
    }

    private void setupRecyclerView() {

        adapter = new WalletTypeAdapter(repo.getAccountTypes(), (typeId, position) -> {

            if (typeId.equals(AccountEntity.TYPE_WALLET) || typeId.equals(AccountEntity.TYPE_BANK)) {
                Toast.makeText(this, "System wallet types cannot be deleted!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (repo.isAccountTypeInUse(typeId)) {
                Toast.makeText(this, "This wallet type is currently in use and cannot be deleted!", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to delete wallet type: " + typeId + "?")
                    .setPositiveButton("Delete", (d, w) -> {

                        repo.deleteAccountType(typeId);

                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, repo.getAccountTypes().size());

                        Toast.makeText(this, "Wallet type deleted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        rvTypes.setLayoutManager(new LinearLayoutManager(this));
        rvTypes.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
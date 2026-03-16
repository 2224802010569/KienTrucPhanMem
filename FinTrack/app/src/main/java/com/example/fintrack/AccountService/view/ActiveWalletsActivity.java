package com.example.fintrack.AccountService.view;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fintrack.AccountService.data.AccountRepository;
import com.example.fintrack.AccountService.model.AccountEntity;
import com.example.fintrack.R;
import java.util.ArrayList;
import java.util.List;

public class ActiveWalletsActivity extends AppCompatActivity {
    private AccountAdapter adapter;
    private List<AccountEntity> activeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_wallets);

        findViewById(R.id.btnBackActive).setOnClickListener(v -> finish());

        RecyclerView rvActive = findViewById(R.id.rvActiveWallets);
        adapter = new AccountAdapter(activeList);
        rvActive.setLayoutManager(new LinearLayoutManager(this));
        rvActive.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        activeList.clear();

        com.example.fintrack.UserService.data.UserRepository userRepo = new com.example.fintrack.UserService.data.UserRepository(this);
        com.example.fintrack.UserService.data.entity.UserEntity currentUser = userRepo.getCurrentUser();

        if (currentUser != null) {
            AccountRepository repo = AccountRepository.getInstance(this);
            for (AccountEntity acc : repo.getAccountsByUser(currentUser.user_id)) {
                if (acc.status == null || acc.status.isEmpty() || acc.status.equalsIgnoreCase(AccountEntity.STATUS_ACTIVE)) {
                    activeList.add(acc);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
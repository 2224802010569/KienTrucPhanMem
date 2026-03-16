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

public class HiddenWalletsActivity extends AppCompatActivity {
    private AccountAdapter adapter;
    private List<AccountEntity> hiddenList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hidden_wallets);

        findViewById(R.id.btnBackHidden).setOnClickListener(v -> finish());

        RecyclerView rvHidden = findViewById(R.id.rvHiddenWallets);
        adapter = new AccountAdapter(hiddenList);
        rvHidden.setLayoutManager(new LinearLayoutManager(this));
        rvHidden.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hiddenList.clear();

        com.example.fintrack.UserService.data.UserRepository userRepo = new com.example.fintrack.UserService.data.UserRepository(this);
        com.example.fintrack.UserService.data.entity.UserEntity currentUser = userRepo.getCurrentUser();

        if (currentUser != null) {
            AccountRepository repo = AccountRepository.getInstance(this);
            for (AccountEntity acc : repo.getAccountsByUser(currentUser.user_id)) {
                if (AccountEntity.STATUS_HIDDEN.equals(acc.status)) {
                    hiddenList.add(acc);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
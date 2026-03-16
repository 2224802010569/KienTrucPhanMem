package com.example.fintrack.UserService.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fintrack.R;
import com.example.fintrack.UserService.data.UserRepository;
import com.example.fintrack.UserService.data.entity.UserEntity;
import com.example.fintrack.AlertService.view.BudgetActivity;
import com.example.fintrack.TransactionService.view.AddTransactionActivity;
import com.example.fintrack.TransactionService.view.CategoryManagementActivity;

public class UserAccountProfileActivity extends AppCompatActivity {

    ImageView btnBack, btnSetting, imgAvatar;
    TextView txtName, txtEmail, txtStatus;
    Button btnLogout;

    LinearLayout itemPersonalInfo, itemSecurity, itemLanguage;
    LinearLayout itemNotification, itemDefaultCurrency, itemHelp;

    // ⭐ 2 BUTTON MỚI
    LinearLayout itemAddTransaction;
    LinearLayout itemCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account_profile);

        // ===== INIT VIEW =====
        btnBack = findViewById(R.id.btnBack);
        btnSetting = findViewById(R.id.btnSetting);

        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtStatus = findViewById(R.id.txtStatus);

        btnLogout = findViewById(R.id.btnLogout);

        imgAvatar = findViewById(R.id.imgAvatar);

        itemPersonalInfo = findViewById(R.id.itemPersonalInfo);
        itemSecurity = findViewById(R.id.itemSecurity);
        itemLanguage = findViewById(R.id.itemLanguage);
        itemNotification = findViewById(R.id.itemNotification);
        itemDefaultCurrency = findViewById(R.id.itemDefaultCurrency);
        itemHelp = findViewById(R.id.itemHelp);

        // ⭐ INIT 2 NÚT
        itemAddTransaction = findViewById(R.id.itemAddTransaction);
        itemCategory = findViewById(R.id.itemCategory);

        // ===== LOAD USER =====
        loadUser();

        // ===== EVENTS =====
        btnBack.setOnClickListener(v -> finish());

        btnSetting.setOnClickListener(v ->
                Toast.makeText(this, "Settings (coming soon)", Toast.LENGTH_SHORT).show()
        );

        itemPersonalInfo.setOnClickListener(v -> {

            Intent intent = new Intent(
                    UserAccountProfileActivity.this,
                    PersonalInfoActivity.class
            );

            startActivity(intent);

        });

        itemSecurity.setOnClickListener(v ->
                Toast.makeText(this, "Security Settings", Toast.LENGTH_SHORT).show()
        );

        itemLanguage.setOnClickListener(v ->
                Toast.makeText(this, "Language feature removed", Toast.LENGTH_SHORT).show()
        );

        // ⭐ MỞ BUDGET
        itemNotification.setOnClickListener(v -> {

            Intent intent = new Intent(
                    UserAccountProfileActivity.this,
                    BudgetActivity.class
            );

            startActivity(intent);

        });

        itemDefaultCurrency.setOnClickListener(v ->
                Toast.makeText(this, "Default Currency: VND", Toast.LENGTH_SHORT).show()
        );

        itemHelp.setOnClickListener(v ->
                Toast.makeText(this, "Help Center", Toast.LENGTH_SHORT).show()
        );

        // ⭐ MỞ ADD TRANSACTION
        itemAddTransaction.setOnClickListener(v -> {

            Intent intent = new Intent(
                    UserAccountProfileActivity.this,
                    AddTransactionActivity.class
            );

            startActivity(intent);

        });

        // ⭐ MỞ CATEGORY MANAGEMENT
        itemCategory.setOnClickListener(v -> {

            Intent intent = new Intent(
                    UserAccountProfileActivity.this,
                    CategoryManagementActivity.class
            );

            startActivity(intent);

        });

        btnLogout.setOnClickListener(v -> logout());
    }

    // ===== LOAD CURRENT USER =====
    private void loadUser() {

        UserRepository repo = new UserRepository(this);

        UserEntity user = repo.getCurrentUser();

        if (user == null) {

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        txtName.setText(user.full_name);
        txtEmail.setText(user.email);
        txtStatus.setText(user.status);

        // ===== LOAD AVATAR =====
        if (user.avatar != null && !user.avatar.isEmpty()) {

            try {
                Uri uri = Uri.parse(user.avatar);
                imgAvatar.setImageURI(uri);
            } catch (Exception e) {
                imgAvatar.setImageResource(R.drawable.avatar);
            }

        } else {

            imgAvatar.setImageResource(R.drawable.avatar);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUser();
    }

    // ===== LOGOUT =====
    private void logout() {

        getSharedPreferences("USER_SESSION", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
package com.example.fintrack.AccountService.port;

import android.content.Context;
import android.content.Intent;

import com.example.fintrack.AccountService.data.AccountRepository;
import com.example.fintrack.AccountService.model.AccountEntity;
import com.example.fintrack.AccountService.view.AccountDashboardActivity;
import com.example.fintrack.UserService.data.UserRepository;
import com.example.fintrack.UserService.data.entity.UserEntity;

public class AccountNavigator {

    public static void initDefaultAccount(Context context, String email) {
        // Lấy User vừa đăng ký dựa vào email
        UserRepository userRepo = new UserRepository(context);
        UserEntity user = userRepo.getUserByEmail(email);

        if (user == null) return;

        AccountRepository accountRepo = AccountRepository.getInstance(context);

        // Nếu user này chưa có ví nào, tạo ngay 1 ví mặc định
        if (accountRepo.getAccountsByUser(user.user_id).isEmpty()) {
            AccountEntity defaultWallet = new AccountEntity(
                    accountRepo.generateNewId(),
                    user.user_id, // Gắn đúng ID của user mới
                    "Ví Tiền Mặt",
                    AccountEntity.TYPE_WALLET,
                    0.0
            );
            accountRepo.addAccount(defaultWallet);
        }
    }

    public static void openDashboard(Context context) {
        Intent intent = new Intent(context, AccountDashboardActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
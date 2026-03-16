package com.example.fintrack.UserService.api;

import android.content.Context;
import android.content.Intent;

import com.example.fintrack.UserService.data.UserRepository;
import com.example.fintrack.UserService.data.dao.UserDao;
import com.example.fintrack.TransactionService.data.db.FintrackDatabase;
import com.example.fintrack.UserService.data.entity.UserEntity;
import com.example.fintrack.UserService.port.UserPort;
import com.example.fintrack.UserService.view.LoginActivity;

public class UserApiImpl implements UserPort {

    private final UserDao userDao;
    private final Context context; // Giữ lại context để dùng cho các hàm dưới

    public UserApiImpl(Context context) {
        this.context = context;
        FintrackDatabase db = FintrackDatabase.getInstance(context);
        userDao = db.userDao();
    }

    @Override
    public UserEntity getUserById(String userId) {
        return userDao.getUserById(userId);
    }

    @Override
    public UserEntity getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }

    @Override
    public UserEntity getUserByUsername(String username) {
        return userDao.getUserByUsername(username);
    }

    // ============= CÁC HÀM CHO NOTIFICATION GỌI =============

    @Override
    public boolean registerFromOtp(String email, String username, String password) {
        UserRepository repo = new UserRepository(context);
        return repo.register(email, username, password);
    }

    @Override
    public boolean changePasswordFromReset(String email, String newPassword) {
        UserRepository repo = new UserRepository(context);
        if (repo.getUserByEmail(email) != null) {
            repo.updatePasswordByEmail(email, newPassword);
            return true;
        }
        return false;
    }

    @Override
    public void openLoginScreen(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
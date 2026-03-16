package com.example.fintrack.UserService.port;

import android.content.Context;
import com.example.fintrack.UserService.data.entity.UserEntity;

public interface UserPort {

    UserEntity getUserById(String userId);

    UserEntity getUserByEmail(String email);

    UserEntity getUserByUsername(String username);
    boolean registerFromOtp(String email, String username, String password);
    boolean changePasswordFromReset(String email, String newPassword);
    void openLoginScreen(Context context);
}
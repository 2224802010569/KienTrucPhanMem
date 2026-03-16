package com.example.fintrack.NotificationService.api;

import android.content.Context;

import com.example.fintrack.UserService.api.UserApiImpl;
import com.example.fintrack.UserService.port.UserPort;

public class ExternalUserApi {

    public boolean registerUser(Context context, String email, String user, String pass) {
        // Khởi tạo UserApiImpl để gọi hàm
        UserPort userApi = new UserApiImpl(context);
        return userApi.registerFromOtp(email, user, pass);
    }

    public boolean updatePassword(Context context, String email, String newPass) {
        UserPort userApi = new UserApiImpl(context);
        return userApi.changePasswordFromReset(email, newPass);
    }

    public void navigateToLogin(Context context) {
        UserPort userApi = new UserApiImpl(context);
        userApi.openLoginScreen(context);
    }
}
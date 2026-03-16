package com.example.fintrack.NotificationService.port;

import android.content.Context;

public interface NotificationPort {
    void sendOtp(String email);
    void requestResetPassword(String email);
    void notifyBalance(String email, String content);
    void openOtpView(Context context, String email, String username, String password);
}
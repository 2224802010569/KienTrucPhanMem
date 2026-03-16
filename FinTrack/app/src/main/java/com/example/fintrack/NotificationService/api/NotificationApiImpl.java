package com.example.fintrack.NotificationService.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.fintrack.NotificationService.data.NotificationRepository;
import com.example.fintrack.NotificationService.port.NotificationPort;
import com.example.fintrack.NotificationService.view.OtpActivity;

public class NotificationApiImpl implements NotificationPort {

    private final NotificationRepository repository;

    public NotificationApiImpl(Context context) {
        this.repository = new NotificationRepository(context);
    }

    @Override
    public void sendOtp(String email) {
        repository.sendOtp(email);
    }

    @Override
    public void requestResetPassword(String email) {
        repository.requestResetPassword(email);
    }

    @Override
    public void notifyBalance(String email, String content) {
        repository.notifyBalance(email, content);
    }

    @Override
    public void openOtpView(Context context, String email, String username, String password) {
        Intent intent = new Intent(context, OtpActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("username", username);
        intent.putExtra("password", password);

        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }
}
package com.example.fintrack.NotificationService.api;

import android.content.Context;

import com.example.fintrack.AccountService.port.AccountNavigator;

public class ExternalAccountApi {

    public void initAccount(Context context, String email) {
        AccountNavigator.initDefaultAccount(context, email);
    }

    public void navigateToDashboard(Context context) {
        AccountNavigator.openDashboard(context);
    }
}
package com.example.fintrack.UserService.api;

import android.content.Context;
import com.example.fintrack.AccountService.port.AccountNavigator;

public class ExternalAccountApi {

    // Gọi AccountNavigator để mở Dashboard
    public void navigateToDashboard(Context context) {
        AccountNavigator.openDashboard(context);
    }
}
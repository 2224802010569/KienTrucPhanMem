package com.example.fintrack.AlertService.usecase;

import android.content.Context;

import com.example.fintrack.AlertService.data.AlertRepository;
import com.example.fintrack.AlertService.entity.BudgetAlert;
import com.example.fintrack.NotificationService.data.NotificationRepository;
import com.example.fintrack.NotificationService.data.entity.AppNotification;

import java.util.List;

public class CheckBudgetWarningUseCase {

    private final AlertRepository repo;
    private final NotificationRepository notificationRepo;

    public CheckBudgetWarningUseCase(Context context, AlertRepository repo) {
        this.repo = repo;
        this.notificationRepo = new NotificationRepository(context);
    }

    public void execute() {

        List<BudgetAlert> list = repo.findAll();

        for (BudgetAlert a : list) {

            if (a.limitAmount <= 0) continue;

            double percent = a.spent / a.limitAmount;

            // reset trigger nếu user chi xuống lại dưới 80%
            if (percent < a.threshold) {
                a.triggered = false;
            }

            // ⚠ đạt 80%
            if (percent >= a.threshold && !a.triggered) {

                notificationRepo.pushAndSave(
                        "Budget Warning",
                        "⚠ " + a.categoryName + " reached " + (int)(a.threshold * 100) + "% of budget",
                        AppNotification.TYPE_ALERT
                );

                a.triggered = true;
            }

            // 🚨 vượt ngân sách
            if (a.spent > a.limitAmount) {
                
                notificationRepo.pushAndSave(
                        "Budget Exceeded",
                        "🚨 " + a.categoryName + " exceeded budget!",
                        AppNotification.TYPE_ALERT
                );
            }
        }
    }
}
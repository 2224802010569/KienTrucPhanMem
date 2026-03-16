package com.example.fintrack.AlertService.usecase;

import android.content.Context;

import com.example.fintrack.AlertService.data.AlertRepository;
import com.example.fintrack.AlertService.entity.BudgetAlert;
import com.example.fintrack.AlertService.service.AlertDomainService;
import com.example.fintrack.NotificationService.data.NotificationRepository;
import com.example.fintrack.NotificationService.data.entity.AppNotification;

import java.text.DecimalFormat;
import java.util.List;

import com.example.fintrack.TransactionService.port.TransactionPort;
import com.example.fintrack.TransactionService.api.TransactionApiImpl;

public class UpdateSpentUseCase {

    private final AlertRepository repo;
    private final TransactionPort transactionPort;
    private final NotificationRepository notificationRepo;
    private final DecimalFormat df = new DecimalFormat("#,###");

    public UpdateSpentUseCase(Context context, AlertRepository repo) {
        this.repo = repo;
        this.transactionPort = new TransactionApiImpl(context);
        this.notificationRepo = new NotificationRepository(context);
    }

    public void execute(Context context, String userId, String categoryId, String month) {

        List<BudgetAlert> list = repo.findAll();
        AlertDomainService domain = new AlertDomainService();

        for (BudgetAlert alert : list) {

            if (!alert.categoryId.equals(categoryId))
                continue;

            Double spent = transactionPort.getTotalExpenseByCategory(
                    userId,
                    categoryId,
                    month
            );

            alert.spent = spent == null ? 0 : spent;

            // reset trigger
            double percent = alert.spent / alert.limitAmount;
            if (percent < alert.threshold) {
                alert.triggered = false;
            }

            if (domain.isWarning(alert) && !alert.triggered) {
                String msg = String.format("⚠ Budget 80%% reached for %s. Spent: %s / %s đ", 
                        alert.categoryName, df.format(alert.spent), df.format(alert.limitAmount));

                notificationRepo.pushAndSave(
                        "Budget Warning",
                        msg,
                        AppNotification.TYPE_ALERT
                );

                alert.triggered = true;
            }

            if (domain.isExceeded(alert)) {
                double overAmount = alert.spent - alert.limitAmount;
                String msg = String.format("🚨 Budget exceeded for %s! Over by %s đ (Total spent: %s đ)", 
                        alert.categoryName, df.format(overAmount), df.format(alert.spent));

                notificationRepo.pushAndSave(
                        "Budget Exceeded",
                        msg,
                        AppNotification.TYPE_ALERT
                );
            }
        }
    }
}
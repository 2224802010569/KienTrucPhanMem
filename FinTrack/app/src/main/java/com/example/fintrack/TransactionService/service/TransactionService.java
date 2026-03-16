package com.example.fintrack.TransactionService.service;

import android.content.Context;

import com.example.fintrack.AccountService.data.AccountRepository;
import com.example.fintrack.AccountService.model.AccountEntity;
import com.example.fintrack.TransactionService.data.db.FintrackDatabase;
import com.example.fintrack.TransactionService.data.entity.TransactionEntity;

public class TransactionService {

    private final FintrackDatabase db;
    private final AccountRepository accountRepo;

    public TransactionService(Context context) {
        db = FintrackDatabase.getInstance(context);
        accountRepo = AccountRepository.getInstance(context);
    }

    public void save(TransactionEntity transaction) {
        new Thread(() -> {
            db.transactionDao().insert(transaction);

            updateAccountBalance(transaction);
        }).start();
    }

    private void updateAccountBalance(TransactionEntity tx) {
        String type = tx.tx_type_id;

        if ("INCOME".equals(type)) {
            // Cộng tiền vào tài khoản đích
            adjustBalance(tx.target_account_id, tx.amount);
        } else if ("EXPENSE".equals(type)) {
            // Trừ tiền từ tài khoản nguồn
            adjustBalance(tx.source_account_id, -tx.amount);
        } else if ("TRANSFER".equals(type)) {
            // Trừ nguồn, cộng đích
            adjustBalance(tx.source_account_id, -tx.amount);
            adjustBalance(tx.target_account_id, tx.amount);
        }
    }

    private void adjustBalance(String accountId, double amount) {
        if (accountId == null) return;
        AccountEntity account = accountRepo.getAccountById(accountId);
        if (account != null) {
            account.balance += amount;
            accountRepo.updateAccount(account);
        }
    }
}
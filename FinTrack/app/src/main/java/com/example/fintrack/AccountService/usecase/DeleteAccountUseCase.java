package com.example.fintrack.AccountService.usecase;

import android.content.Context;
import com.example.fintrack.AccountService.data.AccountRepository;
import com.example.fintrack.AccountService.model.AccountEntity;

public class DeleteAccountUseCase {
    private final AccountRepository repo;

    // Bắt buộc truyền Context vào đây
    public DeleteAccountUseCase(Context context) {
        this.repo = AccountRepository.getInstance(context);
    }

    public void execute(String accountId) {
        boolean hasTransactions = repo.hasTransactions(accountId);
        if (!hasTransactions) {
            repo.deleteAccount(accountId);
        } else {
            repo.updateStatus(accountId, AccountEntity.STATUS_HIDDEN);
        }
    }
}
package com.example.fintrack.AccountService.api;

import android.content.Context;

import com.example.fintrack.AccountService.port.AccountPort;
import com.example.fintrack.AccountService.usecase.DeleteAccountUseCase;
import com.example.fintrack.AccountService.usecase.UpdateBalanceUseCase;
import com.example.fintrack.AccountService.usecase.GetAccountBalanceUseCase;
import com.example.fintrack.TransactionService.data.db.FintrackDatabase;
import com.example.fintrack.AccountService.model.AccountEntity;

import java.util.List;

public class AccountApiImpl implements AccountPort {

    private final DeleteAccountUseCase deleteUC; // Đã sửa: Khai báo ở đây
    private final UpdateBalanceUseCase updateBalanceUC;
    private final GetAccountBalanceUseCase getBalanceUC;

    private final Context context;

    public AccountApiImpl(Context context) {

        this.context = context.getApplicationContext();

        // Đã sửa: Khởi tạo và truyền Context vào đây
        deleteUC = new DeleteAccountUseCase(this.context);
        updateBalanceUC = new UpdateBalanceUseCase(this.context);
        getBalanceUC = new GetAccountBalanceUseCase(this.context);
    }

    @Override
    public void deleteOrHideAccount(String id) {
        deleteUC.execute(id);
    }

    @Override
    public void updateBalance(String accountId, double amount) {
        updateBalanceUC.execute(accountId, amount);
    }

    @Override
    public double getBalance(String accountId) {
        return getBalanceUC.execute(accountId);
    }

    @Override
    public List<AccountEntity> getAccountsByUser(String userId) {

        FintrackDatabase db =
                FintrackDatabase.getInstance(context);

        return db.accountDao().getAccountsByUser(userId);
    }
}
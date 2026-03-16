package com.example.fintrack.AccountService.data;

import android.content.Context;
import com.example.fintrack.AccountService.model.AccountEntity;
import com.example.fintrack.AccountService.model.AccountTypeEntity;
import com.example.fintrack.TransactionService.data.db.FintrackDatabase;
import java.util.List;
import java.util.UUID;

public class AccountRepository {
    private static AccountRepository instance;
    private final AccountDao accountDao;

    private AccountRepository(Context context) {
        accountDao = FintrackDatabase.getInstance(context).accountDao();
        initDefaultTypes();
    }

    // Yêu cầu truyền Context để gọi được Database
    public static AccountRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AccountRepository(context.getApplicationContext());
        }
        return instance;
    }

    private void initDefaultTypes() {
        if (accountDao.getAllTypes().isEmpty()) {
            accountDao.insertType(new AccountTypeEntity(AccountEntity.TYPE_WALLET, "Ví tiền", "Tiền mặt"));
            accountDao.insertType(new AccountTypeEntity(AccountEntity.TYPE_BANK, "Ngân hàng", "Tài khoản ngân hàng"));
        }
    }

    public List<AccountTypeEntity> getAccountTypes() { return accountDao.getAllTypes(); }

    // LẤY VÍ ĐÚNG CỦA USER ĐANG ĐĂNG NHẬP
    public List<AccountEntity> getAccountsByUser(String userId) {
        return accountDao.getAccountsByUser(userId);
    }

    public AccountEntity getAccountById(String id) { return accountDao.getById(id); }

    public void addAccount(AccountEntity account) { accountDao.insertAccount(account); }

    public void updateAccount(AccountEntity account) { accountDao.updateAccount(account); }

    public void updateStatus(String id, String status) {
        AccountEntity acc = getAccountById(id);
        if (acc != null) {
            acc.status = status;
            updateAccount(acc);
        }
    }

    public void deleteAccount(String id) { accountDao.deleteAccount(id); }

    public boolean hasTransactions(String id) { return false; } // Sẽ kết nối với Transaction sau

    public void addAccountType(String id, String name, String description) {
        accountDao.insertType(new AccountTypeEntity(id, name, description));
    }

    public boolean isAccountTypeExists(String id) {
        for (AccountTypeEntity t : getAccountTypes()) {
            if (t.typeid.equals(id)) return true;
        }
        return false;
    }

    public boolean isAccountTypeInUse(String typeId) { return false; }

    public void deleteAccountType(String typeId) { accountDao.deleteAccountType(typeId); }

    public String generateNewId() {
        return "acc_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
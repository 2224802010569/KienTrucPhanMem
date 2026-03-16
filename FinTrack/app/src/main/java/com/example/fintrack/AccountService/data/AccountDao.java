package com.example.fintrack.AccountService.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.fintrack.AccountService.model.AccountEntity;
import com.example.fintrack.AccountService.model.AccountTypeEntity;

import java.util.List;

@Dao
public interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAccount(AccountEntity account);

    @Update
    void updateAccount(AccountEntity account);

    @Query("SELECT * FROM accounts WHERE user_id = :userId")
    List<AccountEntity> getAccountsByUser(String userId);

    @Query("SELECT * FROM accounts WHERE account_id = :accountId LIMIT 1")
    AccountEntity getById(String accountId);

    @Query("UPDATE accounts SET balance = balance + :amount WHERE account_id = :accountId")
    void updateBalance(String accountId, double amount);

    @Query("SELECT * FROM account_types")
    List<AccountTypeEntity> getAllTypes();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertType(AccountTypeEntity type);
    @Query("DELETE FROM accounts WHERE account_id = :accountId")
    void deleteAccount(String accountId);

    @Query("DELETE FROM account_types WHERE type_id = :typeId")
    void deleteAccountType(String typeId);
}
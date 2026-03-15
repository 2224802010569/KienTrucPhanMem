package com.example.fintrack.TransactionService.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.fintrack.TransactionService.data.entity.TransactionEntity;

import java.util.List;

@Dao
public interface TransactionDao {

    // ===== INSERT =====
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TransactionEntity transaction);

    // ===== UPDATE =====
    @Update
    void update(TransactionEntity transaction);
    // ===== BULK INSERT =====
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TransactionEntity> transactions);
    // ===== DELETE =====
    @Delete
    void delete(TransactionEntity transaction);

    // ===== GET BY ID =====
    @Query("SELECT * FROM transactions WHERE tx_id = :txId LIMIT 1")
    TransactionEntity getById(String txId);

    // ===== GET ALL =====
    @Query("SELECT * FROM transactions ORDER BY created_at DESC")
    List<TransactionEntity> getAll();

    // ===== GET LATEST TRANSACTION (thêm từ code bạn)
    @Query("SELECT * FROM transactions ORDER BY created_at DESC LIMIT 1")
    TransactionEntity getLatestTransaction();

    // ===== CHECK DUPLICATE (thêm từ code bạn)
    @Query("SELECT * FROM transactions WHERE tx_date = :date AND note = :note LIMIT 1")
    TransactionEntity checkDuplicate(String date, String note);

    // ===== SEARCH / FILTER =====
    @Query(
            "SELECT * FROM transactions " +
                    "WHERE user_id = :userId " +
                    "AND (:txTypeId IS NULL OR tx_type_id = :txTypeId) " +
                    "AND ( " +
                    "   :accountId IS NULL OR " +
                    "   source_account_id = :accountId OR " +
                    "   target_account_id = :accountId " +
                    ") " +
                    "AND ( " +
                    "   :keyword IS NULL OR " +
                    "   note LIKE '%' || :keyword || '%' " +
                    ") " +
                    "AND ( " +
                    "   (:fromDate IS NULL OR tx_date >= :fromDate) " +
                    "   AND (:toDate IS NULL OR tx_date <= :toDate) " +
                    ") " +
                    "ORDER BY tx_date DESC, created_at DESC"
    )
    List<TransactionEntity> searchTransactions(
            String userId,
            String txTypeId,
            String accountId,
            String keyword,
            String fromDate,
            String toDate
    );
    // ===== GET ALL BY USER =====
    @Query(
            "SELECT * FROM transactions " +
                    "WHERE user_id = :userId " +
                    "ORDER BY tx_date DESC, created_at DESC"
    )
    List<TransactionEntity> getAllByUser(String userId);


    // ===== GET BY ACCOUNT =====
    @Query(
            "SELECT * FROM transactions " +
                    "WHERE user_id = :userId " +
                    "AND (source_account_id = :accountId OR target_account_id = :accountId) " +
                    "ORDER BY tx_date DESC, created_at DESC"
    )
    List<TransactionEntity> getByAccount(String userId, String accountId);


    // ===== GET BY MONTH =====
    @Query(
            "SELECT * FROM transactions " +
                    "WHERE user_id = :userId " +
                    "AND month = :month " +
                    "ORDER BY tx_date DESC"
    )
    List<TransactionEntity> getByMonth(String userId, String month);


    // ===== TOTAL EXPENSE =====
    @Query(
            "SELECT SUM(amount) FROM transactions " +
                    "WHERE user_id = :userId " +
                    "AND tx_type_id = 'EXPENSE' " +
                    "AND month = :month"
    )
    Double getTotalExpense(String userId, String month);


    // ===== TOTAL INCOME =====
    @Query(
            "SELECT SUM(amount) FROM transactions " +
                    "WHERE user_id = :userId " +
                    "AND tx_type_id = 'INCOME' " +
                    "AND month = :month"
    )
    Double getTotalIncome(String userId, String month);
    @Query(
            "SELECT SUM(amount) FROM transactions " +
                    "WHERE user_id = :userId " +
                    "AND category_id = :categoryId " +
                    "AND tx_type_id = 'EXPENSE' " +
                    "AND month = :month"
    )
    Double getTotalExpenseByCategory(String userId, String categoryId, String month);
    @Query(
            "SELECT * FROM transactions " +
                    "WHERE (source_account_id = :accountId OR target_account_id = :accountId) " +
                    "ORDER BY tx_date DESC, created_at DESC " +
                    "LIMIT 5"
    )
    List<TransactionEntity> getRecentByAccount(String accountId);
}
package com.example.fintrack.TransactionService.port;

import com.example.fintrack.TransactionService.data.entity.TransactionEntity;
import java.util.List;

public interface TransactionPort {

    TransactionEntity getTransactionById(String txId);


    List<TransactionEntity> searchTransactions(
            String userId,
            String txTypeId,
            String accountId,
            String keyword,
            String fromDate,
            String toDate
    );

    TransactionEntity getLatestTransaction();

    // ===== thêm cho các service khác =====

    List<TransactionEntity> getAllByUser(String userId);

    List<TransactionEntity> getByAccount(String userId, String accountId);

    List<TransactionEntity> getByMonth(String userId, String month);

    Double getTotalExpense(String userId, String month);

    Double getTotalIncome(String userId, String month);

    Double getTotalExpenseByCategory(String userId, String categoryId, String month);
    void insertTransaction(TransactionEntity tx);

    void deleteTransaction(String txId);
}
package com.example.fintrack.TransactionService.api;

import android.content.Context;

import com.example.fintrack.TransactionService.data.dao.TransactionDao;
import com.example.fintrack.TransactionService.data.db.FintrackDatabase;
import com.example.fintrack.TransactionService.data.entity.TransactionEntity;
import com.example.fintrack.TransactionService.port.TransactionPort;

import java.util.List;

public class TransactionApiImpl implements TransactionPort {

    private final TransactionDao transactionDao;

    public TransactionApiImpl(Context context) {

        FintrackDatabase db =
                FintrackDatabase.getInstance(context);

        transactionDao = db.transactionDao();
    }
    @Override
    public TransactionEntity getTransactionById(String txId) {
        return transactionDao.getById(txId);
    }
    @Override
    public List<TransactionEntity> searchTransactions(
            String userId,
            String txTypeId,
            String accountId,
            String keyword,
            String fromDate,
            String toDate
    ) {

        return transactionDao.searchTransactions(
                userId,
                txTypeId,
                accountId,
                keyword,
                fromDate,
                toDate
        );
    }

    @Override
    public TransactionEntity getLatestTransaction() {
        return transactionDao.getLatestTransaction();
    }
    @Override
    public List<TransactionEntity> getAllByUser(String userId) {
        return transactionDao.getAllByUser(userId);
    }

    @Override
    public List<TransactionEntity> getByAccount(String userId, String accountId) {
        return transactionDao.getByAccount(userId, accountId);
    }

    @Override
    public List<TransactionEntity> getByMonth(String userId, String month) {
        return transactionDao.getByMonth(userId, month);
    }

    @Override
    public Double getTotalExpense(String userId, String month) {
        Double value = transactionDao.getTotalExpense(userId, month);
        return value == null ? 0 : value;
    }

    @Override
    public Double getTotalIncome(String userId, String month) {
        Double value = transactionDao.getTotalIncome(userId, month);
        return value == null ? 0 : value;
    }

    @Override
    public Double getTotalExpenseByCategory(String userId, String categoryId, String month) {
        Double value = transactionDao.getTotalExpenseByCategory(userId, categoryId, month);
        return value == null ? 0 : value;
    }
    @Override
    public void insertTransaction(TransactionEntity tx) {
        transactionDao.insert(tx);
    }

    @Override
    public void deleteTransaction(String txId) {
        TransactionEntity tx = transactionDao.getById(txId);
        if(tx != null){
            transactionDao.delete(tx);
        }
    }
}
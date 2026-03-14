package com.example.fintrack.TransactionService.data.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.fintrack.TransactionService.data.dao.AlertDao;
import com.example.fintrack.TransactionService.data.dao.CategoryDao;
import com.example.fintrack.TransactionService.data.dao.TransactionDao;
import com.example.fintrack.TransactionService.data.entity.*;

import com.example.fintrack.AccountService.data.AccountDao;
import com.example.fintrack.AccountService.model.AccountEntity;
import com.example.fintrack.AccountService.model.AccountTypeEntity;

import com.example.fintrack.UserService.data.dao.UserDao;
import com.example.fintrack.UserService.data.entity.UserEntity;

@Database(
        entities = {
                TransactionEntity.class,
                AccountEntity.class,
                CategoryEntity.class,
                AlertEntity.class,
                TxTypeEntity.class,
                UserEntity.class,
                AccountTypeEntity.class
        },
        version = 12,
        exportSchema = false
)
public abstract class FintrackDatabase extends RoomDatabase {

    private static volatile FintrackDatabase INSTANCE;

    public abstract TransactionDao transactionDao();
    public abstract AccountDao accountDao();
    public abstract CategoryDao categoryDao();
    public abstract AlertDao alertDao();
    public abstract UserDao userDao();

    public static FintrackDatabase getInstance(Context context) {

        if (INSTANCE == null) {
            synchronized (FintrackDatabase.class) {

                if (INSTANCE == null) {

                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    FintrackDatabase.class,
                                    "fintrack_db_clean"
                            )
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .addCallback(new Callback() {

                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);

                                    // ACCOUNT TYPES
                                    db.execSQL(
                                            "INSERT INTO account_types (type_id,name,description) VALUES " +
                                                    "('WALLET','Ví tiền','Tiền mặt')," +
                                                    "('BANK','Ngân hàng','Tài khoản ngân hàng')"
                                    );

                                    // TRANSACTION TYPES
                                    db.execSQL(
                                            "INSERT INTO tx_types (tx_type_id,name,sign) VALUES " +
                                                    "('INCOME','Thu nhập',1)," +
                                                    "('EXPENSE','Chi tiêu',-1)," +
                                                    "('TRANSFER','Chuyển khoản',0)"
                                    );

                                    // KHÔNG seed user data
                                    // users
                                    // accounts
                                    // transactions
                                    // categories
                                }
                            })
                            .build();

                }

            }
        }

        return INSTANCE;
    }
}
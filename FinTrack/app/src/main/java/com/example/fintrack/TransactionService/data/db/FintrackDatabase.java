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

import com.example.fintrack.AlertService.entity.Reminder;

@Database(
        entities = {
                TransactionEntity.class,
                AccountEntity.class,
                CategoryEntity.class,
                AlertEntity.class,
                TxTypeEntity.class,
                UserEntity.class,
                AccountTypeEntity.class,
                Reminder.class   // ⭐ THÊM DÒNG NÀY
        },
        version = 13, // ⭐ tăng version
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

                                    // ===== DEMO DATA =====

                                    db.execSQL(
                                            "INSERT INTO accounts (account_id,user_id,name,type_id,balance,status,created_at) VALUES " +
                                                    "('acc001','u001','Ví chính','WALLET',10000000,'ACTIVE','2026')"
                                    );

                                    db.execSQL(
                                            "INSERT INTO accounts (account_id,user_id,name,type_id,balance,status,created_at) VALUES " +
                                                    "('acc002','u001','Ngân hàng','WALLET',50000000,'ACTIVE','2026')"
                                    );

                                }

                            })

                            .build();
                }
            }
        }

        return INSTANCE;
    }
}
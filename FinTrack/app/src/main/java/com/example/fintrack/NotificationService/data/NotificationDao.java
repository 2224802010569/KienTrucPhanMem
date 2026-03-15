package com.example.fintrack.NotificationService.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.fintrack.NotificationService.data.entity.AppNotification;

import java.util.List;

@Dao
public interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AppNotification notification);

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    List<AppNotification> getAll();

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    void markAsRead(String id);

    @Query("DELETE FROM notifications")
    void deleteAll();
}
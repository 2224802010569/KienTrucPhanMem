package com.example.fintrack.NotificationService.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications")
public class AppNotification {

    public static final String TYPE_TRANSACTION = "TRANSACTION";
    public static final String TYPE_SYSTEM = "SYSTEM";
    public static final String TYPE_ALERT = "ALERT";

    @PrimaryKey
    @NonNull
    public String id;

    public String title;
    public String message;
    public long timestamp;
    public boolean isRead;
    public String type;

    public AppNotification(@NonNull String id, String title, String message, long timestamp, boolean isRead, String type) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = isRead;
        this.type = type;
    }
}
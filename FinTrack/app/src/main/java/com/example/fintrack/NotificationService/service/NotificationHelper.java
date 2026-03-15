package com.example.fintrack.NotificationService.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.fintrack.R;

public class NotificationHelper {

    private static final String CHANNEL_ID = "alert_channel";

    public static void send(Context context, String message) {

        createChannel(context);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("📅 FinTrack Reminder")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        NotificationManagerCompat.from(context)
                .notify((int) System.currentTimeMillis(), builder.build());
    }

    private static void createChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "FinTrack Alerts",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
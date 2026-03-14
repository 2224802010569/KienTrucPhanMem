package com.example.fintrack.NotificationService.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fintrack.NotificationService.data.NotificationRepository;
import com.example.fintrack.NotificationService.data.entity.AppNotification;
import com.example.fintrack.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class NotificationHistoryActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private NotificationRepository repository;
    private List<AppNotification> allNotifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_history);

        repository = new NotificationRepository(this);
        initViews();
        loadNotifications();
    }

    private void initViews() {
        rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(new ArrayList<>());
        rvNotifications.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        findViewById(R.id.btnMarkAllRead).setOnClickListener(v -> {
            new Thread(() -> {
                for (AppNotification n : allNotifications) {
                    repository.markAsRead(n.id);
                }
                runOnUiThread(this::loadNotifications);
            }).start();
        });

        FloatingActionButton btnDeleteAll = findViewById(R.id.btnDeleteAll);
        btnDeleteAll.setOnClickListener(v -> {
            allNotifications.clear();
            adapter.updateData(new ArrayList<>());
            // repository.deleteAll();
        });
    }

    private void loadNotifications() {
        allNotifications = repository.getAllNotifications();
        adapter.updateData(allNotifications);
    }
}
package com.example.fintrack.AlertService.view;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fintrack.R;
import com.example.fintrack.AlertService.data.ReminderRepository;
import com.example.fintrack.AlertService.entity.Reminder;
import com.example.fintrack.AlertService.usecase.CheckReminderUseCase;
import com.example.fintrack.TransactionService.data.db.FintrackDatabase;

import java.util.List;

public class ReminderActivity extends AppCompatActivity {

    Button btnAddReminder;
    RecyclerView recyclerView;
    ImageButton btnBack;

    ReminderRepository repo;
    ReminderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        btnAddReminder = findViewById(R.id.btnAddReminder);
        recyclerView = findViewById(R.id.recyclerReminder);
        btnBack = findViewById(R.id.btnBack);

        // Android 13 permission
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    1
            );
        }

        repo = new ReminderRepository(
                FintrackDatabase
                        .getInstance(this)
                        .getOpenHelper()
                        .getWritableDatabase()
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadReminders();

        btnBack.setOnClickListener(v -> finish());

        btnAddReminder.setOnClickListener(v -> {

            Intent intent = new Intent(
                    ReminderActivity.this,
                    AddReminderActivity.class);

            startActivity(intent);
        });

        checkReminders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReminders();
    }

    private void loadReminders() {

        List<Reminder> list = repo.findAll();

        adapter = new ReminderAdapter(list);

        adapter.setListener(new ReminderAdapter.ReminderListener() {

            @Override
            public void onEdit(Reminder r) {

                Intent intent = new Intent(
                        ReminderActivity.this,
                        AddReminderActivity.class);

                intent.putExtra("id", r.id);
                startActivity(intent);
            }

            @Override
            public void onDelete(Reminder r) {

                new AlertDialog.Builder(ReminderActivity.this)
                        .setTitle("Delete Reminder")
                        .setMessage("Are you sure you want to delete this bill?")
                        .setPositiveButton("Delete", (dialog, which) -> {

                            repo.delete(r.id);
                            loadReminders();

                            Toast.makeText(
                                    ReminderActivity.this,
                                    "Reminder deleted",
                                    Toast.LENGTH_SHORT
                            ).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void checkReminders() {

        CheckReminderUseCase check =
                new CheckReminderUseCase(repo);

        check.execute(this);
    }
}
package com.example.fintrack.UserService.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fintrack.R;
import com.example.fintrack.UserService.data.UserRepository;
import com.example.fintrack.UserService.api.ExternalAccountApi;

public class SplashActivity extends AppCompatActivity {

    ProgressBar progressBar;
    int progress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressBar = findViewById(R.id.progressBar);

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                progress += 5;
                progressBar.setProgress(progress);

                if (progress < 100) {
                    handler.postDelayed(this, 80);
                } else {

                    checkAutoLogin();

                }
            }
        }, 80);
    }

    private void checkAutoLogin() {

        UserRepository repo = new UserRepository(this);

        if (repo.isLoggedIn()) {
            ExternalAccountApi accountApi = new ExternalAccountApi();
            accountApi.navigateToDashboard(this);
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish();
    }
}
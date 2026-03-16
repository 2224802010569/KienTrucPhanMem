package com.example.fintrack.NotificationService.view;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fintrack.NotificationService.api.ExternalUserApi;
import com.example.fintrack.NotificationService.util.TokenManager;
import com.example.fintrack.R;

public class ResetActivity extends AppCompatActivity {

    private EditText edtNewPassword;
    private Button btnConfirmChange;

    private String receivedToken, targetEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);

        edtNewPassword = findViewById(R.id.edtNewPassword);
        btnConfirmChange = findViewById(R.id.btnConfirmChange);

        Uri data = getIntent().getData();

        if (data != null) {
            receivedToken = data.getQueryParameter("token");
            targetEmail = data.getQueryParameter("email");
        }

        btnConfirmChange.setOnClickListener(v -> {

            String pass = edtNewPassword.getText().toString().trim();

            if (targetEmail != null &&
                    TokenManager.getInstance().verifyToken(receivedToken, targetEmail)) {

                ExternalUserApi userApi = new ExternalUserApi();

                // Truyền thêm 'this' vào do ta đã đổi tham số ở API
                if (userApi.updatePassword(this, targetEmail, pass)) {

                    Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show();

                    // Chuyển về màn hình Login
                    userApi.navigateToLogin(this);
                    finish();
                }

            } else {
                Toast.makeText(this, "Invalid or expired reset link", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
package com.example.fintrack.UserService.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fintrack.UserService.data.UserRepository;
import com.example.fintrack.UserService.data.entity.UserEntity;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fintrack.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText edtEmail;
    Button btnSend;
    ImageView btnBack;
    TextView txtLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        edtEmail = findViewById(R.id.edtEmail);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
        txtLogin = findViewById(R.id.txtLogin);

        btnBack.setOnClickListener(v -> finish());
        txtLogin.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                return;
            }

            UserRepository repo = new UserRepository(this);
            UserEntity user = repo.getUserByEmail(email);

            if (user == null) {
                Toast.makeText(this, "Email không tồn tại trong hệ thống", Toast.LENGTH_SHORT).show();
                return;
            }

            com.example.fintrack.NotificationService.api.NotificationApiImpl notifApi =
                    new com.example.fintrack.NotificationService.api.NotificationApiImpl(this);

            notifApi.requestResetPassword(email);
            android.content.SharedPreferences prefs = getSharedPreferences("USER_SESSION", MODE_PRIVATE);
            prefs.edit().putInt("attempt_" + user.full_name, 0).apply();
            Toast.makeText(this, "Hệ thống đã gửi link reset mật khẩu vào email của bạn.", Toast.LENGTH_LONG).show();
            finish();
        });

    }
}

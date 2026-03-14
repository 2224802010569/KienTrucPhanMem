package com.example.fintrack.UserService.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fintrack.R;
import com.example.fintrack.UserService.data.UserRepository;

public class RegisterActivity extends AppCompatActivity {

    EditText edtUsername, edtEmail, edtPassword;
    Button btnRegister;
    TextView txtLogin;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword); // thêm

        btnRegister = findViewById(R.id.btnRegister);
        txtLogin = findViewById(R.id.txtLogin);
        btnBack = findViewById(R.id.btnBack);

        btnRegister.setOnClickListener(v -> register());

        btnBack.setOnClickListener(v -> finish());
        txtLogin.setOnClickListener(v -> finish());
    }

    private void register() {

        String username = edtUsername.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Validate rỗng
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,
                    "Vui lòng nhập đầy đủ thông tin",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Username >= 8 ký tự
        if (username.length() < 8) {
            Toast.makeText(this,
                    "Username phải tối thiểu 8 ký tự",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email
        if (!isValidGmail(email)) {
            Toast.makeText(this,
                    "Email phải đúng định dạng @gmail.com",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Password >= 8 ký tự và có ít nhất 1 chữ hoa
        if (password.length() < 8 || !password.matches(".*[A-Z].*")) {
            Toast.makeText(this,
                    "Password phải tối thiểu 8 ký tự và có ít nhất 1 chữ viết hoa",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        UserRepository repo = new UserRepository(this);

        // Kiểm tra xem email hoặc username đã tồn tại chưa TRƯỚC KHI gửi OTP
        if (repo.getUserByEmail(email) != null || repo.getUserByUsername(username) != null) {
            Toast.makeText(this, "Email hoặc Username đã tồn tại", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Đang gửi OTP đến Email...", Toast.LENGTH_SHORT).show();

        com.example.fintrack.NotificationService.api.NotificationApiImpl notifApi =
                new com.example.fintrack.NotificationService.api.NotificationApiImpl(this);

        notifApi.sendOtp(email);
        notifApi.openOtpView(this, email, username, password);
    }

    private boolean isValidGmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@gmail\\.com$");
    }
}
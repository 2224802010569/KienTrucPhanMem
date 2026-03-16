package com.example.fintrack.UserService.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputType;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fintrack.R;
import com.example.fintrack.UserService.data.UserRepository;
import com.example.fintrack.UserService.api.ExternalAccountApi;

public class LoginActivity extends AppCompatActivity {

    EditText edtUsername, edtPassword;
    Button btnLogin;
    TextView txtRegister, txtForgot;
    ImageView btnBack;

    UserRepository userRepository;
    SharedPreferences prefs;

    boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("USER_SESSION", MODE_PRIVATE);

        // ===== AUTO LOGIN =====
        if (prefs.getBoolean("isLoggedIn", false)) {
            // DÙNG EXTERNAL API ĐỂ CHUYỂN TRANG
            ExternalAccountApi accountApi = new ExternalAccountApi();
            accountApi.navigateToDashboard(this);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        userRepository = new UserRepository(this);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtRegister = findViewById(R.id.txtRegister);
        txtForgot = findViewById(R.id.txtForgot);
        btnBack = findViewById(R.id.btnBack);
        // Show / Hide password
        edtPassword.setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_UP) {

                if (edtPassword.getCompoundDrawables()[2] != null) {

                    int drawableWidth =
                            edtPassword.getCompoundDrawables()[2]
                                    .getBounds().width();

                    if (event.getRawX() >=
                            (edtPassword.getRight() - drawableWidth)) {

                        if (isPasswordVisible) {

                            edtPassword.setInputType(
                                    InputType.TYPE_CLASS_TEXT |
                                            InputType.TYPE_TEXT_VARIATION_PASSWORD
                            );

                            isPasswordVisible = false;

                        } else {

                            edtPassword.setInputType(
                                    InputType.TYPE_CLASS_TEXT |
                                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                            );

                            isPasswordVisible = true;
                        }

                        edtPassword.setSelection(
                                edtPassword.getText().length()
                        );

                        return true;
                    }
                }
            }

            return false;
        });
        btnLogin.setOnClickListener(v -> login());

        btnBack.setOnClickListener(v -> finish());

        txtRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        txtForgot.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class))
        );
    }

    private void login() {

        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // ===== CHECK EMPTY =====
        if (username.isEmpty() || password.isEmpty()) {

            Toast.makeText(this,
                    "Vui lòng nhập đầy đủ thông tin",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        // ===== GET ATTEMPT =====
        int attempts = prefs.getInt("attempt_" + username, 0);

        if (attempts >= 3) {

            Toast.makeText(this,
                    "Tài khoản đã bị khóa do nhập sai 3 lần. Vui lòng đổi mật khẩu !!!",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        boolean success = userRepository.login(username, password);

        if (success) {
            // reset attempts
            prefs.edit().putInt("attempt_" + username, 0).apply();

            // save login session
            prefs.edit()
                    .putBoolean("isLoggedIn", true)
                    .putString("username", username)
                    .apply();

            Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

            // DÙNG EXTERNAL API ĐỂ CHUYỂN TRANG
            ExternalAccountApi accountApi = new ExternalAccountApi();
            accountApi.navigateToDashboard(this);
            finish();

        } else {

            attempts++;

            prefs.edit()
                    .putInt("attempt_" + username, attempts)
                    .apply();

            Toast.makeText(this,
                    "Sai username hoặc password (" + attempts + "/3)",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
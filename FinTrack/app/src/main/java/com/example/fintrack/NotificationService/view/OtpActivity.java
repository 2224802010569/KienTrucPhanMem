package com.example.fintrack.NotificationService.view;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fintrack.NotificationService.api.ExternalAccountApi;
import com.example.fintrack.NotificationService.api.ExternalUserApi;
import com.example.fintrack.NotificationService.data.NotificationRepository;
import com.example.fintrack.R;

import java.util.Locale;

public class OtpActivity extends AppCompatActivity {

    private EditText edtHiddenOtp;
    private final TextView[] tvOtps = new TextView[8];
    private TextView txtTimer;
    private Button btnVerify;

    private String email, username, password;
    private int wrongCount = 0;
    private static final int MAX_ATTEMPTS = 5;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        email = getIntent().getStringExtra("email");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");

        initViews();
        startTimer(300000);
        setupOtpLogic();

        btnVerify.setOnClickListener(v -> handleVerification());
    }

    private void initViews() {
        btnVerify = findViewById(R.id.btnVerify);
        edtHiddenOtp = findViewById(R.id.edtHiddenOtp);
        txtTimer = findViewById(R.id.txtTimer);
        tvOtps[0] = findViewById(R.id.tvOtp1);
        tvOtps[1] = findViewById(R.id.tvOtp2);
        tvOtps[2] = findViewById(R.id.tvOtp3);
        tvOtps[3] = findViewById(R.id.tvOtp4);
        tvOtps[4] = findViewById(R.id.tvOtp5);
        tvOtps[5] = findViewById(R.id.tvOtp6);
        tvOtps[6] = findViewById(R.id.tvOtp7);
        tvOtps[7] = findViewById(R.id.tvOtp8);
    }

    private void setupOtpLogic() {
        findViewById(R.id.layoutOtpBoxes).setOnClickListener(v -> {
            edtHiddenOtp.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(edtHiddenOtp, 0);
        });

        edtHiddenOtp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                for (int i = 0; i < 8; i++) {
                    if (i < s.length()) tvOtps[i].setText(String.valueOf(s.charAt(i)));
                    else tvOtps[i].setText("");
                }
                if (s.length() == 8 && wrongCount < MAX_ATTEMPTS) {
                    handleVerification();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void startTimer(long duration) {
        countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long ms) {
                txtTimer.setText(String.format(Locale.getDefault(), "Code expires in: %02d:%02d", (ms / 1000) / 60, (ms / 1000) % 60));
            }

            @Override
            public void onFinish() {
                btnVerify.setEnabled(false);
            }
        }.start();
    }

    private void handleVerification() {
        String inputOtp = edtHiddenOtp.getText().toString().trim();

        if (wrongCount >= MAX_ATTEMPTS || inputOtp.length() < 8) return;

        NotificationRepository repo = new NotificationRepository(this);
        boolean isOtpValid = repo.verifyOtp(inputOtp, email, username, password);

        if (isOtpValid) {
            if (countDownTimer != null) countDownTimer.cancel();

            ExternalUserApi userApi = new ExternalUserApi();
            ExternalAccountApi accountApi = new ExternalAccountApi();

            if (userApi.registerUser(this, email, username, password)) {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                accountApi.initAccount(this, email);
                accountApi.navigateToDashboard(this);
                finish();
            } else {
                Toast.makeText(this, "Email already exists!", Toast.LENGTH_SHORT).show();
            }
        } else {
            wrongCount++;
            edtHiddenOtp.setText("");
            Toast.makeText(this, "Invalid OTP code!", Toast.LENGTH_SHORT).show();
        }
    }
}
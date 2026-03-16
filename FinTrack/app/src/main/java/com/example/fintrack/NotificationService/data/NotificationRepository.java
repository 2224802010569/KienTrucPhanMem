package com.example.fintrack.NotificationService.data;

import android.content.Context;
import android.util.Log;

import com.example.fintrack.NotificationService.data.entity.AppNotification;
import com.example.fintrack.NotificationService.service.NotificationHelper;
import com.example.fintrack.NotificationService.util.EmailTemplate;
import com.example.fintrack.NotificationService.util.GmailSender;
import com.example.fintrack.NotificationService.util.OtpManager;
import com.example.fintrack.NotificationService.util.TokenManager;
import com.example.fintrack.TransactionService.data.db.FintrackDatabase;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationRepository {

    private final NotificationDao dao;
    private final Context context;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public NotificationRepository(Context context) {
        this.context = context.getApplicationContext();
        this.dao = FintrackDatabase.getInstance(this.context).notificationDao();
    }

    private void sendEmail(String to, String subject, String body) {
        executor.execute(() -> {
            try {
                GmailSender.sendHtml(to, subject, body);
                Log.d("NotificationRepo", "Email sent to: " + to);
            } catch (Exception e) {
                Log.e("NotificationRepo", "Email sending failed: " + e.getMessage());
            }
        });
    }

    public void pushAndSave(String title, String message, String type) {
        // 1. Gửi Push Notification (System Tray)
        NotificationHelper.send(context, message);

        // 2. Lưu vào Database để hiển thị trong App (Inbox)
        executor.execute(() -> {
            AppNotification note = new AppNotification(
                    UUID.randomUUID().toString(),
                    title,
                    message,
                    System.currentTimeMillis(),
                    false,
                    type
            );
            dao.insert(note);
        });
    }

    public List<AppNotification> getAllNotifications() {
        return dao.getAll();
    }

    public void markAsRead(String id) {
        executor.execute(() -> dao.markAsRead(id));
    }

    // ================= OTP & ĐĂNG KÝ =================
    public void sendOtp(String email) {
        String otp = OtpManager.getInstance().generateOtp();
        String html = EmailTemplate.buildOtpEmail(otp);
        sendEmail(email, "FinTrack Account Verification OTP", html);
    }

    public boolean verifyOtp(String inputOtp, String email, String username, String password) {
        boolean valid = OtpManager.getInstance().verifyOtp(inputOtp);
        if (valid) {
            String html = EmailTemplate.buildAccountInfo(username, password);
            sendEmail(email, "FinTrack Registration Successful", html);
        }
        return valid;
    }

    // ================= QUÊN MẬT KHẨU =================
    public void requestResetPassword(String email) {
        String token = TokenManager.getInstance().generateResetToken(email);
        String resetLink = "https://fintrackapp/resetpassword?token=" + token + "&email=" + email;
        String htmlContent = EmailTemplate.buildResetLink(resetLink);
        sendEmail(email, "FinTrack Password Reset Request", htmlContent);
    }

    // ================= THÔNG BÁO SỐ DƯ =================
    public void notifyBalance(String email, String content) {
        // Gửi qua Email
        String html = EmailTemplate.buildBalanceNotice(content);
        sendEmail(email, "Balance Change Notification", html);

        // Đồng thời hiện Push và lưu Inbox
        pushAndSave("Balance Change", content, AppNotification.TYPE_TRANSACTION);
    }
}
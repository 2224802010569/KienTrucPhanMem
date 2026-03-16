package com.example.fintrack.NotificationService.util;

public class EmailTemplate {

    public static String buildOtpEmail(String otp) {
        return "<h2>FINTRACK APP</h2>"
                + "<p>Your verification OTP is:</p>"
                + "<h1 style='color:blue'>" + otp + "</h1>"
                + "<p>This code is valid for 5 minutes.</p>";
    }

    public static String buildAccountInfo(String user, String pass) {
        return "<h2>Registration Successful!</h2>"
                + "<p>Username: <b>" + user + "</b></p>"
                + "<p>Password: <b>" + pass + "</b></p>";
    }

    public static String buildResetLink(String link) {
        return "<div style='padding: 20px; border: 1px solid #ddd; text-align: center;'>"
                + "<h2>Password Reset Request</h2>"
                + "<p>Please click the button below to reset your password:</p>"
                + "<a href=\"" + link + "\" style='background-color: #4CAF50; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;'>"
                + "RESET PASSWORD</a>"
                + "</div>";
    }

    public static String buildBalanceNotice(String content) {
        return "<h2>Account Balance Notification</h2>"
                + "<p>" + content + "</p>";
    }
}
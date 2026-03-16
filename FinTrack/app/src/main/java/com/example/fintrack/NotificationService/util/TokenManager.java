package com.example.fintrack.NotificationService.util;

import java.util.UUID;

public class TokenManager {
    private static TokenManager instance;
    private String currentResetToken;
    private String targetEmail;
    private long expireTime;

    private TokenManager() {}

    public static TokenManager getInstance() {
        if (instance == null) instance = new TokenManager();
        return instance;
    }

    public String generateResetToken(String email) {
        this.targetEmail = email;
        this.currentResetToken = UUID.randomUUID().toString();
        this.expireTime = System.currentTimeMillis() + 15 * 60 * 1000;
        return this.currentResetToken;
    }

    public boolean verifyToken(String token, String email) {
        if (currentResetToken == null || !currentResetToken.equals(token)) return false;
        if (!targetEmail.equals(email)) return false;
        if (System.currentTimeMillis() > expireTime) return false;
        return true;
    }
}
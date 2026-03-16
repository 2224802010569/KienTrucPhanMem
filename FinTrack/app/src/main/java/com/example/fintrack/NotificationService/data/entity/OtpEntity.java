package com.example.fintrack.NotificationService.data.entity;

public class OtpEntity {
    private String code;
    private long expireTime;
    private int attempt;

    public OtpEntity(String code, long expireTime) {
        this.code = code;
        this.expireTime = expireTime;
        this.attempt = 0;
    }

    public String getCode() { return code; }
    public long getExpireTime() { return expireTime; }
    public int getAttempt() { return attempt; }
    public void increaseAttempt() { attempt++; }
}
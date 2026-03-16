package com.example.fintrack.NotificationService.util;

import com.example.fintrack.NotificationService.data.entity.OtpEntity;

import java.util.Random;

public class OtpManager {
    private static OtpManager instance;
    private OtpEntity otpEntity;

    private OtpManager(){}

    public static OtpManager getInstance() {
        if (instance == null) instance = new OtpManager();
        return instance;
    }

    public String generateOtp() {
        String code = String.valueOf(10000000 + new Random().nextInt(90000000));
        long expire = System.currentTimeMillis() + 5*60*1000;
        otpEntity = new OtpEntity(code, expire);
        return code;
    }

    public boolean verifyOtp(String input) {
        if (otpEntity == null) return false;
        if (otpEntity.getAttempt() >= 5) return false;

        otpEntity.increaseAttempt();
        if (System.currentTimeMillis() > otpEntity.getExpireTime()) return false;

        return otpEntity.getCode().equals(input);
    }
}
package com.example.fintrack.AlertService.service;

import java.util.Calendar;

public class ReminderDomainService {

    public boolean isDueToday(int dueDay, String period){

        Calendar cal = Calendar.getInstance();

        int today = cal.get(Calendar.DAY_OF_MONTH);

        if(period == null) return false;

        period = period.toUpperCase(); // ⭐ FIX

        switch (period){

            case "MONTH":
                return today == dueDay;

            case "YEAR":
                return today == dueDay;

            default:
                return false;
        }
    }
}
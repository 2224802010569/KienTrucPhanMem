package com.example.fintrack.TransactionService.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReceiptParser {

    // Lấy tổng số tiền
    public static String extractTotal(String text) {
    // Loại bỏ dấu phẩy trong tiền
        text = text.replace(",", "");
// tạo regex( biểu thức chính quy để tìm số tiền, có thể có hoặc không $, có thể có khonngr trắng, 1 or nhiều chữ số, dấu chậm + 2 số phía sau
        Pattern pattern = Pattern.compile("\\$?\\s*\\d+\\.\\d{2}");

        // Duyệt hết hóa đơn, lấy giá trị cuối cùng vì tiền thường nằm ở vị trí cuối cùng nên lấy giá trị cuối cùng
        Matcher matcher = pattern.matcher(text);

        String lastAmount = "";
        while (matcher.find()) {
            lastAmount = matcher.group().trim();
        }

        return lastAmount.replace("$", ""); // bỏ $
    }

    public static String extractDate(String text) {
// regex để tìm ngày theo format
        Pattern pattern = Pattern.compile("\\d{1,2}[-/]\\d{1,2}[-/]\\d{4}");
        Matcher matcher = pattern.matcher(text);
// lấy ngày đầu tiên tìm được
        if (matcher.find()) {

            String rawDate = matcher.group().trim();

            try {

                String[] parts = rawDate.split("[-/]");

                String day = parts[0];
                String month = parts[1];
                String year = parts[2];

                return day + "-" + month + "-" + year; // dd-MM-yyyy

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "";
    }

    public static String extractStoreName(String text) {

        String[] lines = text.split("\\r?\\n");

        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                return line.trim();
            }
        }

        return "";
    }
}
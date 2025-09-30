package com.example.bettinggame.bet;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Quản lý logic đặt cược: parse, validate, format, settle payout.
 * Giữ class này không phụ thuộc UI để dễ test.
 */
public class BetManager {

    public static final int MIN_BET = 100;

    private final NumberFormat intFormatter = NumberFormat.getIntegerInstance(Locale.getDefault());

    private int balance;

    public BetManager(int initialBalance) {
        this.balance = Math.max(0, initialBalance);
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int newBalance) {
        this.balance = Math.max(0, newBalance);
    }

    // Cho phép input có ký tự phân tách, chỉ giữ chữ số
    public int parseAmount(String raw) {
        try {
            if (raw == null) return 0;
            String digitsOnly = raw.replaceAll("[^0-9]", "");
            if (digitsOnly.isEmpty()) return 0;
            long value = Long.parseLong(digitsOnly);
            if (value > Integer.MAX_VALUE) value = Integer.MAX_VALUE;
            return (int) value;
        } catch (Exception e) {
            return 0;
        }
    }

    public ValidationResult validateBet(int amount) {
        if (amount < MIN_BET) {
            return ValidationResult.error("Tối thiểu " + MIN_BET);
        }
        if (amount > balance) {
            return ValidationResult.error("Vượt số dư hiện có");
        }
        return ValidationResult.ok();
    }

    // Trừ tiền ngay khi bắt đầu đặt
    public void applyBetStart(int amount) {
        balance = Math.max(0, balance - amount);
    }

    // Thanh toán khi kết thúc: win -> + 2x, lose -> 0
    public int settle(boolean win, int amount) {
        if (win) {
            balance = safeAdd(balance, amount * 2);
            return amount; // lợi nhuận ròng
        }
        return 0;
    }

    public String formatInt(int n) {
        return intFormatter.format(n);
    }

    private int safeAdd(int a, int b) {
        long sum = (long) a + (long) b;
        if (sum > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (sum < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) sum;
    }

    // Kết quả validate đơn giản
    public static class ValidationResult {
        public final boolean valid;
        public final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult ok() { return new ValidationResult(true, null); }
        public static ValidationResult error(String msg) { return new ValidationResult(false, msg); }
    }
}



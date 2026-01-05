package com.gateway.util;

import java.time.YearMonth;
import java.util.regex.Pattern;

public final class ValidationUtil {
    private static final Pattern VPA_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+@[a-zA-Z0-9]+$");

    private ValidationUtil() {
    }

    public static boolean isValidVpa(String vpa) {
        if (vpa == null) {
            return false;
        }
        return VPA_PATTERN.matcher(vpa).matches();
    }

    public static boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null) {
            return false;
        }
        String digits = cardNumber.replaceAll("[\\s-]", "");
        if (!digits.matches("\\d{13,19}")) {
            return false;
        }
        int sum = 0;
        boolean alternate = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = digits.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }

    public static String detectCardNetwork(String cardNumber) {
        if (cardNumber == null) {
            return "unknown";
        }
        String digits = cardNumber.replaceAll("[\\s-]", "");
        if (digits.startsWith("4")) {
            return "visa";
        }
        if (digits.length() >= 2) {
            String prefix2 = digits.substring(0, 2);
            if (prefix2.compareTo("51") >= 0 && prefix2.compareTo("55") <= 0) {
                return "mastercard";
            }
            if (prefix2.equals("34") || prefix2.equals("37")) {
                return "amex";
            }
            int prefixInt = Integer.parseInt(prefix2);
            if (prefix2.equals("60") || prefix2.equals("65") || (prefixInt >= 81 && prefixInt <= 89)) {
                return "rupay";
            }
        }
        return "unknown";
    }

    public static boolean isValidExpiry(String monthString, String yearString) {
        if (monthString == null || yearString == null) {
            return false;
        }
        int month;
        int year;
        try {
            month = Integer.parseInt(monthString);
            if (month < 1 || month > 12) {
                return false;
            }
            if (yearString.length() == 2) {
                year = 2000 + Integer.parseInt(yearString);
            } else {
                year = Integer.parseInt(yearString);
            }
        } catch (NumberFormatException ex) {
            return false;
        }
        YearMonth expiry = YearMonth.of(year, month);
        YearMonth now = YearMonth.now();
        return !expiry.isBefore(now);
    }
}

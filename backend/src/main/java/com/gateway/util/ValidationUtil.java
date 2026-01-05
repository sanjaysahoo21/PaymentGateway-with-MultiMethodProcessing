package com.gateway.util;

import java.time.YearMonth;
import java.util.regex.Pattern;

/**
 * Utility class for payment validation.
 * Provides static methods to validate UPI VPA format, card numbers (Luhn algorithm),
 * card networks, and expiry dates.
 */
public final class ValidationUtil {
    private static final Pattern VPA_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+@[a-zA-Z0-9]+$");

    private ValidationUtil() {
    }

    /**
     * Validate UPI Virtual Payment Address (VPA) format.
     * Pattern: username@bank (e.g., user@upi, merchant@okhdfcbank).
     * @param vpa the VPA string to validate
     * @return true if VPA matches pattern, false otherwise
     */
    public static boolean isValidVpa(String vpa) {
        if (vpa == null) {
            return false;
        }
        return VPA_PATTERN.matcher(vpa).matches();
    }

    /**
     * Validate card number using Luhn algorithm.
     * Accepts 13-19 digit cards with optional spaces/dashes.
     * @param cardNumber the card number to validate
     * @return true if card passes Luhn check, false otherwise
     */
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

    /**
     * Detect card network (brand) from card number.
     * Supports Visa (4xxx), Mastercard (51-55xx), Amex (34xx/37xx), RuPay (60/65/81-89xx).
     * @param cardNumber the card number to analyze
     * @return card network as lowercase string (visa, mastercard, amex, rupay) or unknown
     */
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

    /**
     * Validate card expiry date format and check if card is not expired.
     * Accepts month (1-12) and year (YYYY) and checks current date.
     * @param monthString the expiry month as string (1-12)
     * @param yearString the expiry year as string (YYYY)
     * @return true if expiry is in current month or future, false if expired or invalid format
     */
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

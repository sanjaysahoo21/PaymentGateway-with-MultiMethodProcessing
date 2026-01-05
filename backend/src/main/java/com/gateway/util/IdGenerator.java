package com.gateway.util;

import java.security.SecureRandom;

/**
 * Utility class for generating unique IDs for orders and payments.
 * Uses SecureRandom for cryptographically random alphanumeric sequences.
 * Generated IDs are 16 characters with prefixes: order_ (order IDs), pay_ (payment IDs).
 */
public final class IdGenerator {
    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int ID_LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private IdGenerator() {
    }

    /**
     * Generate a unique order ID.
     * Format: order_XXXXXXXXXXXXXXXX (prefix + 16 random alphanumeric chars).
     * @return generated order ID
     */
    public static String generateOrderId() {
        return "order_" + randomId();
    }

    /**
     * Generate a unique payment ID.
     * Format: pay_XXXXXXXXXXXXXXXX (prefix + 16 random alphanumeric chars).
     * @return generated payment ID
     */
    public static String generatePaymentId() {
        return "pay_" + randomId();
    }

    /**
     * Generate random alphanumeric string using SecureRandom.
     * @return 16-character random string
     */
    private static String randomId() {
        StringBuilder sb = new StringBuilder(ID_LENGTH);
        for (int i = 0; i < ID_LENGTH; i++) {
            int idx = RANDOM.nextInt(ALPHANUM.length());
            sb.append(ALPHANUM.charAt(idx));
        }
        return sb.toString();
    }
}

package com.gateway.util;

import java.security.SecureRandom;

public final class IdGenerator {
    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int ID_LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private IdGenerator() {
    }

    public static String generateOrderId() {
        return "order_" + randomId();
    }

    public static String generatePaymentId() {
        return "pay_" + randomId();
    }

    private static String randomId() {
        StringBuilder sb = new StringBuilder(ID_LENGTH);
        for (int i = 0; i < ID_LENGTH; i++) {
            int idx = RANDOM.nextInt(ALPHANUM.length());
            sb.append(ALPHANUM.charAt(idx));
        }
        return sb.toString();
    }
}

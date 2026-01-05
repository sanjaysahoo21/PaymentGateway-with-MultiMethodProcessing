package com.gateway.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdGeneratorTest {

    @Test
    void testGenerateOrderId() {
        String id = IdGenerator.generateOrderId();
        assertTrue(id.startsWith("order_"));
        assertEquals(22, id.length()); // "order_" (6) + 16 alphanumeric
        assertTrue(id.substring(6).matches("[A-Za-z0-9]{16}"));
    }

    @Test
    void testGeneratePaymentId() {
        String id = IdGenerator.generatePaymentId();
        assertTrue(id.startsWith("pay_"));
        assertEquals(20, id.length()); // "pay_" (4) + 16 alphanumeric
        assertTrue(id.substring(4).matches("[A-Za-z0-9]{16}"));
    }

    @Test
    void testIdUniqueness() {
        String id1 = IdGenerator.generateOrderId();
        String id2 = IdGenerator.generateOrderId();
        assertNotEquals(id1, id2);

        String pid1 = IdGenerator.generatePaymentId();
        String pid2 = IdGenerator.generatePaymentId();
        assertNotEquals(pid1, pid2);
    }
}

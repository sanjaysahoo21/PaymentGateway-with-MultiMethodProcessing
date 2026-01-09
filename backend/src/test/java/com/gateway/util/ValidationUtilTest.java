package com.gateway.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    @Test
    void testValidVpa() {
        assertTrue(ValidationUtil.isValidVpa("user@paytm"));
        assertTrue(ValidationUtil.isValidVpa("john.doe@okhdfcbank"));
        assertTrue(ValidationUtil.isValidVpa("user_123@phonepe"));
        assertTrue(ValidationUtil.isValidVpa("a@b"));
    }

    @Test
    void testInvalidVpa() {
        assertFalse(ValidationUtil.isValidVpa("user @paytm")); // space not allowed
        assertFalse(ValidationUtil.isValidVpa("@paytm")); // missing username
        assertFalse(ValidationUtil.isValidVpa("user@@bank")); // double @
        assertFalse(ValidationUtil.isValidVpa("user@")); // missing provider
        assertFalse(ValidationUtil.isValidVpa(null));
    }

    @Test
    void testValidCardNumber() {
        assertTrue(ValidationUtil.isValidCardNumber("4111111111111111")); // Visa
        assertTrue(ValidationUtil.isValidCardNumber("5425233010103442")); // Mastercard
        assertTrue(ValidationUtil.isValidCardNumber("378282246310005")); // Amex
        assertTrue(ValidationUtil.isValidCardNumber("6011111111111117")); // Discover
    }

    @Test
    void testInvalidCardNumber() {
        assertFalse(ValidationUtil.isValidCardNumber("1234")); // too short
        assertFalse(ValidationUtil.isValidCardNumber("12345678901234567890")); // too long
        assertFalse(ValidationUtil.isValidCardNumber("0000000000000000")); // invalid checksum
        assertFalse(ValidationUtil.isValidCardNumber("abcd")); // non-numeric
        assertFalse(ValidationUtil.isValidCardNumber(null));
    }

    @Test
    void testCardNetworkDetection() {
        assertEquals("visa", ValidationUtil.detectCardNetwork("4111111111111111"));
        assertEquals("mastercard", ValidationUtil.detectCardNetwork("5425233010103442"));
        assertEquals("amex", ValidationUtil.detectCardNetwork("378282246310005"));
        assertEquals("rupay", ValidationUtil.detectCardNetwork("6011111111111117"));
        assertEquals("unknown", ValidationUtil.detectCardNetwork("9999999999999999"));
    }

    @Test
    void testExpiryValidation() {
        assertTrue(ValidationUtil.isValidExpiry("12", "26")); // future
        assertTrue(ValidationUtil.isValidExpiry("01", "2027")); // future 4-digit
        assertTrue(ValidationUtil.isValidExpiry("12", "99")); // far future
    }

    @Test
    void testInvalidExpiry() {
        assertFalse(ValidationUtil.isValidExpiry("13", "26")); // invalid month
        assertFalse(ValidationUtil.isValidExpiry("00", "26")); // invalid month
        assertFalse(ValidationUtil.isValidExpiry("12", "20")); // past year
        assertFalse(ValidationUtil.isValidExpiry(null, "26"));
        assertFalse(ValidationUtil.isValidExpiry("12", null));
    }
}

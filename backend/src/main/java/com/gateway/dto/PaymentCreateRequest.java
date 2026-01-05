package com.gateway.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a new payment against an existing order.
 * Encapsulates payment method selection and method-specific details (VPA for UPI, CardDetails for card).
 * Used by the PaymentController POST endpoints for creating payments.
 */
public class PaymentCreateRequest {
    /**
     * The order ID that this payment is being created for.
     * Must reference an existing order in the database.
     * Required field, validated with @NotBlank.
     */
    @NotBlank(message = "order_id is required")
    private String orderId;

    /**
     * Payment method selected by the customer.
     * Valid values: "upi" for UPI/VPA-based payments, "card" for credit/debit cards.
     * Required field, validated with @NotBlank.
     */
    @NotBlank(message = "method is required")
    private String method;

    /**
     * Virtual Payment Address for UPI payments.
     * Format: username@bankname (e.g., user@upi)
     * Only populated when method is "upi".
     */
    private String vpa;

    /**
     * Card details for card-based payments.
     * Only populated when method is "card".
     * Validated with @Valid to trigger CardDetails field validation.
     */
    @Valid
    private CardDetails card;

    public PaymentCreateRequest() {
    }

    /**
     * @return the order ID for this payment
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * @param orderId the order ID to set
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * @return the payment method ("upi" or "card")
     */
    public String getMethod() {
        return method;
    }

    /**
     * @param method the payment method to set
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * @return the UPI VPA address
     */
    public String getVpa() {
        return vpa;
    }

    /**
     * @param vpa the UPI VPA address to set
     */
    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    /**
     * @return the card details for card-based payments
     */
    public CardDetails getCard() {
        return card;
    }

    /**
     * @param card the card details to set
     */
    public void setCard(CardDetails card) {
        this.card = card;
    }

    /**
     * Nested DTO for credit/debit card information.
     * Captures card number, expiry, CVV, and cardholder name.
     * Note: Card numbers are NOT stored in the database; only the network type and last 4 digits are persisted for security.
     */
    public static class CardDetails {
        /**
         * Card number (13-19 digits).
         * Validated with Luhn algorithm at the service layer.
         * Required field.
         */
        @NotBlank(message = "card number is required")
        private String number;

        /**
         * Card expiry month (1-12).
         * Validated at the service layer to ensure it's in the future.
         * Required field.
         */
        @NotBlank(message = "expiry_month is required")
        private String expiryMonth;

        /**
         * Card expiry year (4-digit year, e.g., 2025).
         * Validated at the service layer against the current date.
         * Required field.
         */
        @NotBlank(message = "expiry_year is required")
        private String expiryYear;

        /**
         * Card Verification Value (CVV/CVC) - typically 3-4 digits.
         * Only used for validation; never stored in the database.
         * Required field.
         */
        @NotBlank(message = "cvv is required")
        private String cvv;

        /**
         * Name of the card holder as it appears on the card.
         * Required field.
         */
        @NotBlank(message = "holder_name is required")
        private String holderName;

        public CardDetails() {
        }

        /**
         * @return the card number
         */
        public String getNumber() {
            return number;
        }

        /**
         * @param number the card number to set
         */
        public void setNumber(String number) {
            this.number = number;
        }

        /**
         * @return the expiry month
         */
        public String getExpiryMonth() {
            return expiryMonth;
        }

        /**
         * @param expiryMonth the expiry month to set
         */
        public void setExpiryMonth(String expiryMonth) {
            this.expiryMonth = expiryMonth;
        }

        /**
         * @return the expiry year
         */
        public String getExpiryYear() {
            return expiryYear;
        }

        /**
         * @param expiryYear the expiry year to set
         */
        public void setExpiryYear(String expiryYear) {
            this.expiryYear = expiryYear;
        }

        /**
         * @return the CVV
         */
        public String getCvv() {
            return cvv;
        }

        /**
         * @param cvv the CVV to set
         */
        public void setCvv(String cvv) {
            this.cvv = cvv;
        }

        /**
         * @return the cardholder name
         */
        public String getHolderName() {
            return holderName;
        }

        /**
         * @param holderName the cardholder name to set
         */
        public void setHolderName(String holderName) {
            this.holderName = holderName;
        }
    }
}

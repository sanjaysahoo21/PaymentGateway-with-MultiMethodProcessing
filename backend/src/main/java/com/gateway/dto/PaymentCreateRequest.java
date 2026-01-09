package com.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a new payment.
 */
public class PaymentCreateRequest {
    @JsonProperty("order_id")
    @NotBlank(message = "order_id is required")
    private String orderId;

    @NotBlank(message = "method is required")
    private String method;

    private String vpa;

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

    public String getVpa() {
        return vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public CardDetails getCard() {
        return card;
    }

    public void setCard(CardDetails card) {
        this.card = card;
    }

    public static class CardDetails {
        @NotBlank(message = "card number is required")
        private String number;

        @JsonProperty("expiry_month")
        @NotBlank(message = "expiry_month is required")
        private String expiryMonth;

        @JsonProperty("expiry_year")
        @NotBlank(message = "expiry_year is required")
        private String expiryYear;

        @NotBlank(message = "cvv is required")
        private String cvv;

        @JsonProperty("holder_name")
        @NotBlank(message = "holder_name is required")
        private String holderName;

        public CardDetails() {
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getExpiryMonth() {
            return expiryMonth;
        }

        public void setExpiryMonth(String expiryMonth) {
            this.expiryMonth = expiryMonth;
        }

        public String getExpiryYear() {
            return expiryYear;
        }

        public void setExpiryYear(String expiryYear) {
            this.expiryYear = expiryYear;
        }

        public String getCvv() {
            return cvv;
        }

        public void setCvv(String cvv) {
            this.cvv = cvv;
        }

        public String getHolderName() {
            return holderName;
        }

        public void setHolderName(String holderName) {
            this.holderName = holderName;
        }
    }
}

package com.gateway.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * Request DTO for creating a new order.
 * Encapsulates order details including amount, currency, receipt identifier, and merchant notes.
 * Used by the OrderController POST /api/v1/orders endpoint.
 */
public class OrderCreateRequest {
    /**
     * Order amount in paise. Minimum value is 100 paise (1 INR).
     * Validated with @NotNull and @Min annotations.
     */
    @NotNull(message = "amount must be at least 100")
    @Min(value = 100, message = "amount must be at least 100")
    private Integer amount;

    /**
     * Currency code for the order (e.g., "INR").
     * Defaults to "INR" if not specified.
     */
    private String currency;

    /**
     * Unique receipt identifier provided by the merchant.
     * Used for tracking and deduplication purposes.
     */
    private String receipt;

    /**
     * Arbitrary merchant notes and metadata associated with the order.
     * Useful for storing custom information like customer details, references, etc.
     */
    private Map<String, Object> notes;

    public OrderCreateRequest() {
    }

    /**
     * @return the order amount in paise
     */
    public Integer getAmount() {
        return amount;
    }

    /**
     * @param amount the order amount in paise (minimum 100)
     */
    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    /**
     * @return the currency code
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * @param currency the currency code to set
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * @return the receipt identifier
     */
    public String getReceipt() {
        return receipt;
    }

    /**
     * @param receipt the receipt identifier to set
     */
    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }

    /**
     * @return the merchant notes metadata
     */
    public Map<String, Object> getNotes() {
        return notes;
    }

    /**
     * @param notes the merchant notes metadata to set
     */
    public void setNotes(Map<String, Object> notes) {
        this.notes = notes;
    }
}

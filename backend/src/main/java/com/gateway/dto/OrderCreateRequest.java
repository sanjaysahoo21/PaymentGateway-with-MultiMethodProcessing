package com.gateway.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class OrderCreateRequest {
    @NotNull(message = "amount must be at least 100")
    @Min(value = 100, message = "amount must be at least 100")
    private Integer amount;

    private String currency;

    private String receipt;

    private Map<String, Object> notes;

    public OrderCreateRequest() {
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getReceipt() {
        return receipt;
    }

    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }

    public Map<String, Object> getNotes() {
        return notes;
    }

    public void setNotes(Map<String, Object> notes) {
        this.notes = notes;
    }
}

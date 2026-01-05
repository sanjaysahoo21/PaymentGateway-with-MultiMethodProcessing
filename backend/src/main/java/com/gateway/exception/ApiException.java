package com.gateway.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String code;
    private final String description;

    public ApiException(HttpStatus status, String code, String description) {
        super(description);
        this.status = status;
        this.code = code;
        this.description = description;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}

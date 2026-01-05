package com.gateway.exception;

import org.springframework.http.HttpStatus;

/**
 * Custom API exception with HTTP status, error code, and description.
 * Used throughout services and controllers for consistent error handling.
 */
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String code;
    private final String description;

    /**
     * Create API exception.
     * @param status HTTP status code (BAD_REQUEST, UNAUTHORIZED, NOT_FOUND, etc)
     * @param code error code (AUTHENTICATION_ERROR, BAD_REQUEST_ERROR, NOT_FOUND_ERROR, etc)
     * @param description human-readable error message
     */
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

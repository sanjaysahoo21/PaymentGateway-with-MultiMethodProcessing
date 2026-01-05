package com.gateway.config;

import com.gateway.dto.ErrorResponse;
import com.gateway.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for REST controllers.
 * Transforms exceptions into standardized ErrorResponse with appropriate HTTP status codes.
 * Handles custom ApiException, validation errors, and generic exceptions.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle custom ApiException thrown by services/controllers.
     * @param ex the ApiException with status, code, and description
     * @return ResponseEntity with ErrorResponse and appropriate HTTP status
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        ErrorResponse body = new ErrorResponse(ex.getCode(), ex.getDescription());
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    /**
     * Handle request body validation errors from @Valid annotations.
     * Extracts first validation error message or provides default.
     * @param ex the MethodArgumentNotValidException
     * @return ResponseEntity with BAD_REQUEST_ERROR and validation message
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Validation failed");
        ErrorResponse body = new ErrorResponse("BAD_REQUEST_ERROR", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handle unexpected exceptions.
     * @param ex any uncaught exception
     * @return ResponseEntity with INTERNAL_SERVER_ERROR
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex) {
        ErrorResponse body = new ErrorResponse("BAD_REQUEST_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}

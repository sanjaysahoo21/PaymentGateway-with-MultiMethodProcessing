package com.gateway.dto;

/**
 * Standard error response format for API errors.
 * Contains error code and description wrapped in nested error object.
 */
public class ErrorResponse {
    private ErrorBody error;

    public ErrorResponse() {
    }

    /**
     * Create error response with code and description.
     * @param code error code (e.g., BAD_REQUEST_ERROR, AUTHENTICATION_ERROR)
     * @param description human-readable error message
     */
    public ErrorResponse(String code, String description) {
        this.error = new ErrorBody(code, description);
    }

    public ErrorBody getError() {
        return error;
    }

    public void setError(ErrorBody error) {
        this.error = error;
    }

    /**
     * Nested error body with code and description.
     */
    public static class ErrorBody {
        private String code;
        private String description;

        public ErrorBody() {
        }

        /**
         * Create error body.
         * @param code error code identifier
         * @param description error message
         */
        public ErrorBody(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}

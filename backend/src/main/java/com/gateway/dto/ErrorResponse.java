package com.gateway.dto;

public class ErrorResponse {
    private ErrorBody error;

    public ErrorResponse() {
    }

    public ErrorResponse(String code, String description) {
        this.error = new ErrorBody(code, description);
    }

    public ErrorBody getError() {
        return error;
    }

    public void setError(ErrorBody error) {
        this.error = error;
    }

    public static class ErrorBody {
        private String code;
        private String description;

        public ErrorBody() {
        }

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

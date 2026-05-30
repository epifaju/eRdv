package com.erdv.dto;

import java.util.Map;

public class ErrorResponse {

    private final String message;
    private final Map<String, String> fieldErrors;

    public ErrorResponse(String message, Map<String, String> fieldErrors) {
        this.message = message;
        this.fieldErrors = fieldErrors;
    }

    public static ErrorResponse of(String message) {
        return new ErrorResponse(message, null);
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}

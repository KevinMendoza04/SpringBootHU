package com.example.eventify.exception;

/**
 * BusinessRuleViolationException - Exception thrown when a business rule is violated.
 * Corresponds to HTTP 422 Unprocessable Entity or 400 Bad Request response.
 */
public class BusinessRuleViolationException extends RuntimeException {
    private final String rule;

    public BusinessRuleViolationException(String message) {
        super(message);
        this.rule = null;
    }

    public BusinessRuleViolationException(String message, String rule) {
        super(message);
        this.rule = rule;
    }

    public String getRule() {
        return rule;
    }
}

package com.technova.washtrack.shared.exception;

/** Thrown when an operation violates a domain rule. Mapped to HTTP 422. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}

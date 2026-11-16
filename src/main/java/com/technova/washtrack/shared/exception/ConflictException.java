package com.technova.washtrack.shared.exception;

/** Thrown when a request clashes with the current state of a resource (e.g. no capacity left). Mapped to HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}

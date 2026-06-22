package com.renansiqueira.claudelab.application;

/**
 * Thrown when a resource conflicts with existing state (e.g. duplicate id).
 * Mapped to HTTP 409.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}

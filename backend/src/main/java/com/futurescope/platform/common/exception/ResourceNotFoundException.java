package com.futurescope.platform.common.exception;

/**
 * Thrown when a requested resource does not exist or is not available.
 * Mapped to HTTP 404 by GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

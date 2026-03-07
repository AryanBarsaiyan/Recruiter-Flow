package com.futurescope.platform.common.exception;

/**
 * Thrown when a candidate tries to apply to a job they have already applied to.
 * Mapped to HTTP 409 Conflict by GlobalExceptionHandler.
 */
public class AlreadyAppliedException extends RuntimeException {

    public AlreadyAppliedException(String message) {
        super(message);
    }
}

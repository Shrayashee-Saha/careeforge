package com.aizen.exception;

/**
 * Unchecked exception thrown by model setters and ValidationUtil when
 * user-supplied data fails a business rule (e.g. empty name, malformed
 * email, CGPA out of range). Kept unchecked because validation failures
 * are expected to be handled close to the UI via try/catch and surfaced
 * as an Alert, not propagated through every method signature.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

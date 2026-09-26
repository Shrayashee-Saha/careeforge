package com.aizen.exception;

/**
 * Checked exception thrown by ApiService when the Gemini REST call fails
 * (network error, non-2xx response, malformed JSON). Callers are expected
 * to catch this and fall back to the local heuristic generator rather than
 * crash the UI thread.
 */
public class ApiException extends Exception {

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.aizen.util;

import com.aizen.exception.ValidationException;

import java.util.regex.Pattern;

/**
 * Small collection of static validation helpers used by controllers before
 * data is handed off to model setters (which perform the same checks again
 * defensively - belt and suspenders). Centralizing the regex patterns here
 * avoids duplicating them across LoginController / RegisterController /
 * ResumeBuilderController.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private ValidationUtil() {
    }

    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " cannot be empty.");
        }
    }

    public static void requireValidEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Please enter a valid email address.");
        }
    }

    public static void requireMinLength(String value, int min, String fieldName) {
        if (value == null || value.length() < min) {
            throw new ValidationException(fieldName + " must be at least " + min + " characters.");
        }
    }

    public static void requirePasswordsMatch(String password, String confirm) {
        if (password == null || !password.equals(confirm)) {
            throw new ValidationException("Passwords do not match.");
        }
    }

    public static void requireInRange(double value, double min, double max, String fieldName) {
        if (value < min || value > max) {
            throw new ValidationException(fieldName + " must be between " + min + " and " + max + ".");
        }
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

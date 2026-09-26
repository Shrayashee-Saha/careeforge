package com.aizen.exception;

/**
 * Checked exception thrown when a database operation (connection, query,
 * update) fails. Wrapping SQLExceptions in this custom type lets the
 * service/controller layers handle persistence failures without depending
 * on java.sql directly, and lets us attach a friendlier message for the UI.
 */
public class DatabaseException extends Exception {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}

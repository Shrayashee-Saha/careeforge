package com.aizen.db;

import com.aizen.exception.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Central connection factory for the SQLite-backed AiZen datastore.
 * Every DAO obtains connections through {@link #getConnection()} rather
 * than opening its own, so pragma settings (foreign keys) are applied
 * consistently everywhere.
 *
 * A SQLite database file is stored next to the working directory as
 * {@code aizen.db}, created automatically on first run by
 * {@link DatabaseSetup}.
 */
public final class Database {

    private static final String DB_FILE = "aizen.db";
    private static final String URL = "jdbc:sqlite:" + DB_FILE;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("SQLite JDBC driver not found on classpath: " + e.getMessage());
        }
    }

    private Database() {
        // static utility class - never instantiated
    }

    /**
     * Opens a brand-new JDBC connection with foreign-key enforcement turned
     * on. SQLite connections are cheap; each DAO method opens, uses, and
     * closes its own connection via try-with-resources rather than sharing
     * one across threads, which keeps the background Task classes safe.
     */
    public static Connection getConnection() throws DatabaseException {
        try {
            Connection conn = DriverManager.getConnection(URL);
            try (Statement pragma = conn.createStatement()) {
                pragma.execute("PRAGMA foreign_keys = ON;");
            }
            return conn;
        } catch (SQLException e) {
            throw new DatabaseException("Unable to connect to the AiZen database.", e);
        }
    }
}

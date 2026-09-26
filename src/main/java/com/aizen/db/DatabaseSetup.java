package com.aizen.db;

import com.aizen.exception.DatabaseException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates every SQLite table AiZen needs, if they don't already exist.
 * Called once from {@code Main.start()} before any UI is shown. Idempotent:
 * safe to call on every application launch.
 */
public final class DatabaseSetup {

    private DatabaseSetup() {
    }

    public static void initialize() throws DatabaseException {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    email TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    salt TEXT NOT NULL,
                    full_name TEXT,
                    created_at TEXT NOT NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS resumes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    template_style TEXT NOT NULL DEFAULT 'CLASSIC_EXECUTIVE',
                    job_title TEXT,
                    full_name TEXT,
                    email TEXT,
                    phone TEXT,
                    location TEXT,
                    linkedin TEXT,
                    summary TEXT,
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS experiences (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    resume_id INTEGER NOT NULL,
                    company TEXT NOT NULL,
                    title TEXT NOT NULL,
                    location TEXT,
                    start_date TEXT,
                    end_date TEXT,
                    is_current INTEGER NOT NULL DEFAULT 0,
                    description TEXT,
                    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS education (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    resume_id INTEGER NOT NULL,
                    institution TEXT NOT NULL,
                    degree TEXT NOT NULL,
                    field_of_study TEXT,
                    start_date TEXT,
                    end_date TEXT,
                    gpa REAL,
                    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS projects (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    resume_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    description TEXT,
                    tech_stack TEXT,
                    link TEXT,
                    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS certifications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    resume_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    issuer TEXT,
                    date_issued TEXT,
                    credential_id TEXT,
                    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS skills (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    resume_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    proficiency TEXT NOT NULL DEFAULT 'INTERMEDIATE',
                    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
                );
            """);

        } catch (SQLException e) {
            throw new DatabaseException("Failed to initialize the AiZen database schema.", e);
        }
    }
}

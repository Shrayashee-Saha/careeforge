package com.aizen.model;

import com.aizen.exception.ValidationException;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * An AiZen account. Kept separate from the {@link Person}/{@link Applicant}/
 * {@link Student} hierarchy on purpose: User is an authentication record
 * (credentials + session identity), while Person and its subclasses model
 * the human being described *inside* a resume. A User owns zero or more
 * {@link Resume}s.
 */
public class User {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    public User(int id, String username, String email, String passwordHash, String salt, String fullName, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.fullName = fullName;
        this.createdAt = createdAt;
    }

    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private String salt;
    private String fullName;
    private LocalDateTime createdAt;

    public User(String username, String email, String passwordHash, String salt, String fullName) {
        setUsername(username);
        setEmail(email);
        this.passwordHash = passwordHash;
        this.salt = salt;
        setFullName(fullName);
        this.createdAt = LocalDateTime.now();
    }

    public User() {
        this.createdAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) {
        if (username == null || username.isBlank() || username.length() < 3) {
            throw new ValidationException("Username must be at least 3 characters.");
        }
        this.username = username.trim();
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Invalid email address: " + email);
        }
        this.email = email.trim().toLowerCase();
    }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) {
        this.fullName = fullName == null ? "" : fullName.trim();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', email='" + email + "'}";
    }
}

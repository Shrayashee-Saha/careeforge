package com.aizen.model;

import com.aizen.exception.ValidationException;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Abstract base class for anyone represented in AiZen (an {@link Applicant}
 * or a {@link Student}). Holds the identity fields every person needs and
 * forces subclasses to define what role they play via {@link #getRole()}.
 *
 * Demonstrates: ABSTRACTION + INHERITANCE (root of the Person -> Applicant
 * -> Student hierarchy) and ENCAPSULATION (private fields, validated
 * setters).
 */
public abstract class Person {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private int id;

    public Person(String phone, String email, String name, int id) {
        this.phone = phone;
        this.email = email;
        this.name = name;
        this.id = id;
    }

    private String name;
    private String email;
    private String phone;

    protected Person(String name, String email, String phone) {
        setName(name);
        setEmail(email);
        setPhone(phone);
    }

    /** No-arg constructor for frameworks / DAO row-mapping, followed by setters. */
    protected Person() {
    }

    /**
     * Every concrete subclass must state what kind of person it represents.
     * This is the polymorphic hook: calling {@code person.getRole()} on a
     * {@code Person} reference dispatches to the actual runtime subtype.
     */
    public abstract String getRole();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Name cannot be empty.");
        }
        if (name.length() > 100) {
            throw new ValidationException("Name cannot exceed 100 characters.");
        }
        this.name = name.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Invalid email address: " + email);
        }
        this.email = email.trim().toLowerCase();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone != null && !phone.isBlank() && !phone.matches("^[+()\\-\\s\\d]{6,20}$")) {
            throw new ValidationException("Invalid phone number: " + phone);
        }
        this.phone = phone == null ? "" : phone.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person person)) return false;
        return id == person.id && Objects.equals(email, person.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return getRole() + "{id=" + id + ", name='" + name + "', email='" + email + "'}";
    }
}

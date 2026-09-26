package com.aizen.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Salted SHA-256 password hashing. Each user gets a fresh random salt
 * (stored alongside the hash in the {@code users} table); the salt is
 * prepended to the plaintext password before hashing so identical
 * passwords never produce identical hashes across accounts, and rainbow
 * tables become useless.
 *
 * Not intended to compete with a full KDF like bcrypt/Argon2 for a
 * production system, but is a correct, dependency-free salted-hash
 * implementation appropriate for this coursework project.
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String plainPassword, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Base64.getDecoder().decode(salt));
            byte[] hashed = digest.digest(plainPassword.getBytes());
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available.", e);
        }
    }

    public static boolean verify(String plainPassword, String salt, String expectedHash) {
        String computed = hash(plainPassword, salt);
        return MessageDigest.isEqual(computed.getBytes(), expectedHash.getBytes());
    }
}

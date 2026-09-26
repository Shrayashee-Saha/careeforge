package com.aizen.service;

import com.aizen.dao.UserDAO;
import com.aizen.exception.DatabaseException;
import com.aizen.exception.ValidationException;
import com.aizen.model.User;
import com.aizen.util.PasswordUtil;
import com.aizen.util.ValidationUtil;

import java.util.Optional;

/** Registration and login on top of UserDAO. */
public class AuthService {
    private final UserDAO userDao = new UserDAO();

    public User register(String username, String password, String fullName, String email)
            throws DatabaseException {
        String u = username == null ? null : username.trim();
        ValidationUtil.requireNonBlank(fullName, "Full name");
        ValidationUtil.requireValidEmail(email);
        String name = fullName.trim();
        String mail = email.trim();
        if (password == null || password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters.");
        }
        if (userDao.findByUsername(u).isPresent()) {
            throw new ValidationException("That username is already taken.");
        }
        User user = new User();
        user.setUsername(u);
        user.setFullName(name);
        user.setEmail(mail);
        user.setSalt(PasswordUtil.generateSalt());
        user.setPasswordHash(PasswordUtil.hash(password, user.getSalt()));
        return userDao.save(user);
    }

    public User login(String username, String password) throws DatabaseException {
        if (ValidationUtil.isBlank(username) || password == null || password.isEmpty()) {
            throw new ValidationException("Enter your username and password.");
        }
        Optional<User> found = userDao.findByUsername(username.trim());
        if (found.isEmpty() || !PasswordUtil.verify(password, found.get().getSalt(), found.get().getPasswordHash())) {
            throw new ValidationException("Invalid username or password.");
        }
        return found.get();
    }
}

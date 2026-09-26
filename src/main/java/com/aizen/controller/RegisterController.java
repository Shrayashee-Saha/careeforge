package com.aizen.controller;

import com.aizen.dao.UserDAO;
import com.aizen.exception.DatabaseException;
import com.aizen.exception.ValidationException;
import com.aizen.model.User;
import com.aizen.thread.TaskManager;
import com.aizen.util.PasswordUtil;
import com.aizen.util.SceneManager;
import com.aizen.util.ValidationUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controller for {@code register.fxml}. Validates input client-side with
 * {@link ValidationUtil}, then creates the account on a background
 * {@link Task} (password hashing + DB insert). The "I am a student"
 * checkbox is UI-only metadata for this release; a Student-specific
 * Applicant record can be attached to a resume later from the Resume
 * Builder, where the full Person/Applicant/Student hierarchy is exercised.
 */
public class RegisterController {

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox studentCheckBox;
    @FXML private Label errorLabel;
    @FXML private Button registerButton;
    @FXML private ProgressIndicator progressIndicator;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void handleRegister() {
        hideError();

        String fullName = fullNameField.getText();
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        try {
            ValidationUtil.requireNonBlank(fullName, "Full name");
            ValidationUtil.requireMinLength(username, 3, "Username");
            ValidationUtil.requireValidEmail(email);
            ValidationUtil.requireMinLength(password, 6, "Password");
            ValidationUtil.requirePasswordsMatch(password, confirm);
        } catch (ValidationException ex) {
            showError(ex.getMessage());
            return;
        }

        setLoading(true);

        Task<User> task = new Task<>() {
            @Override
            protected User call() throws DatabaseException {
                String salt = PasswordUtil.generateSalt();
                String hash = PasswordUtil.hash(password, salt);
                User user = new User(username.trim(), email.trim(), hash, salt, fullName.trim());
                return userDAO.save(user);
            }
        };

        task.setOnSucceeded(e -> {
            setLoading(false);
            SceneManager.setCurrentUser(task.getValue());
            SceneManager.switchScene("main_layout.fxml");
        });

        task.setOnFailed(e -> {
            setLoading(false);
            Throwable ex = task.getException();
            showError(ex != null ? ex.getMessage() : "Registration failed. Please try again.");
        });

        TaskManager.submit(task);
    }

    @FXML
    private void handleGoToLogin() {
        SceneManager.switchScene("login.fxml");
    }

    private void setLoading(boolean loading) {
        Platform.runLater(() -> {
            registerButton.setDisable(loading);
            progressIndicator.setVisible(loading);
            progressIndicator.setManaged(loading);
        });
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        });
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}

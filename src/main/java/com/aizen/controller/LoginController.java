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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

import java.util.Optional;

/**
 * Controller for {@code login.fxml}. Authenticates against the local
 * SQLite {@code users} table with salted-hash verification. The lookup
 * runs on a background {@link Task} so a slow disk never freezes the UI,
 * with the login button disabled and a spinner shown while it runs.
 */
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private ProgressIndicator progressIndicator;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin() {
        hideError();
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            ValidationUtil.requireNonBlank(username, "Username");
            ValidationUtil.requireNonBlank(password, "Password");
        } catch (ValidationException ex) {
            showError(ex.getMessage());
            return;
        }

        setLoading(true);

        Task<User> task = new Task<>() {
            @Override
            protected User call() throws DatabaseException {
                Optional<User> found = userDAO.findByUsername(username.trim());
                if (found.isEmpty()) {
                    throw new DatabaseException("No account found with that username.");
                }
                User user = found.get();
                if (!PasswordUtil.verify(password, user.getSalt(), user.getPasswordHash())) {
                    throw new DatabaseException("Incorrect password.");
                }
                return user;
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
            showError(ex != null ? ex.getMessage() : "Login failed. Please try again.");
        });

        TaskManager.submit(task);
    }

    @FXML
    private void handleGoToRegister() {
        SceneManager.switchScene("register.fxml");
    }

    private void setLoading(boolean loading) {
        Platform.runLater(() -> {
            loginButton.setDisable(loading);
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

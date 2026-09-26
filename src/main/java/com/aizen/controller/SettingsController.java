package com.aizen.controller;

import com.aizen.model.User;
import com.aizen.service.ApiService;
import com.aizen.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;

/**
 * Controller for {@code settings.fxml}. Exposes the dark/light theme
 * toggle (delegating to {@link SceneManager}), reports whether the
 * {@code AIZEN_API_KEY} environment variable is configured, and offers
 * logout.
 */
public class SettingsController {

    @FXML private ToggleButton darkModeToggle;
    @FXML private Label apiKeyStatusLabel;
    @FXML private Label accountLabel;

    private final ApiService apiService = new ApiService();

    @FXML
    private void initialize() {
        darkModeToggle.setSelected(SceneManager.isDarkMode());
        darkModeToggle.setText(SceneManager.isDarkMode() ? "On" : "Off");

        apiKeyStatusLabel.setText(apiService.isApiConfigured()
                ? "✅ AIZEN_API_KEY is configured — live Gemini generation is active."
                : "⚠️ AIZEN_API_KEY is not set — using the built-in local fallback generator.");

        User user = SceneManager.getCurrentUser();
        if (user != null) {
            accountLabel.setText("Signed in as " + user.getFullName() + " (" + user.getUsername() + ")");
        }
    }

    @FXML
    private void handleToggleDarkMode() {
        boolean enabled = darkModeToggle.isSelected();
        darkModeToggle.setText(enabled ? "On" : "Off");
        SceneManager.setDarkMode(darkModeToggle.getScene(), enabled);
    }

    @FXML
    private void handleLogout() {
        SceneManager.logout();
    }
}

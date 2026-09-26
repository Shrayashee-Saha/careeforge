package com.aizen.controller;

import com.aizen.util.SceneManager;
import com.aizen.util.SceneManager.View;
import com.aizen.util.Session;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.EnumMap;
import java.util.Map;
import com.aizen.controller.InterviewCoachView;
/** Application shell: sidebar navigation + swappable content area. */
public class MainController {
    private static MainController instance;

    @FXML private StackPane contentPane;
    @FXML private Label lblUser;
    @FXML private Button btnDashboard;
    @FXML private Button btnResume;
    @FXML private Button btnCover;
    @FXML private Button btnSaved;
    @FXML private Button btnTheme;

    private final Map<View, Button> navButtons = new EnumMap<>(View.class);

    public static MainController getInstance() {
        return instance;
    }

    @FXML
    private void initialize() {
        instance = this;
        navButtons.put(View.DASHBOARD, btnDashboard);
        navButtons.put(View.RESUME, btnResume);
        navButtons.put(View.COVER_LETTER, btnCover);
        navButtons.put(View.SAVED, btnSaved);
        navButtons.put(View.PREVIEW, btnSaved);
        lblUser.setText("Signed in as\n" + Session.getCurrentUser().getFullName());
        updateThemeButton();
        navigate(View.DASHBOARD);
    }

    /** Swaps the content area for the requested screen. */
    public void navigate(View view) {
        Parent content = SceneManager.load(view.path());
        contentPane.getChildren().setAll(content);
        navButtons.values().forEach(b -> b.getStyleClass().remove("nav-active"));
        Button active = navButtons.get(view);
        if (active != null) {
            active.getStyleClass().add("nav-active");
        }
    }

    @FXML private void onDashboard() { navigate(View.DASHBOARD); }

    @FXML
    private void onResume() {
        Session.setResumeToEdit(null);
        navigate(View.RESUME);
    }

    @FXML private void onCoverLetter() { navigate(View.COVER_LETTER); }

    @FXML private void onSaved() { navigate(View.SAVED); }
    @FXML
    private void onInterview() {
        contentPane.getChildren().setAll(new InterviewCoachView());
    }
    @FXML
    private void onToggleTheme() {
        SceneManager.toggleTheme();
        updateThemeButton();
    }

    @FXML
    private void onLogout() {
        Session.logout();
        instance = null;
        SceneManager.showLogin();
    }

    private void updateThemeButton() {
        btnTheme.setText(SceneManager.isDark() ? "Light mode" : "Dark mode");
    }
}

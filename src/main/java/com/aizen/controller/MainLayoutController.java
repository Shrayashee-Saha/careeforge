package com.aizen.controller;

import com.aizen.model.User;
import com.aizen.service.ApiService;
import com.aizen.thread.TaskManager;
import com.aizen.util.SceneManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import com.aizen.controller.InterviewCoachView;
/**
 * Controller for {@code main_layout.fxml} - the persistent shell (sidebar
 * navigation + top bar) that hosts every other screen inside its center
 * {@link StackPane}. Screens are swapped by loading their FXML fragment
 * fresh each time via {@link SceneManager#loadFragment}, which keeps each
 * sub-controller stateless between visits and avoids any FXML injection
 * conflicts between fragments.
 *
 * Exposes a static {@link #getInstance()} accessor so a fragment
 * controller (e.g. a Dashboard "quick action" tile) can ask the shell to
 * navigate elsewhere without the fragments needing a direct reference to
 * each other.
 */
public class MainLayoutController {

    private static MainLayoutController instance;

    @FXML private StackPane contentArea;
    @FXML private Label welcomeLabel;
    @FXML private Label networkStatusLabel;
    @FXML private Circle networkStatusDot;
    @FXML private Button themeToggleButton;
    @FXML private Button navDashboard;
    @FXML private Button navResumeBuilder;
    @FXML private Button navCoverLetter;
    @FXML private Button navSavedResumes;
    @FXML private Button navSettings;
    @FXML private Button navInterview;
    @FXML private Button navSalary;
    @FXML private Button navLinkedIn;
    private final ApiService apiService = new ApiService();

    @FXML
    private void initialize() {
        instance = this;
        User user = SceneManager.getCurrentUser();
        welcomeLabel.setText("Welcome back, " + (user != null ? user.getFullName() : "Guest") + " 👋");
        themeToggleButton.setText(SceneManager.isDarkMode() ? "☀️" : "🌙");
        showDashboard();
        checkNetworkStatusAsync();

    }

    public static MainLayoutController getInstance() {
        return instance;
    }

    @FXML
    private void showDashboard() {
        SceneManager.LoadResult<DashboardController> result = SceneManager.loadFragment("dashboard.fxml");
        setContent(result.root());
    }

    @FXML
    private void showResumeBuilder() {
        SceneManager.LoadResult<ResumeBuilderController> result = SceneManager.loadFragment("resume_builder.fxml");
        setContent(result.root());
    }

    @FXML
    private void showCoverLetter() {
        SceneManager.LoadResult<CoverLetterController> result = SceneManager.loadFragment("cover_letter.fxml");
        setContent(result.root());
    }

    @FXML
    private void showSavedResumes() {
        SceneManager.LoadResult<SavedResumesController> result = SceneManager.loadFragment("saved_resumes.fxml");
        setContent(result.root());
    }

    @FXML
    private void showSettings() {
        SceneManager.LoadResult<SettingsController> result = SceneManager.loadFragment("settings.fxml");
        setContent(result.root());
    }

    /** Public navigation entry point for other controllers (e.g. dashboard quick-action tiles). */
    public void navigateTo(String view) {
        switch (view) {
            case "dashboard" -> showDashboard();
            case "resume_builder" -> showResumeBuilder();
            case "cover_letter" -> showCoverLetter();
            case "saved_resumes" -> showSavedResumes();
            case "settings" -> showSettings();
            case "interview_coach" -> showInterviewCoach();
            case "salary_negotiator" -> showSalaryNegotiator();
            case "linkedin_optimizer" -> showLinkedInOptimizer();
            default -> showDashboard();
        }
    }
    @FXML
    private void showSalaryNegotiator() {
        setContent(new SalaryNegotiatorView());
    }

    @FXML
    private void showLinkedInOptimizer() {
        setContent(new LinkedInOptimizerView());
    }
    @FXML
    private void handleToggleTheme() {
        SceneManager.toggleTheme(contentArea.getScene());
        themeToggleButton.setText(SceneManager.isDarkMode() ? "☀️" : "🌙");
    }
    @FXML
    private void showInterviewCoach() {
        setContent(new InterviewCoachView());
    }
    @FXML
    private void handleLogout() {
        SceneManager.logout();
    }

    private void setContent(javafx.scene.Parent node) {
        contentArea.getChildren().setAll(node);
    }

    /** Pings the Gemini API host in the background to reflect real AI-engine reachability, not just key presence. */
    private void checkNetworkStatusAsync() {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                if (!apiService.isApiConfigured()) {
                    return null; // tri-state: not configured
                }
                try {
                    HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://generativelanguage.googleapis.com"))
                            .timeout(Duration.ofSeconds(4))
                            .GET()
                            .build();
                    HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
                    return response.statusCode() < 500;
                } catch (Exception e) {
                    return false;
                }
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            Boolean reachable = task.getValue();
            if (reachable == null) {
                networkStatusDot.getStyleClass().setAll("status-dot-warn");
                networkStatusLabel.setText("AI Engine: Local Fallback (no API key)");
            } else if (reachable) {
                networkStatusDot.getStyleClass().setAll("status-dot-ok");
                networkStatusLabel.setText("AI Engine: Connected");
            } else {
                networkStatusDot.getStyleClass().setAll("status-dot-error");
                networkStatusLabel.setText("AI Engine: Offline (using fallback)");
            }
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            networkStatusDot.getStyleClass().setAll("status-dot-error");
            networkStatusLabel.setText("AI Engine: Offline (using fallback)");
        }));

        TaskManager.submit(task);
    }
}

package com.aizen.util;

import com.aizen.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;

/**
 * Central navigation + session + theme controller for the whole app.
 * Holds the single {@link Stage} the application ever shows, the currently
 * logged-in {@link User} (simple in-memory session cache - AiZen is a
 * single-window desktop app so there is only ever one active session),
 * and the light/dark stylesheet currently applied.
 *
 * Every FXML lives under {@code /com/aizen/fxml/} and every stylesheet
 * under {@code /com/aizen/css/}; callers pass just the file name.
 */
public final class SceneManager {

    private static Stage primaryStage;
    private static User currentUser;
    private static boolean darkMode = false;

    private static final String FXML_BASE = "/com/aizen/fxml/";
    private static final String CSS_BASE = "/com/aizen/css/";
    private static final String LIGHT_CSS = "style.css";
    private static final String DARK_CSS = "dark.css";

    private SceneManager() {
    }

    public static void init(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("AiZen — AI Resume & Career Builder v2.0");
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(720);
    }
    public static boolean isDark() {
        return darkMode;
    }
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
        switchScene("login.fxml");
    }
    public static Parent load(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            return loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    public static void error(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public static void toggleTheme() {
        darkMode = !darkMode;
        if (primaryStage != null && primaryStage.getScene() != null) {
            applyTheme(primaryStage.getScene());
        }
    }
    public static void showLogin() {
        Parent root = load(View.LOGIN.path());
        if (root != null) {
            Scene scene = new Scene(root);
            applyTheme(scene);
            primaryStage.setScene(scene);
            primaryStage.show();
        }
    }
    public enum View {
        DASHBOARD("/com/aizen/fxml/dashboard.fxml"),
        RESUME("/com/aizen/fxml/resume_builder.fxml"),
        COVER_LETTER("/com/aizen/fxml/cover_letter.fxml"),
        SAVED("/com/aizen/fxml/saved_resumes.fxml"),
        PREVIEW("/com/aizen/fxml/preview.fxml"),
        LOGIN("/com/aizen/fxml/login.fxml"),
        REGISTER("/com/aizen/fxml/register.fxml"),
        SETTINGS("/com/aizen/fxml/settings.fxml");

        private final String path;

        View(String path) {
            this.path = path;
        }

        public String path() {
            return path;
        }
    }
    public static boolean isDarkMode() {
        return darkMode;
    }

    /**
     * Loads a top-level FXML screen (login, register, main_layout) and
     * replaces whatever is currently showing on the primary stage.
     */
    public static void switchScene(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(resolveFxml(fxmlFile));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            applyTheme(scene);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load screen: " + fxmlFile, e);
        }
    }

    /** Loads an FXML fragment without touching the primary stage - used for the MainLayout's content area. */
    public static <T> LoadResult<T> loadFragment(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(resolveFxml(fxmlFile));
            Parent root = loader.load();
            T controller = loader.getController();
            return new LoadResult<>(root, controller);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load fragment: " + fxmlFile, e);
        }
    }

    public static URL resolveFxml(String fxmlFile) {
        URL url = SceneManager.class.getResource(FXML_BASE + fxmlFile);
        return Objects.requireNonNull(url, "FXML not found: " + FXML_BASE + fxmlFile);
    }

    public static void applyTheme(Scene scene) {
        scene.getStylesheets().clear();
        URL css = SceneManager.class.getResource(CSS_BASE + (darkMode ? DARK_CSS : LIGHT_CSS));
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
    }

    public static void toggleTheme(Scene scene) {
        darkMode = !darkMode;
        applyTheme(scene);
    }

    public static void setDarkMode(Scene scene, boolean enabled) {
        darkMode = enabled;
        applyTheme(scene);
    }

    /** Simple pair carrying a loaded fragment's root node and its controller instance. */
    public record LoadResult<T>(Parent root, T controller) {
    }
}

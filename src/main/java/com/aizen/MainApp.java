package com.aizen;

import com.aizen.db.DatabaseSetup;
import com.aizen.exception.DatabaseException;
import com.aizen.thread.TaskManager;
import com.aizen.util.SceneManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/** JavaFX entry point of AiZen - AI Resume & Career Builder. */
public class MainApp extends Application {
    private DatabaseException startupError;

    /** Runs on the JavaFX launcher thread (not the Application Thread): safe place for DB setup. */
    @Override
    public void init() {
        try {
            DatabaseSetup.initialize();
        } catch (DatabaseException e) {
            startupError = e;
        }
    }

    @Override
    public void start(Stage stage) {
        SceneManager.init(stage);
        if (startupError != null) {
            SceneManager.error("Database error", startupError.getMessage());
            Platform.exit();
            return;
        }
        SceneManager.showLogin();
    }

    @Override
    public void stop() {
        TaskManager.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

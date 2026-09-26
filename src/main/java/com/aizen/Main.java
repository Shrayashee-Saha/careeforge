package com.aizen;

import com.aizen.db.DatabaseSetup;
import com.aizen.thread.TaskManager;
import com.aizen.util.SceneManager;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.stage.Stage;
import com.aizen.controller.InterviewCoachView;
/**
 * Application entry point. Initializes the SQLite schema, wires up
 * {@link SceneManager} with the primary stage, and shows the login screen.
 * Also registers a clean shutdown hook for the shared background thread
 * pool ({@link TaskManager}) when the window is closed.
 */
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            DatabaseSetup.initialize();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "AiZen could not initialize its local database and cannot continue.\n\n" + e.getMessage());
            alert.setHeaderText("Startup Error");
            alert.showAndWait();
            return;
        }

        SceneManager.init(primaryStage);
        SceneManager.switchScene("login.fxml");

        primaryStage.setOnCloseRequest(event -> TaskManager.shutdown());
        primaryStage.show();
    }

    @Override
    public void stop() {
        TaskManager.shutdown();
    }
}

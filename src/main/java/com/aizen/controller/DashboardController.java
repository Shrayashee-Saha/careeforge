package com.aizen.controller;

import com.aizen.dao.ResumeDAO;
import com.aizen.exception.DatabaseException;
import com.aizen.model.Resume;
import com.aizen.model.User;
import com.aizen.service.ResumeService;
import com.aizen.thread.TaskManager;
import com.aizen.util.SceneManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for {@code dashboard.fxml} - the Command Center landing
 * screen. Loads the current user's resumes on a background {@link Task}
 * to populate the stat cards and a simple recent-activity feed, then
 * hands off navigation to {@link MainLayoutController} for the quick
 * action tiles.
 */
public class DashboardController {

    @FXML private Label totalResumesLabel;
    @FXML private Label totalWordsLabel;
    @FXML private Label avgAtsLabel;
    @FXML private ListView<String> recentActivityList;

    private final ResumeService resumeService = new ResumeService(new ResumeDAO());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a");

    @FXML
    private void initialize() {
        loadStatsAsync();
    }

    private void loadStatsAsync() {
        User user = SceneManager.getCurrentUser();
        if (user == null) return;

        Task<List<Resume>> task = new Task<>() {
            @Override
            protected List<Resume> call() throws DatabaseException {
                return resumeService.findAllForUser(user.getId());
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<Resume> resumes = task.getValue();
            totalResumesLabel.setText(String.valueOf(resumes.size()));
            int totalExperience = resumes.stream().mapToInt(r -> r.getExperiences().size()).sum();
            totalWordsLabel.setText(String.valueOf(totalExperience));
            if (!resumes.isEmpty()) {
                avgAtsLabel.setText(String.valueOf(resumes.get(0).getSkills().size()));
            }

            recentActivityList.getItems().clear();
            resumes.stream().limit(8).forEach(r ->
                    recentActivityList.getItems().add(
                            "📄 " + r.getTitle() + "  —  updated " + r.getUpdatedAt().format(FORMATTER)));
            if (resumes.isEmpty()) {
                recentActivityList.getItems().add("No resumes yet — create your first one from Quick Actions above.");
            }
        }));

        task.setOnFailed(e -> Platform.runLater(() ->
                recentActivityList.getItems().add("Could not load recent activity.")));

        TaskManager.submit(task);
    }

    @FXML
    private void handleNewResume() {
        MainLayoutController.getInstance().navigateTo("resume_builder");
    }

    @FXML
    private void handleNewCoverLetter() {
        MainLayoutController.getInstance().navigateTo("cover_letter");
    }

    @FXML
    private void handleViewSaved() {
        MainLayoutController.getInstance().navigateTo("saved_resumes");
    }
}

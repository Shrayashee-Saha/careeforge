package com.aizen.controller;

import com.aizen.dao.ResumeDAO;
import com.aizen.model.Resume;
import com.aizen.model.User;
import com.aizen.service.PdfService;
import com.aizen.service.ResumeService;
import com.aizen.thread.PdfTask;
import com.aizen.thread.TaskManager;
import com.aizen.util.SceneManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Controller for {@code saved_resumes.fxml}. Lists every resume owned by
 * the current user in a TableView with View / Edit / Duplicate / Export /
 * Delete actions. Deleting always asks for confirmation first, and every
 * DB or PDF operation runs on a background {@link Task}.
 */
public class SavedResumesController {

    @FXML private TableView<Resume> resumeTable;
    @FXML private ProgressIndicator progressIndicator;

    private final ResumeService resumeService = new ResumeService(new ResumeDAO());
    private final PdfService pdfService = new PdfService();

    @FXML
    private void initialize() {
        loadResumesAsync();
    }

    private void loadResumesAsync() {
        User user = SceneManager.getCurrentUser();
        if (user == null) return;

        setLoading(true);
        Task<List<Resume>> task = new Task<>() {
            @Override
            protected List<Resume> call() throws Exception {
                return resumeService.findAllForUser(user.getId());
            }
        };
        task.setOnSucceeded(e -> {
            setLoading(false);
            resumeTable.getItems().setAll(task.getValue());
        });
        task.setOnFailed(e -> {
            setLoading(false);
            error("Failed to load resumes: " + task.getException().getMessage());
        });
        TaskManager.submit(task);
    }

    private Resume selected() {
        return resumeTable.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void handleView() {
        Resume resume = selected();
        if (resume == null) return;

        SceneManager.LoadResult<PreviewController> result = SceneManager.loadFragment("preview.fxml");
        result.controller().setResume(resume);

        Stage stage = new Stage();
        stage.setTitle("Preview — " + resume.getTitle());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new javafx.scene.Scene(result.root(), 650, 750));
        SceneManager.applyTheme(stage.getScene());
        stage.showAndWait();
    }

    @FXML
    private void handleEdit() {
        Resume resume = selected();
        if (resume == null) return;
        ResumeBuilderController.setPendingResume(resume);
        MainLayoutController.getInstance().navigateTo("resume_builder");
    }

    @FXML
    private void handleDuplicate() {
        Resume resume = selected();
        if (resume == null) return;

        setLoading(true);
        Task<Resume> task = new Task<>() {
            @Override
            protected Resume call() throws Exception {
                return resumeService.duplicate(resume);
            }
        };
        task.setOnSucceeded(e -> {
            setLoading(false);
            loadResumesAsync();
        });
        task.setOnFailed(e -> {
            setLoading(false);
            error("Failed to duplicate resume: " + task.getException().getMessage());
        });
        TaskManager.submit(task);
    }

    @FXML
    private void handleExportPdf() {
        Resume resume = selected();
        if (resume == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Resume as PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        chooser.setInitialFileName(resume.getTitle().replaceAll("[^a-zA-Z0-9-_ ]", "").replace(' ', '_') + ".pdf");
        File file = chooser.showSaveDialog(resumeTable.getScene().getWindow());
        if (file == null) return;

        setLoading(true);
        PdfTask task = new PdfTask(resume, file, pdfService);
        task.setOnSucceeded(e -> {
            setLoading(false);
            info("PDF exported to " + file.getAbsolutePath());
        });
        task.setOnFailed(e -> {
            setLoading(false);
            error("Export failed: " + task.getException().getMessage());
        });
        TaskManager.submit(task);
    }

    @FXML
    private void handleDelete() {
        Resume resume = selected();
        if (resume == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + resume.getTitle() + "\"? This cannot be undone.");
        confirm.setHeaderText("Confirm Deletion");
        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isEmpty() || choice.get() != ButtonType.OK) return;

        setLoading(true);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                resumeService.delete(resume.getId());
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            setLoading(false);
            resumeTable.getItems().remove(resume);
        });
        task.setOnFailed(e -> {
            setLoading(false);
            error("Failed to delete resume: " + task.getException().getMessage());
        });
        TaskManager.submit(task);
    }

    private void setLoading(boolean loading) {
        Platform.runLater(() -> {
            progressIndicator.setVisible(loading);
            progressIndicator.setManaged(loading);
        });
    }

    private void info(String message) {
        Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, message).showAndWait());
    }

    private void error(String message) {
        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, message).showAndWait());
    }
}

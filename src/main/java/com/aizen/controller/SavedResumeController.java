package com.aizen.controller;

import com.aizen.model.Resume;
import com.aizen.service.PdfService;
import com.aizen.service.ResumeService;
import com.aizen.thread.PdfTask;
import com.aizen.thread.SaveTask;
import com.aizen.thread.TaskManager;
import com.aizen.util.SceneManager;
import com.aizen.util.SceneManager.View;
import com.aizen.util.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

/** Table of the user's saved resumes with view / edit / export / delete actions. */
public class SavedResumeController {
    @FXML private TableView<Resume> table;
    @FXML private TableColumn<Resume, String> colTitle;
    @FXML private TableColumn<Resume, String> colName;
    @FXML private TableColumn<Resume, String> colJob;
    @FXML private TableColumn<Resume, String> colTemplate;
    @FXML private TableColumn<Resume, String> colUpdated;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progress;

    private final ObservableList<Resume> items = FXCollections.observableArrayList();
    private final ResumeService resumeService = new ResumeService(new com.aizen.dao.ResumeDAO());
    private final PdfService pdfService = new PdfService();

    @FXML
    private void initialize() {
        colTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        colJob.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getJobTitle()));
        colTemplate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTemplate()));
        colUpdated.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUpdatedAtString()));
        table.setItems(items);
        table.setPlaceholder(new Label("No saved resumes yet."));
        table.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Resume> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    onView();
                }
            });
            return row;
        });
        reload();
    }

    private void reload() {
        final int userId = Session.getCurrentUser().getId();
        SaveTask<List<Resume>> task = new SaveTask<>("Loading resumes...", () -> resumeService.findAllForUser(userId));
        TaskManager.run(task, progress, list -> {
            items.setAll(list);
            statusLabel.setText(list.size() + (list.size() == 1 ? " resume" : " resumes") + " saved.");
        }, ex -> statusLabel.setText("Could not load resumes: " + TaskManager.messageOf(ex)));
    }

    private Resume selected() {
        Resume r = table.getSelectionModel().getSelectedItem();
        if (r == null) {
            statusLabel.setText("Please select a resume in the table first.");
        }
        return r;
    }

    @FXML
    private void onView() {
        Resume r = selected();
        if (r != null) {
            Session.setResumeToPreview(r);
            MainController.getInstance().navigate(View.PREVIEW);
        }
    }

    @FXML
    private void onEdit() {
        Resume r = selected();
        if (r != null) {
            Session.setResumeToEdit(r);
            MainController.getInstance().navigate(View.RESUME);
        }
    }

    @FXML
    private void onExportPdf() {
        final Resume r = selected();
        if (r == null) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export resume as PDF");
        chooser.setInitialFileName(r.getTitle().replaceAll("[^A-Za-z0-9_-]+", "_") + ".pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
        final File file = chooser.showSaveDialog(SceneManager.getPrimaryStage());
        if (file == null) {
            return;
        }
        statusLabel.setText("Rendering PDF...");
        PdfTask task = new PdfTask(r, file, pdfService);
        TaskManager.run(task, progress,
            f -> statusLabel.setText("PDF exported to " + f.getAbsolutePath()),
            ex -> statusLabel.setText("Export failed: " + TaskManager.messageOf(ex)));
    }

    @FXML
    private void onDelete() {
        final Resume r = selected();
        if (r == null) {
            return;
        }
        if (!SceneManager.confirm("Delete resume", "Delete \"" + r.getTitle() + "\"? This cannot be undone.")) {
            return;
        }
        SaveTask<Void> task = new SaveTask<>("Deleting...", () -> { resumeService.delete(r.getId()); return null; });
        TaskManager.run(task, progress, ignored -> {
            items.remove(r);
            statusLabel.setText("Deleted \"" + r.getTitle() + "\".");
        }, ex -> statusLabel.setText("Delete failed: " + TaskManager.messageOf(ex)));
    }
}

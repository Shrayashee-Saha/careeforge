package com.aizen.controller;

import com.aizen.service.ApiService;
import com.aizen.service.CoverLetterService;
import com.aizen.service.PdfService;
import com.aizen.thread.TaskManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Controller for {@code cover_letter.fxml}. Collects role/company/tone
 * inputs, dispatches generation to {@link CoverLetterService} (which
 * itself defers to {@link ApiService}'s Gemini-or-local-fallback logic)
 * on a background {@link Task}, and offers clipboard copy + PDF export
 * for the result.
 */
public class CoverLetterController {

    @FXML private TextField applicantNameField;
    @FXML private TextField jobRoleField;
    @FXML private TextField companyField;
    @FXML private TextArea jobDescriptionArea;
    @FXML private TextArea applicantSummaryArea;
    @FXML private ComboBox<CoverLetterService.Tone> toneComboBox;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private TextArea letterArea;

    private final ApiService apiService = new ApiService();
    private final CoverLetterService coverLetterService = new CoverLetterService(apiService);
    private final PdfService pdfService = new PdfService();

    @FXML
    private void initialize() {
        toneComboBox.getItems().setAll(CoverLetterService.Tone.values());
        toneComboBox.setValue(CoverLetterService.Tone.EXECUTIVE_PROFESSIONAL);
    }

    @FXML
    private void handleGenerate() {
        String name = applicantNameField.getText();
        String role = jobRoleField.getText();
        String company = companyField.getText();
        String description = jobDescriptionArea.getText();
        String summary = applicantSummaryArea.getText();
        CoverLetterService.Tone tone = toneComboBox.getValue();

        if (role == null || role.isBlank() || company == null || company.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Please enter at least a job role and company name.").showAndWait();
            return;
        }

        setLoading(true);
        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return coverLetterService.generateFullLetter(name, role, company, description, tone, summary);
            }
        };

        task.setOnSucceeded(e -> {
            setLoading(false);
            letterArea.setText(task.getValue());
        });
        task.setOnFailed(e -> {
            setLoading(false);
            new Alert(Alert.AlertType.ERROR, "Generation failed: " + task.getException().getMessage()).showAndWait();
        });

        TaskManager.submit(task);
    }

    @FXML
    private void handleCopyToClipboard() {
        if (letterArea.getText().isBlank()) return;
        ClipboardContent content = new ClipboardContent();
        content.putString(letterArea.getText());
        Clipboard.getSystemClipboard().setContent(content);
        new Alert(Alert.AlertType.INFORMATION, "Cover letter copied to clipboard.").showAndWait();
    }

    @FXML
    private void handleExportPdf() {
        if (letterArea.getText().isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Generate a cover letter first.").showAndWait();
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Cover Letter as PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        chooser.setInitialFileName("cover_letter.pdf");
        File file = chooser.showSaveDialog(letterArea.getScene().getWindow());
        if (file == null) return;

        setLoading(true);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                pdfService.exportCoverLetter(letterArea.getText(), file);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            setLoading(false);
            new Alert(Alert.AlertType.INFORMATION, "PDF exported to " + file.getAbsolutePath()).showAndWait();
        });
        task.setOnFailed(e -> {
            setLoading(false);
            new Alert(Alert.AlertType.ERROR, "Export failed: " + task.getException().getMessage()).showAndWait();
        });
        TaskManager.submit(task);
    }

    private void setLoading(boolean loading) {
        Platform.runLater(() -> {
            progressIndicator.setVisible(loading);
            progressIndicator.setManaged(loading);
        });
    }
}

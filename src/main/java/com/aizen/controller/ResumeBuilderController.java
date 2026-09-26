package com.aizen.controller;

import com.aizen.dao.ResumeDAO;
import com.aizen.model.*;
import com.aizen.service.ApiService;
import com.aizen.service.AtsService;
import com.aizen.service.JsonService;
import com.aizen.service.PdfService;
import com.aizen.service.ResumeService;
import com.aizen.thread.ApiTask;
import com.aizen.thread.AtsTask;
import com.aizen.thread.PdfTask;
import com.aizen.thread.SaveTask;
import com.aizen.thread.TaskManager;
import com.aizen.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Controller for {@code resume_builder.fxml} - the split-pane resume
 * editor. Left side holds a tabbed form with dynamic add/edit/remove rows
 * per section (Experience, Education, Projects, Certifications, Skills);
 * right side renders a live plain-text preview of the whole resume.
 *
 * Every operation that touches disk, the database, the network, or PDF
 * rendering is dispatched to a background {@code Task} (via TaskManager /
 * the SaveTask / PdfTask / ApiTask / AtsTask wrapper classes) so the FX
 * Application Thread never blocks.
 */
public class ResumeBuilderController {

    /** Set by another controller (e.g. SavedResumesController) before navigating here to edit an existing resume. */
    private static Resume pendingResume;

    public static void setPendingResume(Resume resume) {
        pendingResume = resume;
    }

    @FXML private TextField resumeTitleField;
    @FXML private ComboBox<Resume.TemplateStyle> templateComboBox;
    @FXML private ProgressIndicator progressIndicator;

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField locationField;
    @FXML private TextField linkedinField;
    @FXML private TextArea summaryArea;

    @FXML private TableView<Experience> experienceTable;
    @FXML private TableView<Education> educationTable;
    @FXML private TableView<Project> projectTable;
    @FXML private TableView<Certification> certificationTable;
    @FXML private TableView<Skill> skillTable;

    @FXML private TextArea previewArea;

    private final ResumeService resumeService = new ResumeService(new ResumeDAO());
    private final ApiService apiService = new ApiService();
    private final PdfService pdfService = new PdfService();
    private final JsonService jsonService = new JsonService();
    private final AtsService atsService = new AtsService();

    private Resume resume;

    @FXML
    private void initialize() {
        templateComboBox.getItems().setAll(Resume.TemplateStyle.values());

        if (pendingResume != null) {
            this.resume = pendingResume;
            pendingResume = null;
        } else {
            User user = SceneManager.getCurrentUser();
            this.resume = resumeService.createBlankResume(user != null ? user.getId() : 1, "Untitled Resume");
        }

        bindFieldsFromResume();

        experienceTable.setItems(resume.getExperiences());
        educationTable.setItems(resume.getEducationEntries());
        projectTable.setItems(resume.getProjects());
        certificationTable.setItems(resume.getCertifications());
        skillTable.setItems(resume.getSkills());

        // Live preview refreshes automatically whenever any collection changes.
        resume.getExperiences().addListener((javafx.collections.ListChangeListener<Experience>) c -> refreshPreview());
        resume.getEducationEntries().addListener((javafx.collections.ListChangeListener<Education>) c -> refreshPreview());
        resume.getProjects().addListener((javafx.collections.ListChangeListener<Project>) c -> refreshPreview());
        resume.getCertifications().addListener((javafx.collections.ListChangeListener<Certification>) c -> refreshPreview());
        resume.getSkills().addListener((javafx.collections.ListChangeListener<Skill>) c -> refreshPreview());

        refreshPreview();
    }

    private void bindFieldsFromResume() {
        resumeTitleField.setText(resume.getTitle());
        templateComboBox.setValue(resume.getTemplateStyle());
        fullNameField.setText(resume.getFullName());
        emailField.setText(resume.getEmail());
        phoneField.setText(resume.getPhone());
        locationField.setText(resume.getLocation());
        linkedinField.setText(resume.getLinkedin());
        summaryArea.setText(resume.getSummary());
    }

    private void pushFieldsIntoResume() {
        resume.setTitle(resumeTitleField.getText().isBlank() ? "Untitled Resume" : resumeTitleField.getText());
        resume.setTemplateStyle(templateComboBox.getValue());
        resume.setFullName(fullNameField.getText());
        resume.setEmail(emailField.getText());
        resume.setPhone(phoneField.getText());
        resume.setLocation(locationField.getText());
        resume.setLinkedin(linkedinField.getText());
        resume.setSummary(summaryArea.getText());
    }

    @FXML
    private void refreshPreview() {
        StringBuilder sb = new StringBuilder();
        sb.append(orDash(fullNameField.getText())).append('\n');
        sb.append(String.join("  |  ", nonBlank(emailField.getText()), nonBlank(phoneField.getText()),
                nonBlank(locationField.getText()), nonBlank(linkedinField.getText()))).append("\n\n");

        if (!summaryArea.getText().isBlank()) {
            sb.append("SUMMARY\n").append(summaryArea.getText()).append("\n\n");
        }

        if (!resume.getExperiences().isEmpty()) {
            sb.append("EXPERIENCE\n");
            for (Experience e : resume.getExperiences()) {
                sb.append(e.getTitle()).append(" — ").append(e.getCompany()).append('\n');
                sb.append(e.getStartDate()).append(" – ").append(e.isCurrent() ? "Present" : e.getEndDate()).append('\n');
                if (!e.getDescription().isBlank()) sb.append(e.getDescription()).append('\n');
                sb.append('\n');
            }
        }

        if (!resume.getEducationEntries().isEmpty()) {
            sb.append("EDUCATION\n");
            for (Education ed : resume.getEducationEntries()) {
                sb.append(ed.getDegree()).append(", ").append(ed.getFieldOfStudy()).append('\n');
                sb.append(ed.getInstitution()).append(" (").append(ed.getStartDate()).append(" – ").append(ed.getEndDate()).append(")\n\n");
            }
        }

        if (!resume.getProjects().isEmpty()) {
            sb.append("PROJECTS\n");
            for (Project p : resume.getProjects()) {
                sb.append(p.getName()).append(" (").append(p.getTechStackAsCsv()).append(")\n");
                if (!p.getDescription().isBlank()) sb.append(p.getDescription()).append('\n');
                sb.append('\n');
            }
        }

        if (!resume.getCertifications().isEmpty()) {
            sb.append("CERTIFICATIONS\n");
            for (Certification c : resume.getCertifications()) {
                sb.append(c.getName()).append(" — ").append(c.getIssuer()).append('\n');
            }
            sb.append('\n');
        }

        if (!resume.getSkills().isEmpty()) {
            sb.append("SKILLS\n");
            sb.append(resume.getSkills().stream()
                    .map(s -> s.getName() + " (" + s.getProficiency() + ")")
                    .reduce((a, b) -> a + "  •  " + b).orElse(""));
        }

        previewArea.setText(sb.toString());
    }

    private String nonBlank(String s) { return s == null ? "" : s; }
    private String orDash(String s) { return (s == null || s.isBlank()) ? "Your Name" : s; }

    // ---------------------------------------------------------------
    // Save / Export / Import / ATS
    // ---------------------------------------------------------------

    @FXML
    private void handleSave() {
        pushFieldsIntoResume();
        setLoading(true);
        SaveTask<Resume> task = new SaveTask<>("Saving resume...", () -> resumeService.save(resume));
        TaskManager.run(task, progressIndicator, saved -> {
            setLoading(false);
            this.resume = saved;
            info("Resume saved successfully.");
        }, ex -> {
            setLoading(false);
            error("Failed to save resume: " + describeError(ex));
        });
    }

    @FXML
    private void handleExportPdf() {
        pushFieldsIntoResume();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Resume as PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        chooser.setInitialFileName(safeFileName(resume.getTitle()) + ".pdf");
        File file = chooser.showSaveDialog(resumeTitleField.getScene().getWindow());
        if (file == null) return;

        setLoading(true);
        PdfTask task = new PdfTask(resume, file, pdfService);
        task.setOnSucceeded(e -> {
            setLoading(false);
            info("PDF exported to " + file.getAbsolutePath());
        });
        task.setOnFailed(e -> {
            setLoading(false);
            error("Failed to export PDF: " + describeError(task.getException()));
        });
        TaskManager.submit(task);
    }

    @FXML
    private void handleExportJson() {
        pushFieldsIntoResume();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Resume as JSON");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        chooser.setInitialFileName(safeFileName(resume.getTitle()) + ".json");
        File file = chooser.showSaveDialog(resumeTitleField.getScene().getWindow());
        if (file == null) return;

        try {
            jsonService.exportResume(resume, file);
            info("JSON exported to " + file.getAbsolutePath());
        } catch (Exception e) {
            error("Failed to export JSON: " + e.getMessage());
        }
    }

    @FXML
    private void handleImportJson() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Resume JSON");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = chooser.showOpenDialog(resumeTitleField.getScene().getWindow());
        if (file == null) return;

        try {
            Resume imported = jsonService.importResume(file);
            imported.setUserId(resume.getUserId());
            this.resume = imported;
            bindFieldsFromResume();
            experienceTable.setItems(resume.getExperiences());
            educationTable.setItems(resume.getEducationEntries());
            projectTable.setItems(resume.getProjects());
            certificationTable.setItems(resume.getCertifications());
            skillTable.setItems(resume.getSkills());
            refreshPreview();
            info("Resume imported from " + file.getName());
        } catch (Exception e) {
            error("Failed to import JSON: " + e.getMessage());
        }
    }

    @FXML
    private void handleAtsCheck() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("ATS Keyword Check");
        dialog.setHeaderText("Paste target keywords, comma-separated (e.g. from a job description)");
        dialog.setContentText("Keywords:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) return;

        List<String> keywords = Arrays.stream(result.get().split(",")).map(String::trim).toList();
        pushFieldsIntoResume();

        setLoading(true);
        AtsTask task = new AtsTask(resume, keywords, atsService);
        task.setOnSucceeded(e -> {
            setLoading(false);
            AtsService.AtsResult r = task.getValue();
            StringBuilder msg = new StringBuilder();
            msg.append("ATS Match Score: ").append(r.scorePercent()).append("%\n\n");
            msg.append("Matched: ").append(String.join(", ", r.matchedKeywords())).append("\n");
            msg.append("Missing: ").append(String.join(", ", r.missingKeywords())).append("\n\n");
            msg.append("Tips:\n");
            r.tips().forEach(t -> msg.append("• ").append(t).append('\n'));
            Alert alert = new Alert(Alert.AlertType.INFORMATION, msg.toString());
            alert.setHeaderText("ATS Analysis Results");
            alert.setResizable(true);
            alert.showAndWait();
        });
        task.setOnFailed(e -> {
            setLoading(false);
            error("ATS check failed: " + describeError(task.getException()));
        });
        TaskManager.submit(task);
    }

    @FXML
    private void handleAiSummary() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("AI Summary Assist");
        dialog.setHeaderText("Enter target job title, years of experience, and key skills");
        dialog.setContentText("e.g. Software Engineer | 2 | Java, Spring, SQL");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) return;

        String[] parts = result.get().split("\\|");
        String jobTitle = parts.length > 0 ? parts[0].trim() : "professional";
        String years = parts.length > 1 ? parts[1].trim() : "2";
        String skills = parts.length > 2 ? parts[2].trim() : "relevant technical skills";

        setLoading(true);
        ApiTask<String> task = new ApiTask<>(apiService, svc -> svc.generateSummary(jobTitle, years, skills));
        TaskManager.run(task, progressIndicator, summary -> {
            setLoading(false);
            summaryArea.setText(summary);
            refreshPreview();
        }, ex -> {
            setLoading(false);
            error("AI generation failed: " + describeError(ex));
        });
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

    private String describeError(Throwable t) {
        return t == null ? "Unknown error." : t.getMessage();
    }

    private String safeFileName(String title) {
        return title.replaceAll("[^a-zA-Z0-9-_ ]", "").trim().replace(' ', '_');
    }

    // ---------------------------------------------------------------
    // Experience row dialogs
    // ---------------------------------------------------------------

    @FXML
    private void handleAddExperience() {
        showExperienceDialog(null);
    }

    @FXML
    private void handleEditExperience() {
        Experience selected = experienceTable.getSelectionModel().getSelectedItem();
        if (selected != null) showExperienceDialog(selected);
    }

    @FXML
    private void handleRemoveExperience() {
        Experience selected = experienceTable.getSelectionModel().getSelectedItem();
        if (selected != null) resume.getExperiences().remove(selected);
    }

    private void showExperienceDialog(Experience existing) {
        Dialog<Experience> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Experience" : "Edit Experience");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField company = new TextField(existing != null ? existing.getCompany() : "");
        TextField title = new TextField(existing != null ? existing.getTitle() : "");
        TextField location = new TextField(existing != null ? existing.getLocation() : "");
        TextField startDate = new TextField(existing != null ? existing.getStartDate() : "");
        TextField endDate = new TextField(existing != null ? existing.getEndDate() : "");
        CheckBox current = new CheckBox("Current position");
        current.setSelected(existing != null && existing.isCurrent());
        TextArea description = new TextArea(existing != null ? existing.getDescription() : "");
        description.setPromptText("One bullet point per line");
        description.setPrefRowCount(4);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(16));
        int r = 0;
        grid.addRow(r++, new Label("Company"), company);
        grid.addRow(r++, new Label("Title"), title);
        grid.addRow(r++, new Label("Location"), location);
        grid.addRow(r++, new Label("Start Date"), startDate);
        grid.addRow(r++, new Label("End Date"), endDate);
        grid.addRow(r++, new Label(""), current);
        grid.addRow(r++, new Label("Description"), description);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            try {
                if (existing != null) {
                    existing.setCompany(company.getText());
                    existing.setTitle(title.getText());
                    existing.setLocation(location.getText());
                    existing.setStartDate(startDate.getText());
                    existing.setCurrent(current.isSelected());
                    existing.setEndDate(current.isSelected() ? "" : endDate.getText());
                    existing.setDescription(description.getText());
                    return existing;
                }
                return new Experience(company.getText(), title.getText(), location.getText(),
                        startDate.getText(), endDate.getText(), current.isSelected(), description.getText());
            } catch (Exception ex) {
                error(ex.getMessage());
                return null;
            }
        });

        Optional<Experience> result = dialog.showAndWait();
        result.ifPresent(exp -> {
            if (existing == null) resume.getExperiences().add(exp);
            experienceTable.refresh();
            refreshPreview();
        });
    }

    // ---------------------------------------------------------------
    // Education row dialogs
    // ---------------------------------------------------------------

    @FXML
    private void handleAddEducation() {
        showEducationDialog(null);
    }

    @FXML
    private void handleEditEducation() {
        Education selected = educationTable.getSelectionModel().getSelectedItem();
        if (selected != null) showEducationDialog(selected);
    }

    @FXML
    private void handleRemoveEducation() {
        Education selected = educationTable.getSelectionModel().getSelectedItem();
        if (selected != null) resume.getEducationEntries().remove(selected);
    }

    private void showEducationDialog(Education existing) {
        Dialog<Education> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Education" : "Edit Education");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField institution = new TextField(existing != null ? existing.getInstitution() : "");
        TextField degree = new TextField(existing != null ? existing.getDegree() : "");
        TextField field = new TextField(existing != null ? existing.getFieldOfStudy() : "");
        TextField startDate = new TextField(existing != null ? existing.getStartDate() : "");
        TextField endDate = new TextField(existing != null ? existing.getEndDate() : "");
        TextField gpa = new TextField(existing != null ? String.valueOf(existing.getGpa()) : "0.0");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(16));
        int r = 0;
        grid.addRow(r++, new Label("Institution"), institution);
        grid.addRow(r++, new Label("Degree"), degree);
        grid.addRow(r++, new Label("Field of Study"), field);
        grid.addRow(r++, new Label("Start Date"), startDate);
        grid.addRow(r++, new Label("End Date"), endDate);
        grid.addRow(r++, new Label("GPA"), gpa);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            try {
                double gpaVal = gpa.getText().isBlank() ? 0.0 : Double.parseDouble(gpa.getText());
                if (existing != null) {
                    existing.setInstitution(institution.getText());
                    existing.setDegree(degree.getText());
                    existing.setFieldOfStudy(field.getText());
                    existing.setStartDate(startDate.getText());
                    existing.setEndDate(endDate.getText());
                    existing.setGpa(gpaVal);
                    return existing;
                }
                return new Education(institution.getText(), degree.getText(), field.getText(),
                        startDate.getText(), endDate.getText(), gpaVal);
            } catch (NumberFormatException nfe) {
                error("GPA must be a number.");
                return null;
            } catch (Exception ex) {
                error(ex.getMessage());
                return null;
            }
        });

        Optional<Education> result = dialog.showAndWait();
        result.ifPresent(ed -> {
            if (existing == null) resume.getEducationEntries().add(ed);
            educationTable.refresh();
            refreshPreview();
        });
    }

    // ---------------------------------------------------------------
    // Project row dialogs
    // ---------------------------------------------------------------

    @FXML
    private void handleAddProject() {
        showProjectDialog(null);
    }

    @FXML
    private void handleEditProject() {
        Project selected = projectTable.getSelectionModel().getSelectedItem();
        if (selected != null) showProjectDialog(selected);
    }

    @FXML
    private void handleRemoveProject() {
        Project selected = projectTable.getSelectionModel().getSelectedItem();
        if (selected != null) resume.getProjects().remove(selected);
    }

    private void showProjectDialog(Project existing) {
        Dialog<Project> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Project" : "Edit Project");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField name = new TextField(existing != null ? existing.getName() : "");
        TextArea description = new TextArea(existing != null ? existing.getDescription() : "");
        description.setPrefRowCount(3);
        TextField tech = new TextField(existing != null ? existing.getTechStackAsCsv() : "");
        tech.setPromptText("Comma-separated, e.g. Java, JavaFX, SQLite");
        TextField link = new TextField(existing != null ? existing.getLink() : "");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(16));
        int r = 0;
        grid.addRow(r++, new Label("Name"), name);
        grid.addRow(r++, new Label("Description"), description);
        grid.addRow(r++, new Label("Tech Stack"), tech);
        grid.addRow(r++, new Label("Link"), link);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            try {
                if (existing != null) {
                    existing.setName(name.getText());
                    existing.setDescription(description.getText());
                    existing.setTechStackFromCsv(tech.getText());
                    existing.setLink(link.getText());
                    return existing;
                }
                Project p = new Project(name.getText(), description.getText(), List.of(), link.getText());
                p.setTechStackFromCsv(tech.getText());
                return p;
            } catch (Exception ex) {
                error(ex.getMessage());
                return null;
            }
        });

        Optional<Project> result = dialog.showAndWait();
        result.ifPresent(p -> {
            if (existing == null) resume.getProjects().add(p);
            projectTable.refresh();
            refreshPreview();
        });
    }

    // ---------------------------------------------------------------
    // Certification row dialogs
    // ---------------------------------------------------------------

    @FXML
    private void handleAddCertification() {
        showCertificationDialog(null);
    }

    @FXML
    private void handleEditCertification() {
        Certification selected = certificationTable.getSelectionModel().getSelectedItem();
        if (selected != null) showCertificationDialog(selected);
    }

    @FXML
    private void handleRemoveCertification() {
        Certification selected = certificationTable.getSelectionModel().getSelectedItem();
        if (selected != null) resume.getCertifications().remove(selected);
    }

    private void showCertificationDialog(Certification existing) {
        Dialog<Certification> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Certification" : "Edit Certification");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField name = new TextField(existing != null ? existing.getName() : "");
        TextField issuer = new TextField(existing != null ? existing.getIssuer() : "");
        TextField dateIssued = new TextField(existing != null ? existing.getDateIssued() : "");
        TextField credentialId = new TextField(existing != null ? existing.getCredentialId() : "");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(16));
        int r = 0;
        grid.addRow(r++, new Label("Name"), name);
        grid.addRow(r++, new Label("Issuer"), issuer);
        grid.addRow(r++, new Label("Date Issued"), dateIssued);
        grid.addRow(r++, new Label("Credential ID"), credentialId);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            try {
                if (existing != null) {
                    existing.setName(name.getText());
                    existing.setIssuer(issuer.getText());
                    existing.setDateIssued(dateIssued.getText());
                    existing.setCredentialId(credentialId.getText());
                    return existing;
                }
                return new Certification(name.getText(), issuer.getText(), dateIssued.getText(), credentialId.getText());
            } catch (Exception ex) {
                error(ex.getMessage());
                return null;
            }
        });

        Optional<Certification> result = dialog.showAndWait();
        result.ifPresent(c -> {
            if (existing == null) resume.getCertifications().add(c);
            certificationTable.refresh();
            refreshPreview();
        });
    }

    // ---------------------------------------------------------------
    // Skill row dialog
    // ---------------------------------------------------------------

    @FXML
    private void handleAddSkill() {
        Dialog<Skill> dialog = new Dialog<>();
        dialog.setTitle("Add Skill");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField name = new TextField();
        ComboBox<Skill.Proficiency> proficiency = new ComboBox<>();
        proficiency.getItems().setAll(Skill.Proficiency.values());
        proficiency.setValue(Skill.Proficiency.INTERMEDIATE);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(16));
        grid.addRow(0, new Label("Skill Name"), name);
        grid.addRow(1, new Label("Proficiency"), proficiency);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            try {
                return new Skill(name.getText(), proficiency.getValue());
            } catch (Exception ex) {
                error(ex.getMessage());
                return null;
            }
        });

        Optional<Skill> result = dialog.showAndWait();
        result.ifPresent(s -> {
            resume.getSkills().add(s);
            refreshPreview();
        });
    }

    @FXML
    private void handleRemoveSkill() {
        Skill selected = skillTable.getSelectionModel().getSelectedItem();
        if (selected != null) resume.getSkills().remove(selected);
    }
}

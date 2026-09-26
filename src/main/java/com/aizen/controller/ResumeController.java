package com.aizen.controller;

import com.aizen.exception.ValidationException;
import com.aizen.model.Certification;
import com.aizen.model.Education;
import com.aizen.model.Experience;
import com.aizen.model.Project;
import com.aizen.model.Resume;
import com.aizen.model.Skill;
import com.aizen.service.GenerationResult;
import com.aizen.service.JsonService;
import com.aizen.service.PdfService;
import com.aizen.service.ResumeService;
import com.aizen.thread.ApiTask;
import com.aizen.thread.PdfTask;
import com.aizen.thread.SaveTask;
import com.aizen.thread.TaskManager;
import com.aizen.ui.DynamicSection;
import com.aizen.ui.DynamicSection.FieldSpec;
import com.aizen.util.SceneManager;
import com.aizen.util.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Dynamic resume builder with live preview, DB saving, PDF export and JSON import/export. */
public class ResumeController {
    @FXML private VBox formBox;
    @FXML private TextField tfTitle;
    @FXML private ChoiceBox<String> cbTemplate;
    @FXML private TextField tfFullName;
    @FXML private TextField tfEmail;
    @FXML private TextField tfPhone;
    @FXML private TextField tfJobTitle;
    @FXML private TextField tfLocation;
    @FXML private TextField tfLinkedin;
    @FXML private TextArea taSummary;
    @FXML private TextArea previewArea;
    @FXML private Button btnAi;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progress;

    private final ResumeService resumeService = new ResumeService(null);
    private final JsonService jsonService = new JsonService();
    private final PdfService pdfService = new PdfService();

    private DynamicSection expSection;
    private DynamicSection eduSection;
    private DynamicSection projSection;
    private DynamicSection certSection;
    private DynamicSection skillSection;

    /** The resume currently being edited (null for a brand-new one). */
    private Resume current;
    private boolean loading;

    @FXML
    private void initialize() {
        cbTemplate.setItems(FXCollections.observableArrayList("Classic", "Modern"));
        cbTemplate.setValue("Classic");

        Runnable refresh = this::updatePreview;
        expSection = new DynamicSection("Experience", "+ Add experience", refresh,
                FieldSpec.text("Company"), FieldSpec.text("Role"), FieldSpec.text("Start (e.g. Jan 2022)"),
                FieldSpec.text("End (or Present)"), FieldSpec.area("Description / achievements (one per line)"));
        eduSection = new DynamicSection("Education", "+ Add education", refresh,
                FieldSpec.text("Institution"), FieldSpec.text("Degree"), FieldSpec.text("Year"),
                FieldSpec.text("Grade / CGPA"));
        projSection = new DynamicSection("Projects", "+ Add project", refresh,
                FieldSpec.text("Project name"), FieldSpec.text("Link"), FieldSpec.area("Description"));
        certSection = new DynamicSection("Certifications", "+ Add certification", refresh,
                FieldSpec.text("Certification"), FieldSpec.text("Issuer"), FieldSpec.text("Year"));
        skillSection = new DynamicSection("Skills", "+ Add skill", refresh, FieldSpec.text("Skill"));
        formBox.getChildren().addAll(expSection, eduSection, projSection, certSection, skillSection);

        for (TextInputControl field : List.of(tfTitle, tfFullName, tfEmail, tfPhone, tfJobTitle,
                tfLocation, tfLinkedin, taSummary)) {
            field.textProperty().addListener((obs, o, n) -> updatePreview());
        }
        cbTemplate.valueProperty().addListener((obs, o, n) -> updatePreview());

        Resume toEdit = Session.getResumeToEdit();
        if (toEdit != null) {
            Session.setResumeToEdit(null);
            current = toEdit;
            populate(toEdit);
            setStatus("Editing \"" + toEdit.getTitle() + "\".", false);
        } else {
            startNew();
        }
    }

    // --------------------------------------------------------- form <-> model

    private void startNew() {
        current = null;
        Resume blank = new Resume();
        blank.setFullName(Session.getCurrentUser().getFullName());
        blank.setEmail(Session.getCurrentUser().getEmail());
        populate(blank);
        expSection.addRow();
        eduSection.addRow();
        skillSection.addRow();
        updatePreview();
        setStatus("New resume - fill in the form; the preview updates as you type.", false);
    }

    private static String text(TextInputControl c) {
        return c.getText() == null ? "" : c.getText().trim();
    }

    private static String at(String[] v, int i) {
        return i < v.length ? v[i] : "";
    }

    private Resume readForm() {
        Resume r = new Resume();
        if (current != null) {
            r.setId(current.getId());
            r.setUserId(current.getUserId());
            r.setCreatedAt(current.getCreatedAt());
        }
        r.setTitle(text(tfTitle));
        String tpl = cbTemplate.getValue();
        r.setTemplateStyle("Modern".equalsIgnoreCase(tpl) ? Resume.TemplateStyle.MODERN_TECH : Resume.TemplateStyle.CLASSIC_EXECUTIVE);
        r.setFullName(text(tfFullName));
        r.setEmail(text(tfEmail));
        r.setPhone(text(tfPhone));
        r.setJobTitle(text(tfJobTitle));
        r.setLocation(text(tfLocation));
        r.setLinkedin(text(tfLinkedin));
        r.setSummary(text(taSummary));
        for (String[] v : expSection.getValues()) {
            String company = at(v, 0);
            String role = at(v, 1);
            String start = at(v, 2);
            String end = at(v, 3);
            String desc = at(v, 4);
            boolean currentFlag = "Present".equalsIgnoreCase(end) || end.isBlank();
            r.getExperiences().add(new Experience(company, role, "", start, currentFlag ? "" : end, currentFlag, desc));
        }
        for (String[] v : eduSection.getValues()) {
            String inst = at(v, 0);
            String degree = at(v, 1);
            String year = at(v, 2);
            String grade = at(v, 3);
            double gpa = 0.0;
            try { gpa = Double.parseDouble(grade); } catch (NumberFormatException ignored) {}
            r.getEducationEntries().add(new Education(inst, degree, "", year, "", gpa));
        }
        for (String[] v : projSection.getValues()) {
            r.getProjects().add(new Project(at(v, 0), at(v, 1), null, at(v, 2)));
        }
        for (String[] v : certSection.getValues()) {
            r.getCertifications().add(new Certification(at(v, 0), at(v, 1), at(v, 2), null));
        }
        for (String[] v : skillSection.getValues()) {
            r.getSkills().add(new Skill(at(v, 0), com.aizen.model.Skill.Proficiency.INTERMEDIATE));
        }
        return r;
    }

    private void populate(Resume r) {
        loading = true;
        try {
            tfTitle.setText(r.getTitle());
            cbTemplate.setValue("Modern".equalsIgnoreCase(r.getTemplateStyle() == null ? "" : r.getTemplateStyle().name()) ? "Modern" : "Classic");
            tfFullName.setText(r.getFullName());
            tfEmail.setText(r.getEmail());
            tfPhone.setText(r.getPhone());
            tfJobTitle.setText(r.getJobTitle());
            tfLocation.setText(r.getLocation());
            tfLinkedin.setText(r.getLinkedin());
            taSummary.setText(r.getSummary());

            List<String[]> rows = new ArrayList<>();
            for (Experience e : r.getExperiences()) {
                rows.add(new String[]{e.getCompany(), e.getTitle(), e.getStartDate(), e.getEndDate(), e.getDescription()});
            }
            expSection.setValues(rows);

            rows = new ArrayList<>();
            for (Education e : r.getEducationEntries()) {
                rows.add(new String[]{e.getInstitution(), e.getDegree(), e.getStartDate(), String.valueOf(e.getGpa())});
            }
            eduSection.setValues(rows);

            rows = new ArrayList<>();
            for (Project p : r.getProjects()) {
                rows.add(new String[]{p.getName(), p.getLink(), p.getDescription()});
            }
            projSection.setValues(rows);

            rows = new ArrayList<>();
            for (Certification c : r.getCertifications()) {
                rows.add(new String[]{c.getName(), c.getIssuer(), c.getDateIssued()});
            }
            certSection.setValues(rows);

            rows = new ArrayList<>();
            for (Skill s : r.getSkills()) {
                rows.add(new String[]{s.getName()});
            }
            skillSection.setValues(rows);
        } finally {
            loading = false;
        }
        updatePreview();
    }

    private void updatePreview() {
        if (loading) {
            return;
        }
        previewArea.setText(resumeService.buildPreview(readForm()));
    }

    private void setStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("status-error", "status-ok");
        statusLabel.getStyleClass().add(error ? "status-error" : "status-ok");
    }

    private void fail(Throwable ex) {
        setStatus("Error: " + TaskManager.messageOf(ex), true);
    }

    // ----------------------------------------------------------------- actions

    @FXML
    private void onClear() {
        if (SceneManager.confirm("Start a new resume", "Discard the current form and start a new resume?")) {
            startNew();
        }
    }

    @FXML
    private void onSave() {
        final Resume r = readForm();
        try {
            resumeService.validate(r);
        } catch (ValidationException e) {
            setStatus(e.getMessage(), true);
            return;
        }
        SaveTask<Resume> task = new SaveTask<>("Saving resume...", () -> resumeService.save(r));
        setStatus("Saving...", false);
        TaskManager.run(task, progress, saved -> {
            current = saved;
            setStatus("Saved \"" + saved.getTitle() + "\" successfully.", false);
        }, this::fail);
    }

    @FXML
    private void onExportPdf() {
        final Resume r = readForm();
        if (r.getFullName().isBlank()) {
            setStatus("Enter your full name before exporting a PDF.", true);
            return;
        }
        File file = chooseFile("Export resume as PDF", "resume.pdf", true, "PDF files", "*.pdf");
        if (file == null) {
            return;
        }
        PdfTask task = new PdfTask(r, file, pdfService);
        setStatus("Rendering PDF...", false);
        TaskManager.run(task, progress, f -> setStatus("PDF exported to " + f.getAbsolutePath(), false), this::fail);
    }

    @FXML
    private void onExportJson() {
        final Resume r = readForm();
        File file = chooseFile("Export resume as JSON", "resume.json", true, "JSON files", "*.json");
        if (file == null) {
            return;
        }
        SaveTask<Void> task = new SaveTask<>("Writing JSON...", () -> { jsonService.exportResume(r, file); return null; });
        TaskManager.run(task, progress, ignored -> setStatus("JSON exported to " + file.getAbsolutePath(), false), this::fail);
    }

    @FXML
    private void onImportJson() {
        File file = chooseFile("Import resume from JSON", null, false, "JSON files", "*.json");
        if (file == null) {
            return;
        }
        SaveTask<Resume> task = new SaveTask<>("Reading JSON...", () -> jsonService.importResume(file));
        TaskManager.run(task, progress, imported -> {
            current = null; // imported data becomes a new resume when saved
            populate(imported);
            setStatus("Imported \"" + file.getName() + "\" - press Save to store it.", false);
        }, ex -> setStatus("Could not import the file: " + TaskManager.messageOf(ex), true));
    }

    @FXML
    private void onAiSummary() {
        final Resume r = readForm();
        if (r.getJobTitle().isBlank()) {
            setStatus("Enter a job title first so the AI knows what to write about.", true);
            return;
        }
        setStatus("Generating summary...", false);
        btnAi.setDisable(true);
        ApiTask<GenerationResult> task = new ApiTask<>("Asking AI...", () -> resumeService.generateSummary(r));
        TaskManager.run(task, progress, result -> {
            btnAi.setDisable(false);
            taSummary.setText(result.text());
            setStatus(result.note(), false);
        }, ex -> {
            btnAi.setDisable(false);
            fail(ex);
        });
    }

    private File chooseFile(String title, String initialName, boolean save, String desc, String pattern) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(desc, pattern));
        if (initialName != null) {
            chooser.setInitialFileName(initialName);
        }
        return save ? chooser.showSaveDialog(SceneManager.getPrimaryStage()) : chooser.showOpenDialog(SceneManager.getPrimaryStage());
    }
}
package com.aizen.controller;

import com.aizen.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * Controller for {@code preview.fxml} - a read-only, full-text rendering
 * of a single {@link Resume}, shown in a modal dialog from
 * {@link SavedResumesController}'s "View" action.
 */
public class PreviewController {

    @FXML private TextArea previewArea;

    public void setResume(Resume resume) {
        StringBuilder sb = new StringBuilder();
        sb.append(resume.getFullName().isBlank() ? "Your Name" : resume.getFullName()).append('\n');
        sb.append(String.join("  |  ", resume.getEmail(), resume.getPhone(), resume.getLocation(), resume.getLinkedin())).append("\n\n");

        if (!resume.getSummary().isBlank()) {
            sb.append("SUMMARY\n").append(resume.getSummary()).append("\n\n");
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

    @FXML
    private void handleClose() {
        ((Stage) previewArea.getScene().getWindow()).close();
    }
}

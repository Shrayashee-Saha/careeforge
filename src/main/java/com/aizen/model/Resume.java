package com.aizen.model;

import com.aizen.exception.ValidationException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDateTime;

/**
 * The aggregate root for a saved resume: personal info snapshot plus the
 * collections of {@link Experience}, {@link Education}, {@link Project},
 * {@link Certification} and {@link Skill} entries that make it up.
 *
 * Uses JavaFX {@link ObservableList}s so the Resume Builder's TableViews /
 * ListViews can bind directly to these collections and update live as the
 * user adds or removes rows.
 */
public class Resume {



    /** Visual template chosen for PDF/preview rendering. */
    public enum TemplateStyle {
        CLASSIC_EXECUTIVE, MODERN_TECH, MINIMALIST;

        TemplateStyle() {
        }
    }

    public Resume(LocalDateTime updatedAt, LocalDateTime createdAt, String summary, String linkedin, String location, String phone, String email, String fullName, TemplateStyle templateStyle, String title, int userId, int id) {
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
        this.summary = summary;
        this.linkedin = linkedin;
        this.location = location;
        this.phone = phone;
        this.email = email;
        this.fullName = fullName;
        this.templateStyle = templateStyle;
        this.title = title;
        this.userId = userId;
        this.id = id;
    }

    private int id;
    private int userId;
    private String title;
    private TemplateStyle templateStyle;
    private String jobTitle;

    private String fullName;
    private String email;
    private String phone;
    private String location;
    private String linkedin;
    private String summary;

    private final ObservableList<Experience> experiences = FXCollections.observableArrayList();
    private final ObservableList<Education> educationEntries = FXCollections.observableArrayList();
    private final ObservableList<Project> projects = FXCollections.observableArrayList();
    private final ObservableList<Certification> certifications = FXCollections.observableArrayList();
    private final ObservableList<Skill> skills = FXCollections.observableArrayList();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Resume(int userId, String title) {
        setUserId(userId);
        setTitle(title);
        this.jobTitle = "";
        this.templateStyle = TemplateStyle.CLASSIC_EXECUTIVE;
        this.fullName = "";
        this.email = "";
        this.phone = "";
        this.location = "";
        this.linkedin = "";
        this.summary = "";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Resume() {
        this.templateStyle = TemplateStyle.CLASSIC_EXECUTIVE;
        this.jobTitle = "";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle == null ? "" : jobTitle.trim(); }

    /** Human-friendly template name for UI tables. */
    public String getTemplate() {
        if (templateStyle == null) return "Classic";
        return switch (templateStyle) {
            case CLASSIC_EXECUTIVE -> "Classic";
            case MODERN_TECH -> "Modern";
            case MINIMALIST -> "Minimalist";
        };
    }

    public String getUpdatedAtString() { return updatedAt == null ? "" : updatedAt.toString(); }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) {
        if (userId <= 0) {
            throw new ValidationException("Resume must belong to a valid user.");
        }
        this.userId = userId;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new ValidationException("Resume title cannot be empty.");
        }
        this.title = title.trim();
    }

    public TemplateStyle getTemplateStyle() { return templateStyle; }
    public void setTemplateStyle(TemplateStyle templateStyle) {
        this.templateStyle = templateStyle == null ? TemplateStyle.CLASSIC_EXECUTIVE : templateStyle;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName == null ? "" : fullName.trim(); }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email == null ? "" : email.trim(); }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone == null ? "" : phone.trim(); }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location == null ? "" : location.trim(); }

    public String getLinkedin() { return linkedin; }
    public void setLinkedin(String linkedin) { this.linkedin = linkedin == null ? "" : linkedin.trim(); }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary == null ? "" : summary.trim(); }

    public ObservableList<Experience> getExperiences() { return experiences; }
    public ObservableList<Education> getEducationEntries() { return educationEntries; }
    public ObservableList<Project> getProjects() { return projects; }
    public ObservableList<Certification> getCertifications() { return certifications; }
    public ObservableList<Skill> getSkills() { return skills; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    /** Flattens all resume text into one blob for keyword scanning by AtsService. */
    public String toSearchableText() {
        StringBuilder sb = new StringBuilder();
        sb.append(summary).append(' ');
        experiences.forEach(e -> sb.append(e.getTitle()).append(' ').append(e.getDescription()).append(' '));
        projects.forEach(p -> sb.append(p.getName()).append(' ').append(p.getDescription()).append(' ').append(p.getTechStackAsCsv()).append(' '));
        skills.forEach(s -> sb.append(s.getName()).append(' '));
        certifications.forEach(c -> sb.append(c.getName()).append(' '));
        return sb.toString().toLowerCase();
    }

    @Override
    public String toString() {
        return title + " (" + fullName + ")";
    }
}

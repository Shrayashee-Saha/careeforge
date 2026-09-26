package com.aizen.model;

import com.aizen.exception.ValidationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** A personal/academic project entry inside a {@link Resume}. */
public class Project {

    private int id;
    private int resumeId;
    private String name;
    private String description;
    private List<String> techStack;
    private String link;

    public Project(String name, String description, List<String> techStack, String link) {
        setName(name);
        setDescription(description);
        setTechStack(techStack);
        setLink(link);
    }

    public Project() {
        this.techStack = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getResumeId() { return resumeId; }
    public void setResumeId(int resumeId) { this.resumeId = resumeId; }

    public String getName() { return name; }

    public Project(String name) {
        this.name = name;
    }

    public Project(int id, int resumeId, String name, String description, List<String> techStack, String link) {
        this.id = id;
        this.resumeId = resumeId;
        this.name = name;
        this.description = description;
        this.techStack = techStack;
        this.link = link;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Project name cannot be empty.");
        }
        this.name = name.trim();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description == null ? "" : description.trim(); }

    public List<String> getTechStack() { return techStack; }
    public void setTechStack(List<String> techStack) {
        this.techStack = techStack == null ? new ArrayList<>() : new ArrayList<>(techStack);
    }

    /** Convenience setter used by the FXML form, which collects tech stack as a comma-separated string. */
    public void setTechStackFromCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            this.techStack = new ArrayList<>();
            return;
        }
        this.techStack = new ArrayList<>(Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList());
    }

    public String getTechStackAsCsv() {
        return String.join(", ", techStack);
    }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link == null ? "" : link.trim(); }

    @Override
    public String toString() {
        return name + " (" + getTechStackAsCsv() + ")";
    }
}

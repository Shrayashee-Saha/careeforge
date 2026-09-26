package com.aizen.model;

import com.aizen.exception.ValidationException;

/** A single skill tag with a proficiency level, inside a {@link Resume}. */
public class Skill {

    /** Bounded proficiency scale used both for display and the ATS scorer. */
    public enum Proficiency {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }

    public Skill(Proficiency proficiency, String name, int resumeId, int id) {
        this.proficiency = proficiency;
        this.name = name;
        this.resumeId = resumeId;
        this.id = id;
    }

    private int id;
    private int resumeId;
    private String name;
    private Proficiency proficiency;

    public Skill(String name, Proficiency proficiency) {
        setName(name);
        this.proficiency = proficiency == null ? Proficiency.INTERMEDIATE : proficiency;
    }

    public Skill() {
        this.proficiency = Proficiency.INTERMEDIATE;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getResumeId() { return resumeId; }
    public void setResumeId(int resumeId) { this.resumeId = resumeId; }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Skill name cannot be empty.");
        }
        this.name = name.trim();
    }

    public Proficiency getProficiency() { return proficiency; }
    public void setProficiency(Proficiency proficiency) {
        this.proficiency = proficiency == null ? Proficiency.INTERMEDIATE : proficiency;
    }

    @Override
    public String toString() {
        return name + " (" + proficiency + ")";
    }
}

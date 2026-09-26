package com.aizen.model;

import com.aizen.exception.ValidationException;

/**
 * A {@link Person} who is actively applying for jobs. Adds the fields a
 * resume/cover-letter builder actually needs on top of bare identity.
 *
 * Demonstrates: INHERITANCE (extends Person), METHOD OVERLOADING (three
 * {@code updateProfile} signatures below), and ENCAPSULATION.
 */
public class Applicant extends Person {

    private String jobTitle;
    private String location;

    public Applicant(String name, String email, String phone, String jobTitle, String location, String linkedin, String summary) {
        super(name, email, phone);
        this.jobTitle = jobTitle;
        this.location = location;
        this.linkedin = linkedin;
        this.summary = summary;
    }

    public Applicant(String jobTitle, String location, String linkedin, String summary) {
        this.jobTitle = jobTitle;
        this.location = location;
        this.linkedin = linkedin;
        this.summary = summary;
    }

    private String linkedin;
    private String summary;

    public Applicant(String name, String email, String phone, String jobTitle, String location) {
        super(name, email, phone);
        setJobTitle(jobTitle);
        setLocation(location);
        this.linkedin = "";
        this.summary = "";
    }

    public Applicant() {
        super();
    }

    @Override
    public String getRole() {
        return "Applicant";
    }

    // ---- Overload 1: update the core two fields only ----
    public void updateProfile(String jobTitle, String location) {
        setJobTitle(jobTitle);
        setLocation(location);
    }

    // ---- Overload 2: also update LinkedIn ----
    public void updateProfile(String jobTitle, String location, String linkedin) {
        updateProfile(jobTitle, location);
        setLinkedin(linkedin);
    }

    // ---- Overload 3: update everything including the summary blurb ----
    public void updateProfile(String jobTitle, String location, String linkedin, String summary) {
        updateProfile(jobTitle, location, linkedin);
        setSummary(summary);
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        if (jobTitle == null || jobTitle.isBlank()) {
            throw new ValidationException("Job title cannot be empty.");
        }
        this.jobTitle = jobTitle.trim();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location == null ? "" : location.trim();
    }

    public String getLinkedin() {
        return linkedin;
    }

    public void setLinkedin(String linkedin) {
        this.linkedin = linkedin == null ? "" : linkedin.trim();
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        if (summary != null && summary.length() > 2000) {
            throw new ValidationException("Summary cannot exceed 2000 characters.");
        }
        this.summary = summary == null ? "" : summary.trim();
    }
}

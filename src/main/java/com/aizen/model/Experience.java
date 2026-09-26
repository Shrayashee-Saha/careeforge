package com.aizen.model;

import com.aizen.exception.ValidationException;

/**
 * A single work-experience entry inside a {@link Resume}. Each Resume can
 * hold an arbitrary number of these (stored in an ObservableList in the UI,
 * a normal List when persisted through the DAO layer).
 */
public class Experience {

    private int id;
    private int resumeId;
    private String company;

    public Experience(String description, boolean current, String endDate, String startDate, String location, String title, String company, int resumeId, int id) {
        this.description = description;
        this.current = current;
        this.endDate = endDate;
        this.startDate = startDate;
        this.location = location;
        this.title = title;
        this.company = company;
        this.resumeId = resumeId;
        this.id = id;
    }

    private String title;
    private String location;
    private String startDate;   // stored as "MMM yyyy" free text, kept simple for the form
    private String endDate;     // empty when 'current' is true
    private boolean current;
    private String description; // newline-separated bullet points

    public Experience(String company, String title, String location,
                       String startDate, String endDate, boolean current, String description) {
        setCompany(company);
        setTitle(title);
        setLocation(location);
        setStartDate(startDate);
        this.current = current;
        setEndDate(current ? "" : endDate);
        setDescription(description);
    }

    public Experience() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getResumeId() { return resumeId; }
    public void setResumeId(int resumeId) { this.resumeId = resumeId; }

    public String getCompany() { return company; }
    public void setCompany(String company) {
        if (company == null || company.isBlank()) {
            throw new ValidationException("Company name cannot be empty.");
        }
        this.company = company.trim();
    }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new ValidationException("Experience title cannot be empty.");
        }
        this.title = title.trim();
    }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location == null ? "" : location.trim(); }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) {
        if (startDate == null || startDate.isBlank()) {
            throw new ValidationException("Start date cannot be empty.");
        }
        this.startDate = startDate.trim();
    }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate == null ? "" : endDate.trim(); }

    public boolean isCurrent() { return current; }
    public void setCurrent(boolean current) {
        this.current = current;
        if (current) this.endDate = "";
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description == null ? "" : description.trim(); }

    @Override
    public String toString() {
        return title + " at " + company + (current ? " (Present)" : " (" + endDate + ")");
    }
}

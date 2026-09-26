package com.aizen.model;

import com.aizen.exception.ValidationException;

/** A single education entry (degree/institution) inside a {@link Resume}. */
public class Education {

    private int id;

    public Education(int id, int resumeId, String institution, String degree, String fieldOfStudy, String startDate, String endDate, double gpa) {
        this.id = id;
        this.resumeId = resumeId;
        this.institution = institution;
        this.degree = degree;
        this.fieldOfStudy = fieldOfStudy;
        this.startDate = startDate;
        this.endDate = endDate;
        this.gpa = gpa;
    }

    private int resumeId;
    private String institution;
    private String degree;
    private String fieldOfStudy;
    private String startDate;
    private String endDate;
    private double gpa;

    public Education(String institution, String degree, String fieldOfStudy,
                      String startDate, String endDate, double gpa) {
        setInstitution(institution);
        setDegree(degree);
        setFieldOfStudy(fieldOfStudy);
        setStartDate(startDate);
        setEndDate(endDate);
        setGpa(gpa);
    }

    public Education() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getResumeId() { return resumeId; }
    public void setResumeId(int resumeId) { this.resumeId = resumeId; }

    public String getInstitution() { return institution; }
    public void setInstitution(String institution) {
        if (institution == null || institution.isBlank()) {
            throw new ValidationException("Institution name cannot be empty.");
        }
        this.institution = institution.trim();
    }

    public String getDegree() { return degree; }
    public void setDegree(String degree) {
        if (degree == null || degree.isBlank()) {
            throw new ValidationException("Degree cannot be empty.");
        }
        this.degree = degree.trim();
    }

    public String getFieldOfStudy() { return fieldOfStudy; }
    public void setFieldOfStudy(String fieldOfStudy) { this.fieldOfStudy = fieldOfStudy == null ? "" : fieldOfStudy.trim(); }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate == null ? "" : startDate.trim(); }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate == null ? "" : endDate.trim(); }

    public double getGpa() { return gpa; }
    public void setGpa(double gpa) {
        if (gpa < 0.0 || gpa > 4.0) {
            throw new ValidationException("GPA must be between 0.0 and 4.0.");
        }
        this.gpa = gpa;
    }

    @Override
    public String toString() {
        return degree + " in " + fieldOfStudy + ", " + institution;
    }
}

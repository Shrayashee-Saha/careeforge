package com.aizen.model;

import com.aizen.exception.ValidationException;

/** A professional certification entry inside a {@link Resume}. */
public class Certification {

    private int id;
    private int resumeId;
    private String name;
    private String issuer;

    public Certification(int id, int resumeId, String name, String issuer, String dateIssued, String credentialId) {
        this.id = id;
        this.resumeId = resumeId;
        this.name = name;
        this.issuer = issuer;
        this.dateIssued = dateIssued;
        this.credentialId = credentialId;
    }

    private String dateIssued;
    private String credentialId;

    public Certification(String name, String issuer, String dateIssued, String credentialId) {
        setName(name);
        setIssuer(issuer);
        setDateIssued(dateIssued);
        setCredentialId(credentialId);
    }

    public Certification() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getResumeId() { return resumeId; }
    public void setResumeId(int resumeId) { this.resumeId = resumeId; }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Certification name cannot be empty.");
        }
        this.name = name.trim();
    }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer == null ? "" : issuer.trim(); }

    public String getDateIssued() { return dateIssued; }
    public void setDateIssued(String dateIssued) { this.dateIssued = dateIssued == null ? "" : dateIssued.trim(); }

    public String getCredentialId() { return credentialId; }
    public void setCredentialId(String credentialId) { this.credentialId = credentialId == null ? "" : credentialId.trim(); }

    @Override
    public String toString() {
        return name + " - " + issuer;
    }
}

package com.aizen.service;

import com.aizen.dao.ResumeDAO;
import com.aizen.exception.DatabaseException;
import com.aizen.exception.ValidationException;
import com.aizen.model.*;

import java.util.List;

/**
 * Business-logic layer sitting between the controllers and {@link ResumeDAO}.
 * Controllers should never talk to the DAO directly - they go through this
 * service (or CoverLetterService) so persistence rules and derived
 * behaviour (like duplicating a resume) live in one place.
 */
public class ResumeService {

    private final ResumeDAO resumeDAO;

    public ResumeService(ResumeDAO resumeDAO) {
        this.resumeDAO = resumeDAO;
    }

    public Resume createBlankResume(int userId, String title) {
        return new Resume(userId, title);
    }

    public String buildPreview(Resume r) {
        StringBuilder sb = new StringBuilder();
        sb.append(r.getFullName()).append('\n');
        sb.append(String.join("  |  ", r.getEmail(), r.getPhone(), r.getLocation(), r.getLinkedin())).append("\n\n");
        if (!r.getSummary().isBlank()) sb.append("SUMMARY\n").append(r.getSummary()).append("\n\n");
        return sb.toString();
    }

    public GenerationResult generateSummary(Resume r) {
        String text = new ApiService().generateSummary(r.getJobTitle(), "0", "");
        return new GenerationResult(text, false, "Generated");
    }

    public void validate(Resume r) {
        // lightweight validation kept here for UI usage
        if (r.getTitle() == null || r.getTitle().isBlank()) throw new ValidationException("Resume title cannot be empty.");
    }

    public Resume save(Resume resume) throws DatabaseException {
        if (resume.getId() == 0) {
            return resumeDAO.save(resume);
        }
        resumeDAO.update(resume);
        return resume;
    }

    public List<Resume> findAllForUser(int userId) throws DatabaseException {
        return resumeDAO.findByUserId(userId);
    }

    public void delete(int resumeId) throws DatabaseException {
        resumeDAO.deleteById(resumeId);
    }

    /** Creates a deep-ish copy of a resume (new id, "(Copy)" suffix, all child entries duplicated) and saves it. */
    public Resume duplicate(Resume source) throws DatabaseException {
        Resume copy = new Resume(source.getUserId(), source.getTitle() + " (Copy)");
        copy.setTemplateStyle(source.getTemplateStyle());
        copy.setFullName(source.getFullName());
        copy.setEmail(source.getEmail());
        copy.setPhone(source.getPhone());
        copy.setLocation(source.getLocation());
        copy.setLinkedin(source.getLinkedin());
        copy.setSummary(source.getSummary());

        for (Experience e : source.getExperiences()) {
            copy.getExperiences().add(new Experience(e.getCompany(), e.getTitle(), e.getLocation(),
                    e.getStartDate(), e.getEndDate(), e.isCurrent(), e.getDescription()));
        }
        for (Education ed : source.getEducationEntries()) {
            copy.getEducationEntries().add(new Education(ed.getInstitution(), ed.getDegree(),
                    ed.getFieldOfStudy(), ed.getStartDate(), ed.getEndDate(), ed.getGpa()));
        }
        for (Project p : source.getProjects()) {
            copy.getProjects().add(new Project(p.getName(), p.getDescription(), p.getTechStack(), p.getLink()));
        }
        for (Certification c : source.getCertifications()) {
            copy.getCertifications().add(new Certification(c.getName(), c.getIssuer(), c.getDateIssued(), c.getCredentialId()));
        }
        for (Skill s : source.getSkills()) {
            copy.getSkills().add(new Skill(s.getName(), s.getProficiency()));
        }

        return resumeDAO.save(copy);
    }
}

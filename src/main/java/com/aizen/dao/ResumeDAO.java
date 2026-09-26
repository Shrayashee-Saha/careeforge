package com.aizen.dao;

import com.aizen.db.Database;
import com.aizen.exception.DatabaseException;
import com.aizen.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC PreparedStatement implementation of {@link GenericDAO} for the
 * {@code resumes} table and all of its child tables (experiences,
 * education, projects, certifications, skills). A Resume is persisted as
 * an aggregate: saving or updating one writes the parent row and replaces
 * every child row in the same logical operation, kept consistent through
 * SQLite's manual transaction control (autocommit is turned off for the
 * duration of the write, then committed or rolled back as a whole).
 */
public class ResumeDAO implements GenericDAO<Resume, Integer> {

    @Override
    public Resume save(Resume resume) throws DatabaseException {
        String sql = """
            INSERT INTO resumes (user_id, title, template_style, full_name, email, phone,
                                  location, linkedin, summary, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bindResumeFields(ps, resume);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        resume.setId(keys.getInt(1));
                    }
                }
            }
            writeChildren(conn, resume);
            conn.commit();
            return resume;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save resume.", e);
        }
    }

    @Override
    public void update(Resume resume) throws DatabaseException {
        String sql = """
            UPDATE resumes SET title = ?, template_style = ?, full_name = ?, email = ?, phone = ?,
                                location = ?, linkedin = ?, summary = ?, updated_at = ?
            WHERE id = ?
        """;
        resume.touch();
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, resume.getTitle());
                ps.setString(2, resume.getTemplateStyle().name());
                ps.setString(3, resume.getFullName());
                ps.setString(4, resume.getEmail());
                ps.setString(5, resume.getPhone());
                ps.setString(6, resume.getLocation());
                ps.setString(7, resume.getLinkedin());
                ps.setString(8, resume.getSummary());
                ps.setString(9, resume.getUpdatedAt().toString());
                ps.setInt(10, resume.getId());
                ps.executeUpdate();
            }
            deleteChildren(conn, resume.getId());
            writeChildren(conn, resume);
            conn.commit();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update resume.", e);
        }
    }

    @Override
    public Optional<Resume> findById(Integer id) throws DatabaseException {
        String sql = "SELECT * FROM resumes WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Resume resume = mapResumeRow(rs);
                loadChildren(conn, resume);
                return Optional.of(resume);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load resume.", e);
        }
    }

    public List<Resume> findByUserId(int userId) throws DatabaseException {
        String sql = "SELECT * FROM resumes WHERE user_id = ? ORDER BY updated_at DESC";
        List<Resume> results = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Resume resume = mapResumeRow(rs);
                    loadChildren(conn, resume);
                    results.add(resume);
                }
            }
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list resumes for user.", e);
        }
    }

    @Override
    public List<Resume> findAll() throws DatabaseException {
        String sql = "SELECT * FROM resumes ORDER BY updated_at DESC";
        List<Resume> results = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Resume resume = mapResumeRow(rs);
                loadChildren(conn, resume);
                results.add(resume);
            }
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list resumes.", e);
        }
    }

    @Override
    public void deleteById(Integer id) throws DatabaseException {
        String sql = "DELETE FROM resumes WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete resume.", e);
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private void bindResumeFields(PreparedStatement ps, Resume resume) throws SQLException {
        ps.setInt(1, resume.getUserId());
        ps.setString(2, resume.getTitle());
        ps.setString(3, resume.getTemplateStyle().name());
        ps.setString(4, resume.getJobTitle());
        ps.setString(5, resume.getFullName());
        ps.setString(6, resume.getEmail());
        ps.setString(7, resume.getPhone());
        ps.setString(8, resume.getLocation());
        ps.setString(9, resume.getLinkedin());
        ps.setString(10, resume.getSummary());
        ps.setString(11, resume.getCreatedAt().toString());
        ps.setString(12, resume.getUpdatedAt().toString());
    }

    private Resume mapResumeRow(ResultSet rs) throws SQLException {
        Resume resume = new Resume();
        resume.setId(rs.getInt("id"));
        resume.setUserId(rs.getInt("user_id"));
        resume.setTitle(rs.getString("title"));
        resume.setTemplateStyle(Resume.TemplateStyle.valueOf(rs.getString("template_style")));
        resume.setJobTitle(rs.getString("job_title"));
        resume.setFullName(rs.getString("full_name"));
        resume.setEmail(rs.getString("email"));
        resume.setPhone(rs.getString("phone"));
        resume.setLocation(rs.getString("location"));
        resume.setLinkedin(rs.getString("linkedin"));
        resume.setSummary(rs.getString("summary"));
        resume.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
        resume.setUpdatedAt(LocalDateTime.parse(rs.getString("updated_at")));
        return resume;
    }

    private void writeChildren(Connection conn, Resume resume) throws SQLException {
        int rid = resume.getId();

        String expSql = "INSERT INTO experiences (resume_id, company, title, location, start_date, end_date, is_current, description) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(expSql)) {
            for (Experience e : resume.getExperiences()) {
                ps.setInt(1, rid);
                ps.setString(2, e.getCompany());
                ps.setString(3, e.getTitle());
                ps.setString(4, e.getLocation());
                ps.setString(5, e.getStartDate());
                ps.setString(6, e.getEndDate());
                ps.setInt(7, e.isCurrent() ? 1 : 0);
                ps.setString(8, e.getDescription());
                ps.addBatch();
            }
            ps.executeBatch();
        }

        String eduSql = "INSERT INTO education (resume_id, institution, degree, field_of_study, start_date, end_date, gpa) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(eduSql)) {
            for (Education ed : resume.getEducationEntries()) {
                ps.setInt(1, rid);
                ps.setString(2, ed.getInstitution());
                ps.setString(3, ed.getDegree());
                ps.setString(4, ed.getFieldOfStudy());
                ps.setString(5, ed.getStartDate());
                ps.setString(6, ed.getEndDate());
                ps.setDouble(7, ed.getGpa());
                ps.addBatch();
            }
            ps.executeBatch();
        }

        String projSql = "INSERT INTO projects (resume_id, name, description, tech_stack, link) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(projSql)) {
            for (Project p : resume.getProjects()) {
                ps.setInt(1, rid);
                ps.setString(2, p.getName());
                ps.setString(3, p.getDescription());
                ps.setString(4, p.getTechStackAsCsv());
                ps.setString(5, p.getLink());
                ps.addBatch();
            }
            ps.executeBatch();
        }

        String certSql = "INSERT INTO certifications (resume_id, name, issuer, date_issued, credential_id) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(certSql)) {
            for (Certification c : resume.getCertifications()) {
                ps.setInt(1, rid);
                ps.setString(2, c.getName());
                ps.setString(3, c.getIssuer());
                ps.setString(4, c.getDateIssued());
                ps.setString(5, c.getCredentialId());
                ps.addBatch();
            }
            ps.executeBatch();
        }

        String skillSql = "INSERT INTO skills (resume_id, name, proficiency) VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(skillSql)) {
            for (Skill s : resume.getSkills()) {
                ps.setInt(1, rid);
                ps.setString(2, s.getName());
                ps.setString(3, s.getProficiency().name());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void deleteChildren(Connection conn, int resumeId) throws SQLException {
        String[] tables = {"experiences", "education", "projects", "certifications", "skills"};
        for (String table : tables) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + table + " WHERE resume_id = ?")) {
                ps.setInt(1, resumeId);
                ps.executeUpdate();
            }
        }
    }

    private void loadChildren(Connection conn, Resume resume) throws SQLException {
        int rid = resume.getId();

        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM experiences WHERE resume_id = ?")) {
            ps.setInt(1, rid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Experience e = new Experience();
                    e.setId(rs.getInt("id"));
                    e.setResumeId(rid);
                    e.setCompany(rs.getString("company"));
                    e.setTitle(rs.getString("title"));
                    e.setLocation(rs.getString("location"));
                    e.setStartDate(rs.getString("start_date"));
                    e.setCurrent(rs.getInt("is_current") == 1);
                    e.setEndDate(rs.getString("end_date"));
                    e.setDescription(rs.getString("description"));
                    resume.getExperiences().add(e);
                }
            }
        }

        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM education WHERE resume_id = ?")) {
            ps.setInt(1, rid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Education ed = new Education();
                    ed.setId(rs.getInt("id"));
                    ed.setResumeId(rid);
                    ed.setInstitution(rs.getString("institution"));
                    ed.setDegree(rs.getString("degree"));
                    ed.setFieldOfStudy(rs.getString("field_of_study"));
                    ed.setStartDate(rs.getString("start_date"));
                    ed.setEndDate(rs.getString("end_date"));
                    ed.setGpa(rs.getDouble("gpa"));
                    resume.getEducationEntries().add(ed);
                }
            }
        }

        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM projects WHERE resume_id = ?")) {
            ps.setInt(1, rid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Project p = new Project();
                    p.setId(rs.getInt("id"));
                    p.setResumeId(rid);
                    p.setName(rs.getString("name"));
                    p.setDescription(rs.getString("description"));
                    p.setTechStackFromCsv(rs.getString("tech_stack"));
                    p.setLink(rs.getString("link"));
                    resume.getProjects().add(p);
                }
            }
        }

        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM certifications WHERE resume_id = ?")) {
            ps.setInt(1, rid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Certification c = new Certification();
                    c.setId(rs.getInt("id"));
                    c.setResumeId(rid);
                    c.setName(rs.getString("name"));
                    c.setIssuer(rs.getString("issuer"));
                    c.setDateIssued(rs.getString("date_issued"));
                    c.setCredentialId(rs.getString("credential_id"));
                    resume.getCertifications().add(c);
                }
            }
        }

        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM skills WHERE resume_id = ?")) {
            ps.setInt(1, rid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Skill s = new Skill();
                    s.setId(rs.getInt("id"));
                    s.setResumeId(rid);
                    s.setName(rs.getString("name"));
                    s.setProficiency(Skill.Proficiency.valueOf(rs.getString("proficiency")));
                    resume.getSkills().add(s);
                }
            }
        }
    }
}

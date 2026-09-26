package com.aizen.model;

import com.aizen.exception.ValidationException;
import java.time.Year;

/**
 * An {@link Applicant} who is still enrolled in (or has just finished)
 * a degree program. Specializes the role and adds academic fields.
 *
 * Demonstrates: INHERITANCE (Person -> Applicant -> Student, a three-level
 * chain), OVERRIDING ({@link #getRole()} returns a different value than
 * its parent), and a further OVERLOAD of {@code updateProfile}.
 */
public class Student extends Applicant {

    private String university;
    private double cgpa;
    private int graduationYear;

    public Student(String name, String email, String phone, String jobTitle,
                    String location, String university, double cgpa, int graduationYear) {
        super(name, email, phone, jobTitle, location);
        setUniversity(university);
        setCgpa(cgpa);
        setGraduationYear(graduationYear);
    }

    public Student() {
        super();
    }

    @Override
    public String getRole() {
        return "Student";
    }

    /**
     * Overload #4 in the updateProfile family, only meaningful for a
     * Student: also refreshes the academic fields in one call.
     */
    public void updateProfile(String jobTitle, String location, String linkedin,
                               String summary, String university, double cgpa) {
        super.updateProfile(jobTitle, location, linkedin, summary);
        setUniversity(university);
        setCgpa(cgpa);
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        if (university == null || university.isBlank()) {
            throw new ValidationException("University cannot be empty.");
        }
        this.university = university.trim();
    }

    public double getCgpa() {
        return cgpa;
    }

    public void setCgpa(double cgpa) {
        if (cgpa < 0.0 || cgpa > 4.0) {
            throw new ValidationException("CGPA must be between 0.0 and 4.0.");
        }
        this.cgpa = cgpa;
    }

    public int getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(int graduationYear) {
        int currentYear = Year.now().getValue();
        if (graduationYear < currentYear - 10 || graduationYear > currentYear + 10) {
            throw new ValidationException("Graduation year looks invalid: " + graduationYear);
        }
        this.graduationYear = graduationYear;
    }
}

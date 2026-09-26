package com.aizen.service;

/**
 * Thin business-logic wrapper around {@link ApiService} for cover-letter
 * generation. Formats the AI/local output into a complete letter with a
 * salutation and sign-off, since ApiService only returns the body text.
 */
public class CoverLetterService {

    /** Tone options exposed to the Cover Letter Generator UI. */
    public enum Tone {
        EXECUTIVE_PROFESSIONAL("Executive Professional"),
        ENTHUSIASTIC_CONFIDENT("Enthusiastic Confident"),
        DIRECT_CONCISE("Direct & Concise");

        private final String displayName;

        Tone(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private final ApiService apiService;

    public CoverLetterService(ApiService apiService) {
        this.apiService = apiService;
    }

    public String generateFullLetter(String applicantName, String jobRole, String company,
                                      String jobDescription, Tone tone, String applicantSummary) {
        String body = apiService.generateCoverLetter(jobRole, company, jobDescription,
                tone.getDisplayName(), applicantSummary);

        StringBuilder letter = new StringBuilder();
        letter.append("Dear Hiring Manager,\n\n");
        letter.append(body).append("\n\n");
        letter.append("Sincerely,\n");
        letter.append(applicantName == null || applicantName.isBlank() ? "[Your Name]" : applicantName);
        return letter.toString();
    }
}

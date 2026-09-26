package com.aizen.service;

/**
 * Built-in heuristic template builder used whenever the Gemini API is
 * unreachable (missing {@code AIZEN_API_KEY}, network failure, or a
 * non-2xx response). Produces reasonable, immediately usable text so the
 * app never blocks the user's workflow on an external service.
 *
 * This is intentionally simple string-template logic rather than a real
 * language model - it exists purely as a graceful degradation path.
 */
final class LocalFallbackEngine {

    private LocalFallbackEngine() {
    }

    static String generateSummary(String jobTitle, String yearsExperience, String keySkills) {
        String title = blankToPlaceholder(jobTitle, "professional");
        String years = blankToPlaceholder(yearsExperience, "several");
        String skills = blankToPlaceholder(keySkills, "a strong technical and problem-solving skill set");

        return "Results-driven " + title + " with " + years + " years of experience delivering "
                + "high-quality outcomes through " + skills + ". Known for combining analytical "
                + "rigor with clear communication, and for adapting quickly to new tools, teams, "
                + "and problem domains. Seeking to bring a proactive, detail-oriented approach to "
                + "a role where impact and growth are equally valued.";
    }

    static String generateBulletPoints(String role, String company, String responsibilities) {
        String r = blankToPlaceholder(role, "team member");
        String c = blankToPlaceholder(company, "the organization");
        String base = blankToPlaceholder(responsibilities, "day-to-day project and technical responsibilities");

        StringBuilder sb = new StringBuilder();
        sb.append("• Contributed as ").append(r).append(" at ").append(c)
          .append(", owning ").append(base).append(" from planning through delivery.\n");
        sb.append("• Collaborated cross-functionally to identify improvement opportunities, "
                + "resulting in measurable gains in efficiency and quality.\n");
        sb.append("• Communicated progress and technical decisions clearly to both technical "
                + "and non-technical stakeholders.\n");
        sb.append("• Took initiative on process improvements that reduced turnaround time and "
                + "improved reliability of deliverables.");
        return sb.toString();
    }

    static String generateCoverLetter(String jobRole, String company, String jobDescription,
                                       String tone, String applicantSummary) {
        String role = blankToPlaceholder(jobRole, "this position");
        String comp = blankToPlaceholder(company, "your organization");
        String summary = blankToPlaceholder(applicantSummary, "a dependable, fast-learning background suited to this role");
        String opener;
        String closer;

        switch (tone == null ? "" : tone.toUpperCase()) {
            case "ENTHUSIASTIC CONFIDENT", "ENTHUSIASTIC_CONFIDENT" -> {
                opener = "I was genuinely excited to see the opening for " + role + " at " + comp + ", and I'd love the chance to bring my energy and skills to your team.";
                closer = "I'd welcome the opportunity to talk more about how I can contribute from day one - thank you for considering my application!";
            }
            case "DIRECT & CONCISE", "DIRECT_CONCISE" -> {
                opener = "I am applying for " + role + " at " + comp + ". Below is a brief summary of why I am a strong fit.";
                closer = "I am available to discuss further at your convenience. Thank you for your time.";
            }
            default -> {
                opener = "I am writing to express my interest in the " + role + " position at " + comp + ".";
                closer = "Thank you for your time and consideration; I look forward to the possibility of discussing this opportunity further.";
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(opener).append("\n\n");
        sb.append("With ").append(summary).append(", I am confident I can make a meaningful "
                + "contribution to your team. ");
        if (jobDescription != null && !jobDescription.isBlank()) {
            sb.append("The responsibilities outlined for this role align closely with my "
                    + "experience, and I am particularly drawn to the opportunity to apply my "
                    + "skills in a role that values both quality and initiative.\n\n");
        } else {
            sb.append("\n\n");
        }
        sb.append(closer);
        return sb.toString();
    }

    private static String blankToPlaceholder(String value, String placeholder) {
        return (value == null || value.isBlank()) ? placeholder : value.trim();
    }
}

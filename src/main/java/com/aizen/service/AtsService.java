package com.aizen.service;

import com.aizen.model.Resume;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Applicant Tracking System keyword scanner. Compares the resume's
 * flattened text against a target list of keywords (typically pulled
 * from a job description) and returns a percentage match score plus
 * actionable tips - which keywords are missing and general formatting
 * advice.
 */
public class AtsService {

    /** Immutable result of one ATS scan. */
    public record AtsResult(int scorePercent, List<String> matchedKeywords,
                             List<String> missingKeywords, List<String> tips) {
    }

    public AtsResult analyze(Resume resume, List<String> targetKeywords) {
        String haystack = resume.toSearchableText();
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String rawKeyword : targetKeywords) {
            String keyword = rawKeyword.trim();
            if (keyword.isEmpty()) continue;
            if (haystack.contains(keyword.toLowerCase(Locale.ROOT))) {
                matched.add(keyword);
            } else {
                missing.add(keyword);
            }
        }

        int total = matched.size() + missing.size();
        int score = total == 0 ? 0 : (int) Math.round((matched.size() * 100.0) / total);

        List<String> tips = new ArrayList<>();
        if (!missing.isEmpty()) {
            tips.add("Consider naturally incorporating these missing keywords: " + String.join(", ", missing) + ".");
        }
        if (resume.getSummary() == null || resume.getSummary().isBlank()) {
            tips.add("Add a professional summary - many ATS parsers weight the summary section heavily.");
        }
        if (resume.getSkills().isEmpty()) {
            tips.add("List explicit skill tags; ATS keyword scanners often check a dedicated skills section first.");
        }
        if (resume.getExperiences().stream().anyMatch(e -> e.getDescription() == null || e.getDescription().isBlank())) {
            tips.add("Every experience entry should include bullet points describing measurable impact.");
        }
        if (score >= 80) {
            tips.add("Strong keyword match - this resume is well aligned with the target role.");
        }

        return new AtsResult(score, matched, missing, tips);
    }
}

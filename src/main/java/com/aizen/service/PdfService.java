package com.aizen.service;

import com.aizen.exception.PdfGenerationException;
import com.aizen.model.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders a {@link Resume} (and standalone cover letters) to PDF using
 * Apache PDFBox, with light visual differences per
 * {@link Resume.TemplateStyle}. Runs entirely off the FX thread via
 * {@code com.aizen.thread.PdfTask}.
 */
public class PdfService {

    private static final float MARGIN = 50f;
    private static final float PAGE_WIDTH = PDRectangle.LETTER.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.LETTER.getHeight();
    private static final float USABLE_WIDTH = PAGE_WIDTH - 2 * MARGIN;

    public void exportResume(Resume resume, File destination) throws PdfGenerationException {
        try (PDDocument document = new PDDocument()) {
            Writer writer = new Writer(document, resume.getTemplateStyle());
            writer.newPage();

            writer.heading(resume.getFullName().isBlank() ? "Your Name" : resume.getFullName(), 20);
            writer.subheading(contactLine(resume));
            writer.gap(10);

            if (!resume.getSummary().isBlank()) {
                writer.sectionTitle("Summary");
                writer.bodyText(resume.getSummary());
                writer.gap(8);
            }

            if (!resume.getExperiences().isEmpty()) {
                writer.sectionTitle("Experience");
                for (Experience e : resume.getExperiences()) {
                    writer.entryTitle(e.getTitle() + " — " + e.getCompany());
                    String dateRange = e.getStartDate() + " – " + (e.isCurrent() ? "Present" : e.getEndDate());
                    writer.entrySubtitle((e.getLocation() == null || e.getLocation().isBlank() ? "" : e.getLocation() + " | ") + dateRange);
                    for (String line : e.getDescription().split("\\r?\\n")) {
                        if (!line.isBlank()) writer.bulletLine(stripBullet(line));
                    }
                    writer.gap(6);
                }
            }

            if (!resume.getEducationEntries().isEmpty()) {
                writer.sectionTitle("Education");
                for (Education ed : resume.getEducationEntries()) {
                    writer.entryTitle(ed.getDegree() + (ed.getFieldOfStudy().isBlank() ? "" : ", " + ed.getFieldOfStudy()));
                    String range = ed.getStartDate() + " – " + ed.getEndDate();
                    String gpaText = ed.getGpa() > 0 ? String.format(" | GPA: %.2f", ed.getGpa()) : "";
                    writer.entrySubtitle(ed.getInstitution() + " | " + range + gpaText);
                    writer.gap(6);
                }
            }

            if (!resume.getProjects().isEmpty()) {
                writer.sectionTitle("Projects");
                for (Project p : resume.getProjects()) {
                    writer.entryTitle(p.getName() + (p.getTechStackAsCsv().isBlank() ? "" : " (" + p.getTechStackAsCsv() + ")"));
                    if (!p.getDescription().isBlank()) writer.bodyText(p.getDescription());
                    if (!p.getLink().isBlank()) writer.bodyText(p.getLink());
                    writer.gap(6);
                }
            }

            if (!resume.getCertifications().isEmpty()) {
                writer.sectionTitle("Certifications");
                for (Certification c : resume.getCertifications()) {
                    writer.entryTitle(c.getName() + (c.getIssuer().isBlank() ? "" : " — " + c.getIssuer()));
                    if (!c.getDateIssued().isBlank()) writer.entrySubtitle(c.getDateIssued());
                    writer.gap(4);
                }
            }

            if (!resume.getSkills().isEmpty()) {
                writer.sectionTitle("Skills");
                StringBuilder sb = new StringBuilder();
                for (Skill s : resume.getSkills()) {
                    if (sb.length() > 0) sb.append("  •  ");
                    sb.append(s.getName()).append(" (").append(s.getProficiency()).append(")");
                }
                writer.bodyText(sb.toString());
            }

            writer.close();
            document.save(destination);
        } catch (IOException e) {
            throw new PdfGenerationException("Failed to render resume PDF.", e);
        }
    }

    public void exportCoverLetter(String letterText, File destination) throws PdfGenerationException {
        try (PDDocument document = new PDDocument()) {
            Writer writer = new Writer(document, Resume.TemplateStyle.CLASSIC_EXECUTIVE);
            writer.newPage();
            for (String paragraph : letterText.split("\\r?\\n\\r?\\n")) {
                writer.bodyText(paragraph.replace("\n", " "));
                writer.gap(10);
            }
            writer.close();
            document.save(destination);
        } catch (IOException e) {
            throw new PdfGenerationException("Failed to render cover letter PDF.", e);
        }
    }

    private String contactLine(Resume r) {
        List<String> parts = new ArrayList<>();
        if (!r.getEmail().isBlank()) parts.add(r.getEmail());
        if (!r.getPhone().isBlank()) parts.add(r.getPhone());
        if (!r.getLocation().isBlank()) parts.add(r.getLocation());
        if (!r.getLinkedin().isBlank()) parts.add(r.getLinkedin());
        return String.join("   |   ", parts);
    }

    private String stripBullet(String line) {
        String trimmed = line.trim();
        return trimmed.startsWith("•") ? trimmed.substring(1).trim() : trimmed;
    }

    /**
     * Small internal helper that owns the current PDPageContentStream and
     * cursor position, wraps text to the page width, and starts a new page
     * automatically when content would run off the bottom margin. Kept
     * private to PdfService since nothing outside PDF rendering needs it.
     */
    private static class Writer {
        private final PDDocument document;
        private final Resume.TemplateStyle style;
        private PDPage page;
        private PDPageContentStream stream;
        private float y;
        private final PDFont regularFont;
        private final PDFont boldFont;

        Writer(PDDocument document, Resume.TemplateStyle style) {
            this.document = document;
            this.style = style;
            this.regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            this.boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        }

        void newPage() throws IOException {
            if (stream != null) stream.close();
            page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            y = PAGE_HEIGHT - MARGIN;
        }

        private void ensureSpace(float needed) throws IOException {
            if (y - needed < MARGIN) {
                newPage();
            }
        }

        void heading(String text, float size) throws IOException {
            ensureSpace(size + 6);
            writeLine(text, boldFont, size, style == Resume.TemplateStyle.MODERN_TECH);
            y -= size + 6;
        }

        void subheading(String text) throws IOException {
            if (text.isBlank()) return;
            ensureSpace(14);
            writeLine(text, regularFont, 10, false);
            y -= 14;
        }

        void sectionTitle(String text) throws IOException {
            ensureSpace(20);
            y -= 6;
            writeLine(text.toUpperCase(), boldFont, 12, false);
            stream.setLineWidth(1f);
            stream.moveTo(MARGIN, y - 2);
            stream.lineTo(PAGE_WIDTH - MARGIN, y - 2);
            stream.stroke();
            y -= 14;
        }

        void entryTitle(String text) throws IOException {
            ensureSpace(14);
            writeLine(text, boldFont, 11, false);
            y -= 14;
        }

        void entrySubtitle(String text) throws IOException {
            if (text.isBlank()) return;
            ensureSpace(12);
            writeLine(text, regularFont, 9, false);
            y -= 12;
        }

        void bulletLine(String text) throws IOException {
            for (String wrapped : wrap("• " + text, regularFont, 10)) {
                ensureSpace(13);
                writeLine(wrapped, regularFont, 10, false);
                y -= 13;
            }
        }

        void bodyText(String text) throws IOException {
            for (String wrapped : wrap(text, regularFont, 10)) {
                ensureSpace(13);
                writeLine(wrapped, regularFont, 10, false);
                y -= 13;
            }
        }

        void gap(float amount) {
            y -= amount;
        }

        private void writeLine(String text, PDFont font, float size, boolean centered) throws IOException {
            float x = MARGIN;
            if (centered) {
                float width = font.getStringWidth(text) / 1000 * size;
                x = (PAGE_WIDTH - width) / 2;
            }
            stream.beginText();
            stream.setFont(font, size);
            stream.newLineAtOffset(x, y);
            stream.showText(sanitize(text));
            stream.endText();
        }

        private List<String> wrap(String text, PDFont font, float size) throws IOException {
            List<String> lines = new ArrayList<>();
            for (String rawLine : text.split("\\r?\\n")) {
                StringBuilder current = new StringBuilder();
                for (String word : rawLine.split(" ")) {
                    String candidate = current.isEmpty() ? word : current + " " + word;
                    float width = font.getStringWidth(sanitize(candidate)) / 1000 * size;
                    if (width > USABLE_WIDTH && !current.isEmpty()) {
                        lines.add(current.toString());
                        current = new StringBuilder(word);
                    } else {
                        current = new StringBuilder(candidate);
                    }
                }
                lines.add(current.toString());
            }
            return lines;
        }

        private String sanitize(String text) {
            // Standard14 fonts only support WinAnsi-encodable characters.
            return text.replaceAll("[^\\x00-\\xFF]", "?");
        }

        void close() throws IOException {
            if (stream != null) stream.close();
        }
    }
}

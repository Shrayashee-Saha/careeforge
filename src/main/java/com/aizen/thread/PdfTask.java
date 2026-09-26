package com.aizen.thread;

import com.aizen.exception.PdfGenerationException;
import com.aizen.model.Resume;
import com.aizen.service.PdfService;
import javafx.concurrent.Task;

import java.io.File;

/**
 * Background {@link Task} that renders a {@link Resume} to a PDF file via
 * {@link PdfService} / Apache PDFBox. PDF rendering touches the filesystem
 * and can take a noticeable moment for a long resume, so it always runs
 * off the JavaFX Application Thread.
 */
public class PdfTask extends Task<File> {

    private final Resume resume;
    private final File destination;
    private final PdfService pdfService;

    public PdfTask(Resume resume, File destination, PdfService pdfService) {
        this.resume = resume;
        this.destination = destination;
        this.pdfService = pdfService;
    }

    @Override
    protected File call() throws PdfGenerationException {
        updateMessage("Rendering PDF...");
        updateProgress(-1, 1);
        pdfService.exportResume(resume, destination);
        updateMessage("PDF exported.");
        updateProgress(1, 1);
        return destination;
    }
}

package com.aizen.thread;

import com.aizen.model.Resume;
import com.aizen.service.AtsService;
import javafx.concurrent.Task;

import java.util.List;

/**
 * Background {@link Task} that runs the {@link AtsService} keyword scan
 * against a resume and a target job description. Kept off the UI thread
 * even though the scan itself is CPU-light, so the pattern stays
 * consistent with the rest of the app's "no I/O or scanning on the FX
 * thread" rule and scales fine if the scoring logic grows heavier later.
 */
public class AtsTask extends Task<AtsService.AtsResult> {

    private final Resume resume;
    private final List<String> targetKeywords;
    private final AtsService atsService;

    public AtsTask(Resume resume, List<String> targetKeywords, AtsService atsService) {
        this.resume = resume;
        this.targetKeywords = targetKeywords;
        this.atsService = atsService;
    }

    @Override
    protected AtsService.AtsResult call() {
        updateMessage("Scanning resume against target keywords...");
        updateProgress(-1, 1);
        AtsService.AtsResult result = atsService.analyze(resume, targetKeywords);
        updateMessage("Scan complete.");
        updateProgress(1, 1);
        return result;
    }
}

package com.aizen.thread;

import javafx.concurrent.Task;

import java.util.concurrent.Callable;

/**
 * Generic background Task wrapper that executes any blocking operation
 * (provided as a {@link Callable}) off the FX thread and surfaces the
 * result via the Task API. Controllers use this for DB/network/file
 * operations without creating bespoke Task classes for every case.
 */
public class SaveTask<T> extends Task<T> {

    private final String title;
    private final Callable<T> operation;

    public SaveTask(String title, Callable<T> operation) {
        this.title = title;
        this.operation = operation;
    }

    @Override
    protected T call() throws Exception {
        updateMessage(title == null ? "Working..." : title);
        updateProgress(-1, 1);
        T result = operation.call();
        updateMessage("Done.");
        updateProgress(1, 1);
        return result;
    }
}

package com.aizen.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;

/**
 * Application-wide fixed thread pool (4 worker threads) that every
 * background {@link javafx.concurrent.Task} in AiZen is submitted to.
 * Centralizing the executor means we can shut it down cleanly on exit
 * instead of leaking daemon threads, and it keeps the JavaFX Application
 * Thread free of any blocking I/O (JDBC, HttpClient, PDFBox rendering).
 */
public final class TaskManager {

    private static final int POOL_SIZE = 4;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(POOL_SIZE, runnable -> {
        Thread t = new Thread(runnable, "aizen-worker");
        t.setDaemon(true);
        return t;
    });

    private TaskManager() {
    }

    /** Submits any Runnable (including a javafx.concurrent.Task, which implements Runnable) to the shared pool. */
    public static void submit(Runnable task) {
        EXECUTOR.submit(task);
    }

    /**
     * Run a JavaFX Task on the shared executor and wire up UI callbacks.
     */
    public static <T> void run(Task<T> task, ProgressIndicator progress, Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        if (progress != null) {
            progress.visibleProperty().bind(task.runningProperty());
        }
        task.setOnSucceeded(e -> {
            if (progress != null) progress.visibleProperty().unbind();
            if (onSuccess != null) onSuccess.accept(task.getValue());
        });
        task.setOnFailed(e -> {
            if (progress != null) progress.visibleProperty().unbind();
            if (onFailure != null) onFailure.accept(task.getException());
        });
        EXECUTOR.submit(task);
    }

    public static String messageOf(Throwable ex) {
        if (ex == null) return "Unknown error";
        Throwable root = ex;
        while (root.getCause() != null) root = root.getCause();
        String msg = root.getMessage();
        return msg == null ? root.toString() : msg;
    }

    /** Gracefully shuts the pool down, waiting briefly for in-flight work to finish. Call from Main.stop(). */
    public static void shutdown() {
        EXECUTOR.shutdown();
        try {
            if (!EXECUTOR.awaitTermination(3, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

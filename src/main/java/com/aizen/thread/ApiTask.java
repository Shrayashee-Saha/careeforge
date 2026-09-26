package com.aizen.thread;

import com.aizen.exception.ApiException;
import com.aizen.service.ApiService;
import javafx.concurrent.Task;

import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Generic background Task for API operations. Supports two construction
 * styles: supply an {@link ApiService} + {@link Function} or a simple
 * title + {@link Callable} for arbitrary operations.
 */
public class ApiTask<T> extends Task<T> {

    private final ApiService apiService;
    private final Function<ApiService, T> functionOp;
    private final Callable<T> callableOp;
    private final String title;

    public ApiTask(ApiService apiService, Function<ApiService, T> operation) {
        this.apiService = apiService;
        this.functionOp = operation;
        this.callableOp = null;
        this.title = "Contacting AI engine...";
    }

    public ApiTask(String title, Callable<T> operation) {
        this.apiService = null;
        this.functionOp = null;
        this.callableOp = operation;
        this.title = title;
    }

    @Override
    protected T call() throws ApiException {
        updateMessage(title == null ? "Working..." : title);
        updateProgress(-1, 1);
        try {
            T result;
            if (functionOp != null && apiService != null) {
                result = functionOp.apply(apiService);
            } else if (callableOp != null) {
                try {
                    result = callableOp.call();
                } catch (Exception e) {
                    throw new ApiException("API task failed: " + e.getMessage(), e);
                }
            } else {
                throw new ApiException("No operation supplied to ApiTask.");
            }
            updateMessage("Done.");
            updateProgress(1, 1);
            return result;
        } catch (ApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ApiException("API task failed: " + e.getMessage(), e);
        }
    }
}

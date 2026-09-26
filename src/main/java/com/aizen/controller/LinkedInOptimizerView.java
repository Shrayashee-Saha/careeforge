package com.aizen.controller;

import com.aizen.exception.ApiException;
import com.aizen.service.ApiService;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LinkedInOptimizerView extends VBox {

    private final ApiService apiService = new ApiService();
    private final ComboBox<String> toneCombo = new ComboBox<>();
    private final TextArea resumeContextArea = new TextArea();
    private final TextArea outputArea = new TextArea();
    private final ProgressIndicator progressIndicator = new ProgressIndicator();

    public LinkedInOptimizerView() {
        setSpacing(12);
        setPadding(new Insets(20));

        toneCombo.getItems().addAll(
                "Executive & Authoritative",
                "Startup & Dynamic",
                "Technical & Deep-Dive",
                "Creative & Engaging"
        );
        toneCombo.setValue("Executive & Authoritative");

        resumeContextArea.setPromptText("Paste your current resume summary, work history, or key skills here...");
        resumeContextArea.setPrefHeight(120);

        Button optimizeBtn = new Button("Optimize My LinkedIn Profile");
        optimizeBtn.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold;");

        progressIndicator.setVisible(false);
        progressIndicator.setMaxSize(25, 25);

        outputArea.setPromptText("Recruiter-optimized headlines, 'About' summary, and experience bullets will appear here...");
        outputArea.setEditable(false);
        outputArea.setPrefHeight(250);

        HBox optionsBar = new HBox(15, new Label("Select Tone:"), toneCombo, optimizeBtn, progressIndicator);
        optionsBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        getChildren().addAll(
                new Label("🚀 Professional LinkedIn Headline & Bio Optimizer"),
                new Label("Transform your experience into high-visibility, recruiter-optimized profile copy."),
                new Label("Your Professional Background / Resume Snippet:"),
                resumeContextArea,
                optionsBar,
                new Label("Generated LinkedIn Profile Suite:"),
                outputArea
        );

        optimizeBtn.setOnAction(e -> {
            String context = resumeContextArea.getText().trim();
            String tone = toneCombo.getValue();

            if (context.isEmpty()) {
                outputArea.setText("Please paste some background or resume info first.");
                return;
            }

            progressIndicator.setVisible(true);
            optimizeBtn.setDisable(true);

            Task<String> task = new Task<>() {
                @Override
                protected String call() throws ApiException {
                    return apiService.generateLinkedInProfile(tone, context);
                }
            };

            task.setOnSucceeded(event -> {
                outputArea.setText(task.getValue());
                progressIndicator.setVisible(false);
                optimizeBtn.setDisable(false);
            });

            task.setOnFailed(event -> {
                outputArea.setText("Optimization failed. Check API connection.");
                progressIndicator.setVisible(false);
                optimizeBtn.setDisable(false);
            });

            new Thread(task).start();
        });
    }
}

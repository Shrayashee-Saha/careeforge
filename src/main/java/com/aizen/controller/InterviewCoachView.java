package com.aizen.controller;

import com.aizen.service.ApiService;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class InterviewCoachView extends VBox {

    private final ApiService apiService = new ApiService();
    private final TextArea questionsArea = new TextArea();
    private final TextArea answerField = new TextArea();
    private final TextArea feedbackArea = new TextArea();
    private final TextField jobTitleField = new TextField();
    private final ProgressIndicator progressIndicator = new ProgressIndicator();

    public InterviewCoachView() {
        setSpacing(15);
        setPadding(new Insets(20));

        jobTitleField.setPromptText("Enter Target Job Title (e.g. Senior Java Developer)");

        Button generateBtn = new Button("Generate Mock Questions");
        generateBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold;");

        questionsArea.setPromptText("Click 'Generate Mock Questions' to load customized questions...");
        questionsArea.setEditable(false);
        questionsArea.setPrefHeight(200);

        answerField.setPromptText("Type your practice answer to a question here...");
        answerField.setPrefHeight(100);

        Button evaluateBtn = new Button("Get AI Feedback & Score");
        evaluateBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold;");

        feedbackArea.setPromptText("AI coaching feedback, scores, and improvement tips will appear here...");
        feedbackArea.setEditable(false);
        feedbackArea.setPrefHeight(150);

        progressIndicator.setVisible(false);
        progressIndicator.setMaxSize(30, 30);

        HBox topBar = new HBox(10, jobTitleField, generateBtn, progressIndicator);

        getChildren().addAll(
                new Label("AI Mock Interview Coach"),
                topBar,
                new Label("Customized Interview Questions:"),
                questionsArea,
                new Label("Your Practice Answer:"),
                answerField,
                evaluateBtn,
                new Label("Coach Evaluation & Score:"),
                feedbackArea
        );

        // Action to generate questions asynchronously
        generateBtn.setOnAction(e -> {
            String jobTitle = jobTitleField.getText().trim();
            if (jobTitle.isEmpty()) {
                questionsArea.setText("Please enter a target job title first.");
                return;
            }

            progressIndicator.setVisible(true);
            generateBtn.setDisable(true);

            Task<String> task = new Task<>() {
                @Override
                protected String call() {
                    return apiService.generateInterviewQuestions(jobTitle, "Experienced professional candidate");
                }
            };

            task.setOnSucceeded(event -> {
                questionsArea.setText(task.getValue());
                progressIndicator.setVisible(false);
                generateBtn.setDisable(false);
            });

            task.setOnFailed(event -> {
                questionsArea.setText("Failed to generate questions. Check API settings.");
                progressIndicator.setVisible(false);
                generateBtn.setDisable(false);
            });

            new Thread(task).start();
        });

        // Action to evaluate answer asynchronously
        evaluateBtn.setOnAction(e -> {
            // Evaluated safely as a final variable or evaluated inline
            String rawSelection = questionsArea.getSelectedText();
            final String selectedQuestion = (rawSelection == null || rawSelection.isBlank())
                    ? "General interview response"
                    : rawSelection.trim();

            String userAns = answerField.getText().trim();
            if (userAns.isEmpty()) {
                feedbackArea.setText("Please write an answer in the practice box before evaluating.");
                return;
            }

            evaluateBtn.setDisable(true);
            feedbackArea.setText("Evaluating your response with Gemini...");

            Task<String> evalTask = new Task<>() {
                @Override
                protected String call() {
                    return apiService.evaluateInterviewAnswer(selectedQuestion, userAns);
                }
            };

            evalTask.setOnSucceeded(event -> {
                feedbackArea.setText(evalTask.getValue());
                evaluateBtn.setDisable(false);
            });

            evalTask.setOnFailed(event -> {
                feedbackArea.setText("Evaluation failed.");
                evaluateBtn.setDisable(false);
            });

            new Thread(evalTask).start();
        });
    }
}

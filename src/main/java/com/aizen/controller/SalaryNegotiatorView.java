package com.aizen.controller;

import com.aizen.exception.ApiException;
import com.aizen.service.ApiService;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SalaryNegotiatorView extends VBox {

    private final ApiService apiService = new ApiService();
    private final TextField roleField = new TextField();
    private final TextField offerField = new TextField();
    private final TextField targetField = new TextField();
    private final TextArea contextArea = new TextArea();
    private final TextArea resultArea = new TextArea();
    private final ProgressIndicator progressIndicator = new ProgressIndicator();

    public SalaryNegotiatorView() {
        setSpacing(12);
        setPadding(new Insets(20));

        roleField.setPromptText("e.g. Senior Software Engineer");
        offerField.setPromptText("e.g. $110,000 + standard benefits");
        targetField.setPromptText("e.g. $130,000");
        contextArea.setPromptText("Optional: Remote status, sign-on bonus, competing offers, equity...");
        contextArea.setPrefHeight(70);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Target Job Title:"), roleField);
        grid.addRow(1, new Label("Offered Compensation:"), offerField);
        grid.addRow(2, new Label("Target Compensation:"), targetField);

        Button generateBtn = new Button("Generate Negotiation Strategy & Email Script");
        generateBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold;");

        progressIndicator.setVisible(false);
        progressIndicator.setMaxSize(25, 25);

        resultArea.setPromptText("Tailored negotiation plan, scripts, and counter-offer wording will appear here...");
        resultArea.setEditable(false);
        resultArea.setPrefHeight(250);

        getChildren().addAll(
                new Label("💼 AI Salary Negotiation Coach"),
                new Label("Maximize your compensation with expert data-driven positioning and scripts."),
                grid,
                new Label("Additional Context (Location, Perks, Competing Offers):"),
                contextArea,
                new HBox(15, generateBtn, progressIndicator),
                new Label("Strategy & Professional Email Script:"),
                resultArea
        );

        generateBtn.setOnAction(e -> {
            String role = roleField.getText().trim();
            String offer = offerField.getText().trim();
            String target = targetField.getText().trim();
            String context = contextArea.getText().trim();

            if (role.isEmpty() || offer.isEmpty() || target.isEmpty()) {
                resultArea.setText("Please fill out the Job Title, Offered Compensation, and Target Compensation fields.");
                return;
            }

            progressIndicator.setVisible(true);
            generateBtn.setDisable(true);

            Task<String> task = new Task<>() {
                @Override
                protected String call() throws ApiException {
                    return apiService.generateSalaryStrategy(role, offer, target, context);
                }
            };

            task.setOnSucceeded(event -> {
                resultArea.setText(task.getValue());
                progressIndicator.setVisible(false);
                generateBtn.setDisable(false);
            });

            task.setOnFailed(event -> {
                resultArea.setText("Failed to generate strategy. Check API connection.");
                progressIndicator.setVisible(false);
                generateBtn.setDisable(false);
            });

            new Thread(task).start();
        });
    }
}
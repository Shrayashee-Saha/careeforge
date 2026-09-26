package com.aizen.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * A reusable form section with dynamically added / removed rows
 * (used for Experience, Education, Projects, Certifications and Skills).
 * Each row is described by a list of {@link FieldSpec}s; values are exchanged as String[].
 */
public class DynamicSection extends VBox {

    /** Describes one input of a row. */
    public record FieldSpec(String label, boolean multiline) {
        public static FieldSpec text(String label) {
            return new FieldSpec(label, false);
        }

        public static FieldSpec area(String label) {
            return new FieldSpec(label, true);
        }
    }

    private final List<FieldSpec> specs;
    private final Runnable onChange;
    private final VBox rowsBox = new VBox(8);
    private final List<Row> rows = new ArrayList<>();

    public DynamicSection(String title, String addLabel, Runnable onChange, FieldSpec... specs) {
        super(8);
        this.specs = List.of(specs);
        this.onChange = onChange;
        getStyleClass().add("section-box");
        setPadding(new Insets(12));

        Label heading = new Label(title);
        heading.getStyleClass().add("section-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button add = new Button(addLabel);
        add.getStyleClass().add("secondary-button");
        add.setOnAction(e -> {
            addRow();
            onChange.run();
        });
        HBox header = new HBox(10, heading, spacer, add);
        header.setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(header, rowsBox);
    }

    /** Adds a row pre-filled with the given values (missing values stay empty). */
    public void addRow(String... values) {
        Row row = new Row(values);
        rows.add(row);
        rowsBox.getChildren().add(row.node);
    }

    public void clear() {
        rows.clear();
        rowsBox.getChildren().clear();
    }

    public void setValues(List<String[]> values) {
        clear();
        for (String[] v : values) {
            addRow(v);
        }
    }

    /** Returns the values of all non-empty rows. */
    public List<String[]> getValues() {
        List<String[]> result = new ArrayList<>();
        for (Row row : rows) {
            String[] values = new String[specs.size()];
            boolean any = false;
            for (int i = 0; i < values.length; i++) {
                values[i] = row.inputs.get(i).getText() == null ? "" : row.inputs.get(i).getText().trim();
                any |= !values[i].isEmpty();
            }
            if (any) {
                result.add(values);
            }
        }
        return result;
    }

    private final class Row {
        private final VBox node = new VBox(6);
        private final List<TextInputControl> inputs = new ArrayList<>();

        Row(String[] values) {
            node.getStyleClass().add("row-box");
            node.setPadding(new Insets(8));
            HBox singles = new HBox(8);
            singles.setAlignment(Pos.CENTER_LEFT);
            List<TextInputControl> areas = new ArrayList<>();

            for (int i = 0; i < specs.size(); i++) {
                FieldSpec spec = specs.get(i);
                TextInputControl input;
                if (spec.multiline()) {
                    TextArea ta = new TextArea();
                    ta.setPrefRowCount(2);
                    ta.setWrapText(true);
                    input = ta;
                    areas.add(ta);
                } else {
                    TextField tf = new TextField();
                    tf.setMinWidth(70);
                    tf.setPrefWidth(120);
                    HBox.setHgrow(tf, Priority.ALWAYS);
                    input = tf;
                    singles.getChildren().add(tf);
                }
                input.setPromptText(spec.label());
                if (values != null && i < values.length && values[i] != null) {
                    input.setText(values[i]);
                }
                input.textProperty().addListener((obs, oldV, newV) -> onChange.run());
                inputs.add(input);
            }

            Button remove = new Button("Remove");
            remove.getStyleClass().add("danger-button-small");
            remove.setOnAction(e -> {
                rows.remove(this);
                rowsBox.getChildren().remove(node);
                onChange.run();
            });
            singles.getChildren().add(remove);

            node.getChildren().add(singles);
            node.getChildren().addAll(areas);
        }
    }
}

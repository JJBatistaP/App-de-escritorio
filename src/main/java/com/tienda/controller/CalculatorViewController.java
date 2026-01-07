package com.tienda.controller;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CalculatorViewController {

    // Input fields for bill quantities
    private TextField fiveField;
    private TextField tenField;
    private TextField twentyField;
    private TextField fiftyField;
    private TextField hundredField;
    private TextField twoHundredField;
    private TextField fiveHundredField;
    private TextField thousandField;

    // Result labels for multiplications
    private Label fiveResult;
    private Label tenResult;
    private Label twentyResult;
    private Label fiftyResult;
    private Label hundredResult;
    private Label twoHundredResult;
    private Label fiveHundredResult;
    private Label thousandResult;

    // Transfer section
    private TextField transferField;
    private Label transferSumLabel;

    // UI components
    private Button calculateBtn;
    private Label totalLabel;

    public VBox createView() {
        initializeComponents();

        VBox mainVBox = new VBox(15);
        mainVBox.setStyle("-fx-padding: 20;");

        Label titleLabel = new Label("Calculadora de Efectivo");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        mainVBox.getChildren().add(titleLabel);

        // Create input grid
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);

        // Row 0: Labels
        inputGrid.add(new Label("5 x"), 0, 0);
        inputGrid.add(new Label("10 x"), 0, 1);
        inputGrid.add(new Label("20 x"), 0, 2);
        inputGrid.add(new Label("50 x"), 0, 3);
        inputGrid.add(new Label("100 x"), 0, 4);
        inputGrid.add(new Label("200 x"), 0, 5);
        inputGrid.add(new Label("500 x"), 0, 6);
        inputGrid.add(new Label("1000 x"), 0, 7);

        // Row 1: Input fields
        inputGrid.add(fiveField, 1, 0);
        inputGrid.add(new Label("="), 2, 0);
        inputGrid.add(fiveResult, 3, 0);
        inputGrid.add(tenField, 1, 1);
        inputGrid.add(new Label("="), 2, 1);
        inputGrid.add(tenResult, 3, 1);
        inputGrid.add(twentyField, 1, 2);
        inputGrid.add(new Label("="), 2, 2);
        inputGrid.add(twentyResult, 3, 2);
        inputGrid.add(fiftyField, 1, 3);
        inputGrid.add(new Label("="), 2, 3);
        inputGrid.add(fiftyResult, 3, 3);
        inputGrid.add(hundredField, 1, 4);
        inputGrid.add(new Label("="), 2, 4);
        inputGrid.add(hundredResult, 3, 4);
        inputGrid.add(twoHundredField, 1, 5);
        inputGrid.add(new Label("="), 2, 5);
        inputGrid.add(twoHundredResult, 3, 5);
        inputGrid.add(fiveHundredField, 1, 6);
        inputGrid.add(new Label("="), 2, 6);
        inputGrid.add(fiveHundredResult, 3, 6);
        inputGrid.add(thousandField, 1, 7);
        inputGrid.add(new Label("="), 2, 7);
        inputGrid.add(thousandResult, 3, 7);

        mainVBox.getChildren().add(inputGrid);

        // Calculate button
        mainVBox.getChildren().add(calculateBtn);

        // Total result
        mainVBox.getChildren().add(totalLabel);

        // Transfer section
        HBox transferBox = new HBox(10);
        transferBox.getChildren().addAll(new Label("+"), transferField, new Label("="), transferSumLabel);
        mainVBox.getChildren().add(transferBox);

        return mainVBox;
    }

    private void initializeComponents() {
        // Initialize input fields
        fiveField = new TextField();
        fiveField.setPromptText("Inserte la cantidad");
        fiveField.setPrefWidth(150);
        fiveField.setStyle("-fx-prompt-text-fill: white;");

        tenField = new TextField();
        tenField.setPromptText("Inserte la cantidad");
        tenField.setPrefWidth(150);
        tenField.setStyle("-fx-prompt-text-fill: white;");

        twentyField = new TextField();
        twentyField.setPromptText("Inserte la cantidad");
        twentyField.setPrefWidth(150);
        twentyField.setStyle("-fx-prompt-text-fill: white;");

        fiftyField = new TextField();
        fiftyField.setPromptText("Inserte la cantidad");
        fiftyField.setPrefWidth(150);
        fiftyField.setStyle("-fx-prompt-text-fill: white;");

        hundredField = new TextField();
        hundredField.setPromptText("Inserte la cantidad");
        hundredField.setPrefWidth(150);
        hundredField.setStyle("-fx-prompt-text-fill: white;");

        twoHundredField = new TextField();
        twoHundredField.setPromptText("Inserte la cantidad");
        twoHundredField.setPrefWidth(150);
        twoHundredField.setStyle("-fx-prompt-text-fill: white;");

        fiveHundredField = new TextField();
        fiveHundredField.setPromptText("Inserte la cantidad");
        fiveHundredField.setPrefWidth(150);
        fiveHundredField.setStyle("-fx-prompt-text-fill: white;");

        thousandField = new TextField();
        thousandField.setPromptText("Inserte la cantidad");
        thousandField.setPrefWidth(150);
        thousandField.setStyle("-fx-prompt-text-fill: white;");

        // Initialize result labels
        fiveResult = new Label("0.00");
        fiveResult.setStyle("-fx-font-weight: bold;");
        tenResult = new Label("0.00");
        tenResult.setStyle("-fx-font-weight: bold;");
        twentyResult = new Label("0.00");
        twentyResult.setStyle("-fx-font-weight: bold;");
        fiftyResult = new Label("0.00");
        fiftyResult.setStyle("-fx-font-weight: bold;");
        hundredResult = new Label("0.00");
        hundredResult.setStyle("-fx-font-weight: bold;");
        twoHundredResult = new Label("0.00");
        twoHundredResult.setStyle("-fx-font-weight: bold;");
        fiveHundredResult = new Label("0.00");
        fiveHundredResult.setStyle("-fx-font-weight: bold;");
        thousandResult = new Label("0.00");
        thousandResult.setStyle("-fx-font-weight: bold;");

        // Initialize transfer components
        transferField = new TextField();
        transferField.setPromptText("Ingrese el monto de las transferencias");
        transferField.setPrefWidth(250);
        transferField.setStyle("-fx-prompt-text-fill: white;");
        transferField.setOnKeyReleased(e -> updateTransferSum());

        transferSumLabel = new Label("0.00");
        transferSumLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Initialize UI components
        calculateBtn = new Button("Calcular");
        calculateBtn.setOnAction(e -> performCalculation());

        totalLabel = new Label("Total: 0.00");
        totalLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
    }

    private void performCalculation() {
        try {
            double five = parseDouble(fiveField.getText()) * 5;
            double ten = parseDouble(tenField.getText()) * 10;
            double twenty = parseDouble(twentyField.getText()) * 20;
            double fifty = parseDouble(fiftyField.getText()) * 50;
            double hundred = parseDouble(hundredField.getText()) * 100;
            double twoHundred = parseDouble(twoHundredField.getText()) * 200;
            double fiveHundred = parseDouble(fiveHundredField.getText()) * 500;
            double thousand = parseDouble(thousandField.getText()) * 1000;

            // Update result labels
            fiveResult.setText(String.format("%.2f", five));
            tenResult.setText(String.format("%.2f", ten));
            twentyResult.setText(String.format("%.2f", twenty));
            fiftyResult.setText(String.format("%.2f", fifty));
            hundredResult.setText(String.format("%.2f", hundred));
            twoHundredResult.setText(String.format("%.2f", twoHundred));
            fiveHundredResult.setText(String.format("%.2f", fiveHundred));
            thousandResult.setText(String.format("%.2f", thousand));

            double total = five + ten + twenty + fifty + hundred + twoHundred + fiveHundred + thousand;
            totalLabel.setText(String.format("Total: %.2f", total));

            updateTransferSum();

        } catch (NumberFormatException e) {
            totalLabel.setText("Error: Verifique que todos los campos numéricos sean válidos");
        } catch (Exception e) {
            totalLabel.setText("Error inesperado: " + e.getMessage());
        }
    }

    private void updateTransferSum() {
        try {
            double total = parseDouble(totalLabel.getText().replace("Total: ", ""));
            double transfer = parseDouble(transferField.getText());
            double sum = total + transfer;
            transferSumLabel.setText(String.format("%.2f", sum));
        } catch (NumberFormatException e) {
            transferSumLabel.setText("0.00");
        }
    }

    private double parseDouble(String text) throws NumberFormatException {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(text.trim());
    }
}

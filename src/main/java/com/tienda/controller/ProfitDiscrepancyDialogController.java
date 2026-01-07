package com.tienda.controller;

import com.tienda.Product;
// import com.tienda.service.ProfitAnalysisService;
// import com.tienda.service.ProfitAnalysisService.ProfitSuggestion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class ProfitDiscrepancyDialogController {

    private TextField systemProfitField;
    private TextField userProfitField;
    private Button analyzeButton;
    // private TableView<ProfitSuggestion> suggestionsTable;
    // private TableColumn<ProfitSuggestion, String> productCol;
    // private TableColumn<ProfitSuggestion, Integer> quantityCol;
    // private TableColumn<ProfitSuggestion, Double> unitProfitCol;
    // private TableColumn<ProfitSuggestion, Double> totalDifferenceCol;
    // private TableColumn<ProfitSuggestion, String> typeCol;

    private ObservableList<Product> products;
    // private ProfitAnalysisService profitAnalysisService = new ProfitAnalysisService();

    public ProfitDiscrepancyDialogController(ObservableList<Product> products) {
        this.products = products;
    }

    public void showDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Análisis de Discrepancias de Ganancia");
        dialog.setResizable(true);

        VBox mainVBox = new VBox(15);
        mainVBox.setPadding(new Insets(20));
        mainVBox.setPrefWidth(800);
        mainVBox.setPrefHeight(600);

        // Input section
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);

        inputGrid.add(new Label("Ganancia del Sistema:"), 0, 0);
        systemProfitField = new TextField();
        systemProfitField.setEditable(false);
        systemProfitField.setPrefWidth(150);
        inputGrid.add(systemProfitField, 1, 0);

        inputGrid.add(new Label("Ganancia Ingresada por Usuario:"), 0, 1);
        userProfitField = new TextField();
        userProfitField.setPromptText("Ingrese la ganancia real");
        userProfitField.setPrefWidth(150);
        inputGrid.add(userProfitField, 1, 1);

        analyzeButton = new Button("Analizar Discrepancias");
        analyzeButton.setOnAction(e -> analyzeDiscrepancies());
        inputGrid.add(analyzeButton, 2, 1);

        mainVBox.getChildren().add(inputGrid);

        // Results section
        Label resultsLabel = new Label("Sugerencias de Productos que Pueden Explicar la Diferencia:");
        resultsLabel.setStyle("-fx-font-weight: bold;");
        mainVBox.getChildren().add(resultsLabel);

        // Table - commented out due to missing ProfitAnalysisService
        // setupTable();
        // mainVBox.getChildren().add(suggestionsTable);

        Label notAvailableLabel = new Label("Esta funcionalidad no está disponible actualmente.");
        notAvailableLabel.setStyle("-fx-text-fill: red;");
        mainVBox.getChildren().add(notAvailableLabel);

        // Calculate and display system profit
        calculateSystemProfit();

        Scene scene = new Scene(mainVBox);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // private void setupTable() {
    //     suggestionsTable = new TableView<>();
    //     suggestionsTable.setPrefHeight(400);
    //
    //     productCol = new TableColumn<>("Producto");
    //     productCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
    //         data.getValue().getProduct().getProducto()));
    //     productCol.setPrefWidth(200);
    //
    //     quantityCol = new TableColumn<>("Cantidad Estimada");
    //     quantityCol.setCellValueFactory(new PropertyValueFactory<>("estimatedQuantity"));
    //     quantityCol.setPrefWidth(120);
    //
    //     unitProfitCol = new TableColumn<>("Precio Venta Unitario");
    //     unitProfitCol.setCellValueFactory(new PropertyValueFactory<>("unitProfit"));
    //     unitProfitCol.setPrefWidth(120);
    //
    //     totalDifferenceCol = new TableColumn<>("Valor Total");
    //     totalDifferenceCol.setCellValueFactory(new PropertyValueFactory<>("totalDifference"));
    //     totalDifferenceCol.setPrefWidth(120);
    //
    //     typeCol = new TableColumn<>("Tipo");
    //     typeCol.setCellValueFactory(new PropertyValueFactory<>("discrepancyType"));
    //     typeCol.setPrefWidth(150);
    //
    //     suggestionsTable.getColumns().addAll(productCol, quantityCol, unitProfitCol, totalDifferenceCol, typeCol);
    // }

    private void calculateSystemProfit() {
        // Calculate system profit: sum of (precioV * invFinal) for all products
        double systemProfit = products.stream()
            .mapToDouble(p -> p.getPrecioV() * p.getInvFinal())
            .sum();

        // Use US locale to ensure dot as decimal separator
        NumberFormat usFormat = NumberFormat.getInstance(Locale.US);
        usFormat.setMaximumFractionDigits(2);
        usFormat.setMinimumFractionDigits(2);
        systemProfitField.setText(usFormat.format(systemProfit));
    }

    private void analyzeDiscrepancies() {
        try {
            String userInput = userProfitField.getText().trim();

            // Check if input is empty
            if (userInput.isEmpty()) {
                showError("Error", "Por favor ingrese la ganancia del usuario.");
                return;
            }

            // Debug: Check system profit field
            String systemProfitText = systemProfitField.getText().trim();
            if (systemProfitText.isEmpty()) {
                showError("Error", "Error interno: La ganancia del sistema no se calculó correctamente.");
                return;
            }

            // Use NumberFormat to parse both values, handling locale-specific formatting
            NumberFormat numberFormat = NumberFormat.getInstance();
            double systemProfit = numberFormat.parse(systemProfitText).doubleValue();
            double userProfit = numberFormat.parse(userInput).doubleValue();

            if (userProfit < 0) {
                showError("Error", "La ganancia del usuario no puede ser negativa.");
                return;
            }

            // Find suggestions - functionality not available
            // var suggestions = profitAnalysisService.findProfitDiscrepancySuggestions(products, systemProfit, userProfit);

            // Display results - functionality not available
            // suggestionsTable.setItems(FXCollections.observableArrayList(suggestions));

            showInfo("Funcionalidad No Disponible", "La funcionalidad de análisis de discrepancias no está implementada actualmente.");

        } catch (ParseException e) {
            String systemText = systemProfitField.getText();
            String userText = userProfitField.getText();
            showError("Error de formato", "Error al procesar los números:\n\nGanancia sistema: '" + systemText + "'\nGanancia usuario: '" + userText + "'\n\nFormatos válidos: 1234.56, 1234,56, 1234");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

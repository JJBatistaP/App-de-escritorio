package com.tienda.controller;

import com.tienda.Product;
import com.tienda.service.ExcelService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class TotalUtilidadesController {

    private Button importBtn;
    private Button calculateBtn;
    private Button exportChartBtn;
    private Button refreshBtn;
    private Button filterBtn;

    private VBox loadingBox;
    private VBox resultsContainer;

    private Label totalUtilityLabel;
    private Label productsCountLabel;
    private Label bestProductLabel;
    private Label highUtilityLabel;
    private Label mediumUtilityLabel;
    private Label lowUtilityLabel;

    private PieChart pieChart;
    private TextField searchField;
    private VBox legendBox;
    private TableView<Product> utilitiesTable;
    private TableColumn<Product, String> productCol;
    private TableColumn<Product, Double> utilityCol;
    private TableColumn<Product, Double> percentageCol;

    private DatePicker startDatePicker;
    private DatePicker endDatePicker;

    private Scene scene;
    private ObservableList<Product> utilitiesProducts = FXCollections.observableArrayList();
    private List<File> selectedFiles;
    private ExcelService excelService = new ExcelService();

    public ScrollPane createView() {
        initializeComponents();
        setupTable();

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);

        VBox mainVBox = new VBox(10);
        mainVBox.setStyle("-fx-padding: 20;");

        // Controles superiores
        HBox controlsBox = new HBox(10);
        controlsBox.getChildren().addAll(importBtn, calculateBtn, exportChartBtn, refreshBtn, filterBtn);
        mainVBox.getChildren().add(controlsBox);

        // Filtros de fecha
        HBox dateBox = new HBox(10);
        dateBox.getChildren().addAll(new Label("Desde:"), startDatePicker, new Label("Hasta:"), endDatePicker);
        mainVBox.getChildren().add(dateBox);

        // Loading box
        loadingBox = new VBox(10);
        loadingBox.setStyle("-fx-alignment: center; -fx-padding: 20;");
        loadingBox.getChildren().add(new Label("Procesando archivos..."));
        loadingBox.setVisible(false);
        mainVBox.getChildren().add(loadingBox);

        // Contenedor de resultados
        resultsContainer = new VBox(10);
        resultsContainer.setVisible(false);

        // Labels de resumen
        VBox summaryBox = new VBox(5);
        summaryBox.getChildren().addAll(
            totalUtilityLabel, productsCountLabel, bestProductLabel,
            highUtilityLabel, mediumUtilityLabel, lowUtilityLabel
        );
        resultsContainer.getChildren().add(summaryBox);

        // Gráfica y tabla
        HBox chartTableBox = new HBox(20);
        chartTableBox.setStyle("-fx-padding: 10 0 10 0;");

        // Gráfica de pastel
        VBox chartBox = new VBox(10);
        chartBox.getChildren().addAll(
            new Label("Distribución de Utilidades por Producto"),
            pieChart, searchField
        );
        HBox.setHgrow(chartBox, Priority.ALWAYS);

        // Leyenda interactiva
        VBox legendContainer = new VBox(10);
        legendContainer.getChildren().addAll(
            new Label("Leyenda Interactiva"),
            new ScrollPane(legendBox)
        );
        ((ScrollPane)legendContainer.getChildren().get(1)).setFitToWidth(true);
        ((ScrollPane)legendContainer.getChildren().get(1)).setPrefHeight(350);

        chartTableBox.getChildren().addAll(chartBox, legendContainer);
        resultsContainer.getChildren().add(chartTableBox);

        // Tabla
        resultsContainer.getChildren().add(utilitiesTable);

        mainVBox.getChildren().add(resultsContainer);
        scrollPane.setContent(mainVBox);

        return scrollPane;
    }

    private void initializeComponents() {
        importBtn = new Button("Importar Archivos");
        importBtn.setOnAction(e -> importFiles());

        calculateBtn = new Button("Calcular Utilidades");
        calculateBtn.setOnAction(e -> calculateUtilities());
        calculateBtn.setDisable(true);

        exportChartBtn = new Button("Exportar Gráfica");
        exportChartBtn.setOnAction(e -> exportChart());

        refreshBtn = new Button("Actualizar");
        refreshBtn.setOnAction(e -> refreshData());

        filterBtn = new Button("Aplicar Filtros");
        filterBtn.setOnAction(e -> filterByDate());

        startDatePicker = new DatePicker();
        endDatePicker = new DatePicker();

        totalUtilityLabel = new Label("Utilidad Total: $0.00");
        productsCountLabel = new Label("Productos: 0");
        bestProductLabel = new Label("Mejor producto: N/A");
        highUtilityLabel = new Label("Alta utilidad (>50%): 0 productos");
        mediumUtilityLabel = new Label("Media utilidad (20-50%): 0 productos");
        lowUtilityLabel = new Label("Baja utilidad (<20%): 0 productos");

        pieChart = new PieChart();
        pieChart.setPrefHeight(400);

        searchField = new TextField();
        searchField.setPromptText("Buscar producto...");
        searchField.setOnAction(e -> searchProduct());

        legendBox = new VBox(5);
        legendBox.setStyle("-fx-padding: 10;");

        utilitiesTable = new TableView<>();
        productCol = new TableColumn<>("Producto");
        productCol.setPrefWidth(200);
        productCol.setMinWidth(200);

        utilityCol = new TableColumn<>("Utilidad");
        utilityCol.setPrefWidth(150);

        percentageCol = new TableColumn<>("Porcentaje");
        percentageCol.setPrefWidth(150);

        utilitiesTable.getColumns().addAll(productCol, utilityCol, percentageCol);
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    private void setupTable() {
        productCol.setCellValueFactory(new PropertyValueFactory<>("producto"));
        utilityCol.setCellValueFactory(new PropertyValueFactory<>("utilidadVenta"));
        percentageCol.setCellValueFactory(data -> data.getValue().percentageProperty().asObject());
    }

    private void importFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivos Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        selectedFiles = fileChooser.showOpenMultipleDialog(null);
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            calculateBtn.setDisable(false);
        }
    }

    private void calculateUtilities() {
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            loadingBox.setVisible(true);
            resultsContainer.setVisible(false);

            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    processFiles();
                    return null;
                }

                @Override
                protected void succeeded() {
                    loadingBox.setVisible(false);
                    resultsContainer.setVisible(true);
                    updateUI();
                }

                @Override
                protected void failed() {
                    loadingBox.setVisible(false);
                    Throwable exception = getException();
                    showError("Error", "Error durante el procesamiento: " + exception.getMessage());
                }
            };

            new Thread(task).start();
        }
    }

    private void processFiles() {
        utilitiesProducts.clear();
        for (File file : selectedFiles) {
            try {
                List<Product> products = excelService.importFromExcel(file);
                utilitiesProducts.addAll(products);
            } catch (Exception e) {
                // Log error but continue
                System.err.println("Error processing file " + file.getName() + ": " + e.getMessage());
            }
        }
    }

    private void updateUI() {
        // Calcular estadísticas
        double totalUtility = utilitiesProducts.stream().mapToDouble(Product::getUtilidadVenta).sum();
        int productsCount = utilitiesProducts.size();

        Product bestProduct = utilitiesProducts.stream()
            .max((p1, p2) -> Double.compare(p1.getUtilidadVenta(), p2.getUtilidadVenta()))
            .orElse(null);

        // Actualizar labels
        totalUtilityLabel.setText(String.format("Utilidad Total: $%.2f", totalUtility));
        productsCountLabel.setText("Productos: " + productsCount);
        if (bestProduct != null) {
            bestProductLabel.setText("Mejor producto: " + bestProduct.getProducto() + " ($" + String.format("%.2f", bestProduct.getUtilidadVenta()) + ")");
        }

        // Calcular rangos
        long highCount = utilitiesProducts.stream().filter(p -> p.getUtilidadVenta() > 1000).count();
        long mediumCount = utilitiesProducts.stream().filter(p -> p.getUtilidadVenta() >= 500 && p.getUtilidadVenta() <= 1000).count();
        long lowCount = utilitiesProducts.stream().filter(p -> p.getUtilidadVenta() < 500).count();

        highUtilityLabel.setText("Alta Utilidad (>1000): " + highCount + " productos");
        mediumUtilityLabel.setText("Media Utilidad (500-1000): " + mediumCount + " productos");
        lowUtilityLabel.setText("Baja Utilidad (<500): " + lowCount + " productos");

        // Crear gráfica
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Product p : utilitiesProducts) {
            pieChartData.add(new PieChart.Data(p.getProducto(), p.getUtilidadVenta()));
        }
        pieChart.setData(pieChartData);

        // Configurar porcentajes
        for (Product p : utilitiesProducts) {
            p.setPercentage((p.getUtilidadVenta() / totalUtility) * 100);
        }

        // Agregar manejadores de eventos para selección
        setupChartSelection();

        // Actualizar tabla
        utilitiesTable.setItems(utilitiesProducts);

        // Crear leyenda
        createLegend();

        exportChartBtn.setVisible(true);
    }

    private void setupChartSelection() {
        for (PieChart.Data data : pieChart.getData()) {
            data.getNode().setOnMouseClicked(event -> {
                // Limpiar selección anterior
                clearChartSelection();

                // Aplicar estilo de selección al elemento clicado
                data.getNode().getStyleClass().add("selected");

                // Resaltar en la tabla
                highlightProductInTable(data.getName());
            });
        }
    }

    private void clearChartSelection() {
        for (PieChart.Data data : pieChart.getData()) {
            if (data.getNode() != null) {
                data.getNode().getStyleClass().remove("selected");
            }
        }
        // Limpiar resaltado en tabla
        utilitiesTable.getSelectionModel().clearSelection();
    }

    private void highlightProductInTable(String productName) {
        for (int i = 0; i < utilitiesProducts.size(); i++) {
            if (utilitiesProducts.get(i).getProducto().equals(productName)) {
                utilitiesTable.getSelectionModel().select(i);
                utilitiesTable.scrollTo(i);
                break;
            }
        }
    }

    private void createLegend() {
        legendBox.getChildren().clear();
        for (PieChart.Data data : pieChart.getData()) {
            CheckBox checkBox = new CheckBox(data.getName());
            checkBox.setSelected(true);
            checkBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    data.getNode().setVisible(true);
                    data.getNode().setManaged(true);
                } else {
                    data.getNode().setVisible(false);
                    data.getNode().setManaged(false);
                }
            });
            checkBox.setOnMouseEntered(e -> {
                if (data.getNode() != null) {
                    data.getNode().setScaleX(1.1);
                    data.getNode().setScaleY(1.1);
                }
            });
            checkBox.setOnMouseExited(e -> {
                if (data.getNode() != null) {
                    data.getNode().setScaleX(1.0);
                    data.getNode().setScaleY(1.0);
                }
            });
            legendBox.getChildren().add(checkBox);
        }
    }

    private void exportChart() {
        // Implementar exportación de gráfica
        showInfo("Exportar", "Funcionalidad de exportación próximamente.");
    }

    private void refreshData() {
        if (!utilitiesProducts.isEmpty()) {
            updateUI();
        }
    }

    private void filterByDate() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        if (startDate != null && endDate != null) {
            // Aquí se implementaría el filtro por fecha si los archivos tienen fechas
            showInfo("Filtro", "Filtro por fecha aplicado (simulado).");
        }
    }

    private void applyDateFilter() {
        filterByDate();
    }

    private void searchProduct() {
        String query = searchField.getText().toLowerCase();
        ObservableList<Product> filtered = utilitiesProducts.stream()
            .filter(p -> p.getProducto().toLowerCase().contains(query))
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
        utilitiesTable.setItems(filtered);
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
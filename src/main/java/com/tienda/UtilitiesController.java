package com.tienda;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class UtilitiesController implements Initializable {

    @FXML private ScrollPane utilitiesPane;
    @FXML private Label titleLabel;
    @FXML private Button fileBtn;
    @FXML private Button calcBtn;
    @FXML private VBox uploadCard;
    @FXML private VBox loadingBox;
    @FXML private ProgressIndicator loadingSpinner;
    @FXML private VBox resultadosWrapper;
    @FXML private Button exportChartBtn;
    @FXML private CheckBox toggleThemeBtn;
    @FXML private TextField searchLegend;
    @FXML private HBox chartWrapper;
    @FXML private VBox chartContainer;
    @FXML private PieChart pieChart;
    @FXML private VBox legendContainer;
    @FXML private VBox legendVBox;
    @FXML private TableView<Product> top5Table;
    @FXML private TableColumn<Product, String> top5ProductCol;
    @FXML private TableColumn<Product, Double> top5UtilityCol;
    @FXML private TableView<Product> bottom5Table;
    @FXML private TableColumn<Product, String> bottom5ProductCol;
    @FXML private TableColumn<Product, Double> bottom5UtilityCol;
    @FXML private VBox fileResults;
    @FXML private Label fileCountLabel;

    private Scene scene;
    private boolean isDarkTheme = false;
    private ObservableList<Product> utilitiesProducts = FXCollections.observableArrayList();
    private List<File> selectedFiles;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar columnas de tablas
        top5ProductCol.setCellValueFactory(new PropertyValueFactory<>("producto"));
        top5UtilityCol.setCellValueFactory(new PropertyValueFactory<>("utilidadVenta"));
        bottom5ProductCol.setCellValueFactory(new PropertyValueFactory<>("producto"));
        bottom5UtilityCol.setCellValueFactory(new PropertyValueFactory<>("utilidadVenta"));
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    @FXML
    private void selectFiles(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivos Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        selectedFiles = fileChooser.showOpenMultipleDialog(null);
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            calcBtn.setDisable(false);
        }
    }

    @FXML
    private void calculateUtilities(ActionEvent event) {
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            // Mostrar loading
            loadingBox.setVisible(true);
            uploadCard.setVisible(false);

            // Procesar en background
            new Thread(() -> {
                processFiles();
            }).start();
        }
    }

    private void processFiles() {
        utilitiesProducts.clear();
        int totalFiles = selectedFiles.size();
        for (int i = 0; i < totalFiles; i++) {
            File file = selectedFiles.get(i);
            try (FileInputStream fis = new FileInputStream(file); Workbook workbook = new XSSFWorkbook(fis)) {
                Sheet sheet = workbook.getSheetAt(0);
                for (int j = 1; j <= sheet.getLastRowNum(); j++) {
                    Row row = sheet.getRow(j);
                    if (row != null && row.getCell(0) != null) {
                        String cellValue = row.getCell(0).getStringCellValue().toLowerCase();
                        // Ignorar filas que contengan "total", "resumen" o "unidades"
                        if (cellValue.contains("total") || cellValue.contains("resumen") || cellValue.contains("unidades")) {
                            continue;
                        }
                        if (!cellValue.isEmpty()) {
                            try {
                                String producto = row.getCell(0).getStringCellValue();
                                String unidad = "Unidades";
                                int invInicial = (int) (row.getCell(2) != null && row.getCell(2).getCellType() == CellType.NUMERIC ? row.getCell(2).getNumericCellValue() : 0);
                                int entrada = (int) (row.getCell(3) != null && row.getCell(3).getCellType() == CellType.NUMERIC ? row.getCell(3).getNumericCellValue() : 0);
                                int venta = (int) (row.getCell(4) != null && row.getCell(4).getCellType() == CellType.NUMERIC ? row.getCell(4).getNumericCellValue() : 0);
                                int merma = (int) (row.getCell(5) != null && row.getCell(5).getCellType() == CellType.NUMERIC ? row.getCell(5).getNumericCellValue() : 0);
                                double precioC = row.getCell(7) != null && row.getCell(7).getCellType() == CellType.NUMERIC ? row.getCell(7).getNumericCellValue() : 0.0;
                                double precioV = row.getCell(8) != null && row.getCell(8).getCellType() == CellType.NUMERIC ? row.getCell(8).getNumericCellValue() : 0.0;
                                utilitiesProducts.add(new Product(producto, invInicial, entrada, venta, merma, precioC, precioV, "", unidad));
                            } catch (Exception ex) {
                                // Ignorar fila con error
                            }
                        }
                    }
                }
            } catch (IOException e) {
                // Manejar error
            }
        }
        // Guardar inventario después de procesar
        saveInventory();

        javafx.application.Platform.runLater(() -> {
            loadingBox.setVisible(false);
            resultadosWrapper.setVisible(true);
            resultadosWrapper.setOpacity(1.0);
            updateUIAfterImport();
            // Actualizar contador de archivos
            fileCountLabel.setText("Archivos importados: " + totalFiles);
        });
    }

    @FXML
    private void exportChartAsPNG(ActionEvent event) {
        // Lógica de exportación
    }

    @FXML
    private void toggleTheme(ActionEvent event) {
        isDarkTheme = toggleThemeBtn.isSelected();
        if (isDarkTheme) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/dark-theme.css").toExternalForm());
            utilitiesPane.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50 0%, #34495e 100%);");
        } else {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            utilitiesPane.setStyle("-fx-background-color: linear-gradient(to bottom, #f4f6f9 0%, #ffffff 100%);");
        }
    }

    @FXML
    private void filterLegend(ActionEvent event) {
        // Lógica de filtrado
    }

    private void updateUIAfterImport() {
        // Crear datos del gráfico
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Product p : utilitiesProducts) {
            pieChartData.add(new PieChart.Data(p.productoProperty().get(), p.utilidadVentaProperty().get()));
        }
        java.util.Collections.shuffle(pieChartData);
        pieChart.setData(pieChartData);

        // Tooltips y leyenda
        for (PieChart.Data data : pieChartData) {
            Tooltip.install(data.getNode(), new Tooltip(data.getName()));
        }
        createInteractiveLegend();

        // Mostrar elementos
        pieChart.setVisible(true);
        legendVBox.setVisible(true);
        exportChartBtn.setVisible(true);
        updateTop5Utilities();
    }

    private void createInteractiveLegend() {
        legendVBox.getChildren().clear();
        for (PieChart.Data data : pieChart.getData()) {
            CheckBox checkBox = new CheckBox(data.getName());
            checkBox.setSelected(true);
            // Agregar tooltip para mostrar el nombre completo si es muy largo
            Tooltip tooltip = new Tooltip(data.getName());
            checkBox.setTooltip(tooltip);
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
            legendVBox.getChildren().add(checkBox);
        }
    }

    private void updateTop5Utilities() {
        // Top 5 mayor utilidad
        ObservableList<Product> top5Data = FXCollections.observableArrayList();
        utilitiesProducts.stream()
            .sorted((p1, p2) -> Double.compare(p2.getUtilidadVenta(), p1.getUtilidadVenta()))
            .limit(5)
            .forEach(top5Data::add);
        top5Table.setItems(top5Data);

        // Top 5 menor utilidad
        ObservableList<Product> bottom5Data = FXCollections.observableArrayList();
        utilitiesProducts.stream()
            .sorted((p1, p2) -> Double.compare(p1.getUtilidadVenta(), p2.getUtilidadVenta()))
            .limit(5)
            .forEach(bottom5Data::add);
        bottom5Table.setItems(bottom5Data);
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    private void saveInventory() {
        try {
            LocalDate now = LocalDate.now();
            String dateStr = now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            Path inventoryDir = Paths.get(System.getProperty("user.home"), "Desktop", "Tienda APP", "Inventarios", String.valueOf(now.getYear()), String.format("%02d", now.getMonthValue()));
            Files.createDirectories(inventoryDir);
            Path inventoryFile = inventoryDir.resolve(dateStr + ".dat");
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(inventoryFile))) {
                oos.writeObject(utilitiesProducts);
            }
        } catch (IOException e) {
            // Manejar error
        }
    }
}

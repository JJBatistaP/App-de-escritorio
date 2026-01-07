package com.tienda.controller;

import com.tienda.Product;
import com.tienda.DecimalStringConverter;
import com.tienda.service.ExcelService;
import com.tienda.service.FileTrackingService;
import com.tienda.service.AutoSaveService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.scene.control.TextInputDialog;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MainViewController {

    private TableView<Product> table;
    private TableColumn<Product, String> productoCol;
    private TableColumn<Product, Void> eliminarCol;
    private TableColumn<Product, Double> invInicialCol;
    private TableColumn<Product, Double> entradaCol;
    private TableColumn<Product, Double> ventaCol;
    private TableColumn<Product, Double> mermaCol;
    private TableColumn<Product, Double> invFinalCol;
    private TableColumn<Product, Double> precioCCol;
    private TableColumn<Product, Double> precioVCol;
    private TableColumn<Product, Double> valorInvInicialCol;
    private TableColumn<Product, Double> utilidadUnidadCol;
    private TableColumn<Product, Double> utilidadVentaCol;

    private Button addBtn;
    private Button clearBtn;
    private Button exportBtn;
    private Button importBtn;
    private Button importNewBtn;
    private Button loadBackupBtn;

    private Label totalProductosLabel;
    private Label precioCTotalLabel;
    private Label precioVTotalLabel;
    private Label gananciaTotalLabel;
    private Label utilidadDiaLabel;

    private ListView<String> inventoryListView;

    private ObservableList<Product> products = FXCollections.observableArrayList();

    public ObservableList<Product> getProducts() {
        return products;
    }

    // Lista de unidades del sistema
    private final List<String> unidadesSistema = List.of("Unidades", "Libras", "Kg", "Litros", "Paquetes", "libras");

    // Variable para rastrear qué celda tiene el editLabel visible
    private Label currentEditLabel = null;
    private ExcelService excelService = new ExcelService();
    private FileTrackingService fileTrackingService = new FileTrackingService();
    private AutoSaveService autoSaveService;

    public VBox createView() {
        // Inicializar servicios
        autoSaveService = new AutoSaveService(products);
        autoSaveService.startAutoSave();

        initializeComponents();
        setupTable();
        table.setItems(products);
        updateInventoryListView();

        VBox mainVBox = new VBox(10);
        mainVBox.setStyle("-fx-padding: 20;");
        mainVBox.getStyleClass().add("table-container");

        // Tabla de productos
        VBox.setVgrow(table, Priority.ALWAYS);
        mainVBox.getChildren().add(table);

        // Botones
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(addBtn, clearBtn, exportBtn, importBtn, importNewBtn, loadBackupBtn);
        mainVBox.getChildren().add(buttonBox);

        // Totales organizados en 3 columnas
        GridPane totalsBox = new GridPane();
        totalsBox.setHgap(20);
        totalsBox.setVgap(5);
        totalsBox.add(totalProductosLabel, 0, 0);
        totalsBox.add(precioCTotalLabel, 0, 1);
        totalsBox.add(precioVTotalLabel, 0, 2);
        totalsBox.add(gananciaTotalLabel, 1, 0);
        totalsBox.add(utilidadDiaLabel, 1, 1);
        mainVBox.getChildren().add(totalsBox);

        // ListView de productos con inventario 0
        VBox inventoryBox = new VBox(5);
        Label inventoryLabel = new Label("Productos con inventario 0:");
        inventoryLabel.setStyle("-fx-font-weight: bold;");
        inventoryBox.getChildren().addAll(inventoryLabel, inventoryListView);
        mainVBox.getChildren().add(inventoryBox);

        return mainVBox;
    }

    private void initializeComponents() {
        // Tabla y columnas
        table = new TableView<>();
        table.setEditable(true);
        table.getStyleClass().add("table-view");

        productoCol = new TableColumn<>("Producto");
        productoCol.setPrefWidth(200);
        productoCol.setMinWidth(200);

        eliminarCol = new TableColumn<>("Eliminar");
        eliminarCol.setMinWidth(120);
        eliminarCol.setPrefWidth(120);

        invInicialCol = new TableColumn<>("Inv. Inicial");
        invInicialCol.setPrefWidth(120);

        entradaCol = new TableColumn<>("Entrada");
        entradaCol.setPrefWidth(100);

        ventaCol = new TableColumn<>("Venta");
        ventaCol.setPrefWidth(100);

        mermaCol = new TableColumn<>("Merma");
        mermaCol.setPrefWidth(100);

        invFinalCol = new TableColumn<>("Inv. Final");
        invFinalCol.setPrefWidth(120);

        precioCCol = new TableColumn<>("Precio C.");
        precioCCol.setPrefWidth(120);

        precioVCol = new TableColumn<>("Precio V.");
        precioVCol.setPrefWidth(120);

        valorInvInicialCol = new TableColumn<>("Valor inv final");
        valorInvInicialCol.setPrefWidth(150);

        utilidadUnidadCol = new TableColumn<>("Utilidad Unidad");
        utilidadUnidadCol.setPrefWidth(140);

        utilidadVentaCol = new TableColumn<>("Utilidad Venta");
        utilidadVentaCol.setPrefWidth(140);

        table.getColumns().addAll(productoCol, eliminarCol, invInicialCol, entradaCol, ventaCol,
                                mermaCol, invFinalCol, precioCCol, precioVCol, valorInvInicialCol,
                                utilidadUnidadCol, utilidadVentaCol);

        // Agregar borde rojo a filas con precio 0, azul a filas con entrada > 0
        // Asegurar alineación perfecta de todas las celdas
        table.setRowFactory(tv -> new TableRow<Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    // Aplicar estilos sin afectar la alineación de celdas
                    if (item.getPrecioC() == 0 || item.getPrecioV() == 0) {
                        // Estilo para filas con precios en 0 - borde rojo, fondo sutil
                        setStyle("-fx-border-color: red; -fx-border-width: 2px 0 2px 0; -fx-background-color: rgba(255, 235, 235, 0.3); -fx-padding: 0; -fx-border-insets: 0; -fx-background-insets: 0;");
                    } else if (item.getEntrada() > 0) {
                        // Estilo para filas con entrada > 0 - borde azul, fondo sutil
                        setStyle("-fx-border-color: blue; -fx-border-width: 2px 0 2px 0; -fx-background-color: rgba(235, 245, 255, 0.3); -fx-padding: 0; -fx-border-insets: 0; -fx-background-insets: 0;");
                    } else {
                        // Estilo normal - sin borde visible, fondo transparente para permitir que el fondo de la tabla se muestre
                        setStyle("-fx-border-color: transparent; -fx-border-width: 1px 0 1px 0; -fx-background-color: transparent; -fx-padding: 0; -fx-border-insets: 0; -fx-background-insets: 0;");
                    }
                    // Asegurar que la fila sea siempre visible y esté correctamente posicionada
                    setVisible(true);
                    setManaged(true);
                    // Forzar actualización del layout para mantener alineación
                    requestLayout();
                } else {
                    setStyle("-fx-border-color: transparent; -fx-border-width: 1px 0 1px 0; -fx-padding: 0; -fx-border-insets: 0; -fx-background-insets: 0;");
                    setVisible(false);
                    setManaged(false);
                }
            }
        });

        // Botones
        addBtn = new Button("Agregar Producto");
        addBtn.setOnAction(e -> addProduct());

        clearBtn = new Button("Vaciar Tabla");
        clearBtn.setOnAction(e -> clearTable());
        clearBtn.getStyleClass().add("danger-button");

        exportBtn = new Button("Exportar a Excel");
        exportBtn.setOnAction(e -> exportToExcel());

        importBtn = new Button("Importar Excel");
        importBtn.setOnAction(e -> importExcel());

        importNewBtn = new Button("Nuevo inventario");
        importNewBtn.setOnAction(e -> importNewExcel());

        loadBackupBtn = new Button("Cargar respaldo");
        loadBackupBtn.setOnAction(e -> loadBackup());

        // Labels (sin productos, unidades vendidas y ganancia de ventas)
        totalProductosLabel = new Label("Total de productos añadidos: 0");
        precioCTotalLabel = new Label("Precio C. total: 0.00");
        precioVTotalLabel = new Label("Precio V. total: 0.00");
        gananciaTotalLabel = new Label("Ganancia Total: 0.00");
        utilidadDiaLabel = new Label("Utilidad del Día: 0.00");

        // ListView de inventarios
        inventoryListView = new ListView<>();
        inventoryListView.setPrefHeight(150);
        inventoryListView.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 1) {
                showProductActions();
            }
        });
    }

    private void setupTable() {
        productoCol.setCellValueFactory(new PropertyValueFactory<>("producto"));
        productoCol.setCellFactory(column -> new TableCell<Product, String>() {
            private final VBox vbox = new VBox();
            private final Label nameLabel = new Label();
            private final Label editLabel = new Label("Editar producto");

            {
                nameLabel.setStyle("-fx-font-weight: bold;");
                editLabel.setStyle("-fx-text-fill: blue; -fx-underline: true; -fx-cursor: hand;");
                editLabel.setVisible(false);
                editLabel.setOnMouseClicked(e -> editSelectedProduct());
                nameLabel.setOnMouseClicked(e -> {
                    if (currentEditLabel != null && currentEditLabel != editLabel) {
                        currentEditLabel.setVisible(false);
                    }
                    editLabel.setVisible(!editLabel.isVisible());
                    currentEditLabel = editLabel.isVisible() ? editLabel : null;
                });
                vbox.getChildren().addAll(nameLabel, editLabel);
                // Ensure the VBox always has proper layout
                vbox.setFillWidth(true);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    nameLabel.setText("");
                    editLabel.setVisible(false);
                } else {
                    // Get the product from the row value instead of using getIndex()
                    Product product = getTableRow().getItem();
                    if (product != null) {
                        String displayText = product.getProducto();
                        if (!"Sin unidad de venta".equals(product.getUnidadVenta())) {
                            displayText += " " + product.getUnidadVenta();
                        }
                        nameLabel.setText(displayText);
                        setGraphic(vbox);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        productoCol.setOnEditCommit(e -> {
            e.getRowValue().setProducto(e.getNewValue());
            updateTotals();
        });

        eliminarCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Eliminar");
            {
                btn.setOnAction(e -> {
                    Product p = getTableView().getItems().get(getIndex());
                    products.remove(p);
                    updateTotals();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        invInicialCol.setCellValueFactory(data -> data.getValue().invInicialProperty().asObject());
        invInicialCol.setCellFactory(TextFieldTableCell.forTableColumn(new DecimalStringConverter()));
        invInicialCol.setOnEditCommit(e -> {
            e.getRowValue().setInvInicial(e.getNewValue());
            updateTotals();
        });

        entradaCol.setCellValueFactory(data -> data.getValue().entradaProperty().asObject());
        entradaCol.setCellFactory(TextFieldTableCell.forTableColumn(new DecimalStringConverter()));
        entradaCol.setOnEditCommit(e -> {
            e.getRowValue().setEntrada(e.getNewValue());
            updateTotals();
        });

        ventaCol.setCellValueFactory(data -> data.getValue().ventaProperty().asObject());
        ventaCol.setCellFactory(TextFieldTableCell.forTableColumn(new DecimalStringConverter()));
        ventaCol.setOnEditCommit(e -> {
            e.getRowValue().setVenta(e.getNewValue());
            updateTotals();
        });

        mermaCol.setCellValueFactory(data -> data.getValue().mermaProperty().asObject());
        mermaCol.setCellFactory(TextFieldTableCell.forTableColumn(new DecimalStringConverter()));
        mermaCol.setOnEditCommit(e -> {
            e.getRowValue().setMerma(e.getNewValue());
            updateTotals();
        });

        invFinalCol.setCellValueFactory(data -> data.getValue().invFinalProperty().asObject());
        invFinalCol.setCellFactory(TextFieldTableCell.forTableColumn(new DecimalStringConverter()));
        invFinalCol.setOnEditCommit(e -> {
            e.getRowValue().setInvFinal(e.getNewValue());
            // Automatically calculate sales when final inventory changes
            // Formula: venta = inv_inicial + entrada - merma - inv_final
            double invInicial = e.getRowValue().getInvInicial();
            double entrada = e.getRowValue().getEntrada();
            double merma = e.getRowValue().getMerma();
            double invFinal = e.getNewValue();
            double calculatedVenta = invInicial + entrada - merma - invFinal;
            // Ensure venta is not negative
            calculatedVenta = Math.max(0, calculatedVenta);
            e.getRowValue().setVenta(calculatedVenta);
            updateTotals();
            // Refresh table to show updated venta value
            table.refresh();
        });

        precioCCol.setCellValueFactory(data -> data.getValue().precioCProperty().asObject());
        precioCCol.setCellFactory(TextFieldTableCell.forTableColumn(new DecimalStringConverter()));
        precioCCol.setOnEditCommit(e -> {
            e.getRowValue().setPrecioC(e.getNewValue());
            updateTotals();
        });

        precioVCol.setCellValueFactory(data -> data.getValue().precioVProperty().asObject());
        precioVCol.setCellFactory(TextFieldTableCell.forTableColumn(new DecimalStringConverter()));
        precioVCol.setOnEditCommit(e -> {
            e.getRowValue().setPrecioV(e.getNewValue());
            updateTotals();
        });

        valorInvInicialCol.setCellValueFactory(data -> data.getValue().valorInvInicialProperty().asObject());
        utilidadUnidadCol.setCellValueFactory(data -> data.getValue().utilidadUnidadProperty().asObject());
        utilidadVentaCol.setCellValueFactory(data -> data.getValue().utilidadVentaProperty().asObject());
    }

    private void addProduct() {
        // Crear el diálogo modal
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Añadir Producto");
        dialog.setHeaderText("Ingrese los datos del nuevo producto");

        // Crear los campos del formulario
        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre del producto");

        TextField cantidadField = new TextField();
        cantidadField.setPromptText("Cantidad/Stock");

        TextField precioCField = new TextField();
        precioCField.setPromptText("Precio de compra");

        TextField precioVField = new TextField();
        precioVField.setPromptText("Precio de venta");

        TextField descripcionField = new TextField();
        descripcionField.setPromptText("Descripción (opcional)");

        ComboBox<String> unidadComboBox = new ComboBox<>();
        unidadComboBox.getItems().addAll("Unidades", "Libras", "Kg", "Litros", "Paquetes", "libras", "Sin unidad de venta", "Otro tipo de unidad…");

        // Determinar la unidad inicial basada en el nombre del producto
        String initialUnit = determinarUnidadVenta(null, nombreField.getText().trim(), unidadesSistema);
        unidadComboBox.setValue(initialUnit);

        // Listener para manejar selección de "Otro tipo de unidad…"
        unidadComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if ("Otro tipo de unidad…".equals(newVal)) {
                TextInputDialog inputDialog = new TextInputDialog();
                inputDialog.setTitle("Unidad personalizada");
                inputDialog.setHeaderText("Ingrese la unidad de venta personalizada");
                inputDialog.setContentText("Unidad:");
                Optional<String> result = inputDialog.showAndWait();
                if (result.isPresent() && !result.get().trim().isEmpty()) {
                    String customUnit = result.get().trim();
                    unidadComboBox.getItems().add(customUnit);
                    unidadComboBox.setValue(customUnit);
                } else {
                    unidadComboBox.setValue(oldVal);
                }
            }
        });

        // Crear labels para mensajes de error
        Label nombreErrorLabel = new Label();
        nombreErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11;");
        nombreErrorLabel.setVisible(false);

        Label cantidadErrorLabel = new Label();
        cantidadErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11;");
        cantidadErrorLabel.setVisible(false);

        Label precioCErrorLabel = new Label();
        precioCErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11;");
        precioCErrorLabel.setVisible(false);

        Label precioVErrorLabel = new Label();
        precioVErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11;");
        precioVErrorLabel.setVisible(false);

        Label entradaErrorLabel = new Label();
        entradaErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11;");
        entradaErrorLabel.setVisible(false);

        // Validación en tiempo real
        nombreField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                nombreField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                nombreErrorLabel.setText("Campo obligatorio");
                nombreErrorLabel.setVisible(true);
            } else {
                nombreField.setStyle("");
                nombreErrorLabel.setVisible(false);
            }
        });

        cantidadField.textProperty().addListener((obs, oldVal, newVal) -> {
            String normalized = newVal.trim().replace(',', '.');
            if (!normalized.isEmpty()) {
                try {
                    Double.parseDouble(normalized);
                    cantidadField.setStyle("");
                    cantidadErrorLabel.setVisible(false);
                } catch (NumberFormatException e) {
                    cantidadField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                    cantidadErrorLabel.setText("Debe ser un número válido");
                    cantidadErrorLabel.setVisible(true);
                }
            } else {
                cantidadField.setStyle("");
                cantidadErrorLabel.setVisible(false);
            }
        });

        precioCField.textProperty().addListener((obs, oldVal, newVal) -> {
            String normalized = newVal.trim().replace(',', '.');
            if (!normalized.isEmpty()) {
                try {
                    Double.parseDouble(normalized);
                    precioCField.setStyle("");
                    precioCErrorLabel.setVisible(false);
                } catch (NumberFormatException e) {
                    precioCField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                    precioCErrorLabel.setText("Debe ser un número válido");
                    precioCErrorLabel.setVisible(true);
                }
            } else {
                precioCField.setStyle("");
                precioCErrorLabel.setVisible(false);
            }
        });

        precioVField.textProperty().addListener((obs, oldVal, newVal) -> {
            String normalized = newVal.trim().replace(',', '.');
            if (!normalized.isEmpty()) {
                try {
                    Double.parseDouble(normalized);
                    precioVField.setStyle("");
                    precioVErrorLabel.setVisible(false);
                } catch (NumberFormatException e) {
                    precioVField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                    precioVErrorLabel.setText("Debe ser un número válido");
                    precioVErrorLabel.setVisible(true);
                }
            } else {
                precioVField.setStyle("");
                precioVErrorLabel.setVisible(false);
            }
        });

        TextField entradaField = new TextField();
        entradaField.setPromptText("Cantidad de entrada");
        entradaField.textProperty().addListener((obs, oldVal, newVal) -> {
            String normalized = newVal.trim().replace(',', '.');
            if (!normalized.isEmpty()) {
                try {
                    Double.parseDouble(normalized);
                    entradaField.setStyle("");
                    entradaErrorLabel.setVisible(false);
                } catch (NumberFormatException e) {
                    entradaField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                    entradaErrorLabel.setText("Debe ser un número válido");
                    entradaErrorLabel.setVisible(true);
                }
            } else {
                entradaField.setStyle("");
                entradaErrorLabel.setVisible(false);
            }
        });

        // Crear el layout del formulario
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Nombre del producto:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(nombreErrorLabel, 2, 0);
        grid.add(new Label("Cantidad / Stock:"), 0, 1);
        grid.add(cantidadField, 1, 1);
        grid.add(cantidadErrorLabel, 2, 1);
        grid.add(new Label("Precio C:"), 0, 2);
        grid.add(precioCField, 1, 2);
        grid.add(precioCErrorLabel, 2, 2);
        grid.add(new Label("Precio V:"), 0, 3);
        grid.add(precioVField, 1, 3);
        grid.add(precioVErrorLabel, 2, 3);
        grid.add(new Label("Entrada:"), 0, 4);
        grid.add(entradaField, 1, 4);
        grid.add(entradaErrorLabel, 2, 4);
        grid.add(new Label("Descripción:"), 0, 5);
        grid.add(descripcionField, 1, 5);
        grid.add(new Label("Cómo se venderá:"), 0, 6);
        grid.add(unidadComboBox, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // Botones del diálogo
        ButtonType confirmarButtonType = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelarButtonType = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmarButtonType, cancelarButtonType);

        // Obtener el botón confirmar para agregar event filter
        Button confirmarButton = (Button) dialog.getDialogPane().lookupButton(confirmarButtonType);
        confirmarButton.addEventFilter(ActionEvent.ACTION, event -> {
            // Reset field styles
            nombreField.setStyle("");
            cantidadField.setStyle("");
            precioCField.setStyle("");
            precioVField.setStyle("");
            entradaField.setStyle("");

            String nombre = capitalizeFirstLetter(nombreField.getText().trim());
            String cantidadText = cantidadField.getText().trim();
            String precioCText = precioCField.getText().trim();
            String precioVText = precioVField.getText().trim();
            String entradaText = entradaField.getText().trim();
            String descripcion = descripcionField.getText().trim();
            String unidad = unidadComboBox.getValue();

            boolean hasWarnings = false;
            StringBuilder warnings = new StringBuilder("Algunos campos están vacíos o inválidos:\n");

            if (nombre.isEmpty()) {
                nombreField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                warnings.append("- Nombre del producto está vacío.\n");
                hasWarnings = true;
            }

            double cantidad = 0.0;
            try {
                cantidad = Double.parseDouble(cantidadText.replace(',', '.'));
            } catch (NumberFormatException e) {
                if (!cantidadText.isEmpty()) {
                    cantidadField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                    warnings.append("- Cantidad no es un número válido.\n");
                    hasWarnings = true;
                }
            }

            double precioC = 0.0;
            try {
                precioC = Double.parseDouble(precioCText.replace(',', '.'));
            } catch (NumberFormatException e) {
                if (!precioCText.isEmpty()) {
                    precioCField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                    warnings.append("- Precio de compra no es un número válido.\n");
                    hasWarnings = true;
                }
            }

            double precioV = 0.0;
            try {
                precioV = Double.parseDouble(precioVText.replace(',', '.'));
            } catch (NumberFormatException e) {
                if (!precioVText.isEmpty()) {
                    precioVField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                    warnings.append("- Precio de venta no es un número válido.\n");
                    hasWarnings = true;
                }
            }

            double entrada = 0.0;
            try {
                entrada = Double.parseDouble(entradaText.replace(',', '.'));
            } catch (NumberFormatException e) {
                if (!entradaText.isEmpty()) {
                    entradaField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                    warnings.append("- Entrada no es un número válido.\n");
                    hasWarnings = true;
                }
            }

            if (cantidadText.isEmpty()) {
                cantidadField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                warnings.append("- Cantidad está vacía.\n");
                hasWarnings = true;
            }

            if (precioCText.isEmpty()) {
                precioCField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                warnings.append("- Precio de compra está vacío.\n");
                hasWarnings = true;
            }

            if (precioVText.isEmpty()) {
                precioVField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                warnings.append("- Precio de venta está vacío.\n");
                hasWarnings = true;
            }

            if (hasWarnings) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Advertencia");
                confirmAlert.setHeaderText("Algunos campos están vacíos o inválidos");
                confirmAlert.setContentText(warnings.toString() + "\n\n¿Desea continuar de todos modos?");

                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (!(result.isPresent() && result.get() == ButtonType.OK)) {
                    event.consume(); // No cerrar el diálogo si el usuario cancela la advertencia
                    return;
                }
            }

            // Check for existing product (same name and unit)
            Product existingProduct = products.stream()
                .filter(p -> p.getProducto().equalsIgnoreCase(nombre) && p.getUnidadVenta().equals(unidad))
                .findFirst()
                .orElse(null);

            if (existingProduct != null) {
                // Show dialog for existing product options
                Alert existingAlert = new Alert(Alert.AlertType.CONFIRMATION);
                existingAlert.setTitle("Producto existente");
                existingAlert.setHeaderText("El producto '" + nombre + "' ya existe.");
                existingAlert.setContentText("¿Qué deseas hacer?");

                ButtonType mergeButton = new ButtonType("Combinar");
                ButtonType replaceButton = new ButtonType("Reemplazar");
                ButtonType cancelButton = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

                existingAlert.getButtonTypes().setAll(mergeButton, replaceButton, cancelButton);

                Optional<ButtonType> result = existingAlert.showAndWait();
                if (result.isPresent()) {
                    if (result.get() == mergeButton) {
                        // Merge: add quantities to existing product
                        existingProduct.setInvInicial(existingProduct.getInvInicial() + cantidad);
                        existingProduct.setEntrada(existingProduct.getEntrada() + entrada);
                        existingProduct.recalcular();
                        event.consume(); // No cerrar el diálogo, solo actualizar
                        return;
                    } else if (result.get() == replaceButton) {
                        // Replace: update all fields of existing product
                        existingProduct.setInvInicial(cantidad);
                        existingProduct.setEntrada(entrada);
                        existingProduct.setVenta(0);
                        existingProduct.setMerma(0);
                        existingProduct.setPrecioC(precioC);
                        existingProduct.setPrecioV(precioV);
                        existingProduct.setDescripcion(descripcion);
                        existingProduct.setUnidadVenta(unidad);
                        existingProduct.recalcular();
                        event.consume(); // No cerrar el diálogo, solo actualizar
                        return;
                    } else {
                        // Cancel
                        event.consume();
                        return;
                    }
                } else {
                    event.consume();
                    return;
                }
            }

            // Si todo está bien, permitir que el diálogo se cierre
        });

        // Resultado del diálogo
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmarButtonType) {
                String nombre = capitalizeFirstLetter(nombreField.getText().trim());
                String cantidadText = cantidadField.getText().trim();
                String precioCText = precioCField.getText().trim();
                String precioVText = precioVField.getText().trim();
                String entradaText = entradaField.getText().trim();
                String descripcion = descripcionField.getText().trim();
                String unidad = unidadComboBox.getValue();

                double cantidad = 0.0;
                try {
                    cantidad = Double.parseDouble(cantidadText.replace(',', '.'));
                } catch (NumberFormatException e) {
                    // Dejar en 0.0 si vacío
                }

                double precioC = 0.0;
                try {
                    precioC = Double.parseDouble(precioCText.replace(',', '.'));
                } catch (NumberFormatException e) {
                    // Dejar en 0.0 si vacío
                }

                double precioV = 0.0;
                try {
                    precioV = Double.parseDouble(precioVText.replace(',', '.'));
                } catch (NumberFormatException e) {
                    // Dejar en 0.0 si vacío
                }

                double entrada = 0.0;
                try {
                    entrada = Double.parseDouble(entradaText.replace(',', '.'));
                } catch (NumberFormatException e) {
                    // Dejar en 0 si vacío
                }

                return new Product(nombre, cantidad, entrada, 0, 0, precioC, precioV, descripcion, unidad);
            }
            return null;
        });

        // Mostrar el diálogo y procesar el resultado
        dialog.showAndWait().ifPresent(product -> {
            products.add(product);
            updateTotals();
        });
    }

    private void clearTable() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar acción");
        alert.setHeaderText("¿Está seguro de que desea vaciar la tabla?");
        alert.setContentText("Esta acción no se puede deshacer.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                products.clear();
                updateTotals();
            }
        });
    }

    private void exportToExcel() {
        try {
            File file = excelService.exportToExcel(products);
            fileTrackingService.addExportedFile(file.getAbsolutePath());
            updateInventoryListView();
            showInfo("Exportación exitosa", "Archivo exportado: " + file.getName());
        } catch (Exception e) {
            showError("Error al exportar", e.getMessage());
        }
    }

    private void importExcel() {
        File file = excelService.chooseFile();
        if (file != null) {
            try {
                products.clear();
                products.addAll(excelService.importFromExcel(file));
                updateTotals();
                showInfo("Importación exitosa", "Datos importados correctamente.");
            } catch (Exception e) {
                showError("Error al importar", e.getMessage());
            }
        }
    }

    private void importNewExcel() {
        File file = excelService.chooseFile();
        if (file != null) {
            try {
                excelService.importNewExcel(file, products);
                updateTotals();
                showInfo("Importación exitosa", "Nuevo Excel importado correctamente.");
            } catch (Exception e) {
                showError("Error al importar", e.getMessage());
            }
        }
    }

    private void openInventoryFile() {
        String selectedFile = inventoryListView.getSelectionModel().getSelectedItem();
        if (selectedFile != null) {
            try {
                String filePath = fileTrackingService.getFilePathFromDisplay(selectedFile);
                // Abrir el archivo Excel y cargar los datos en una nueva ventana
                openInventoryInNewWindow(filePath);
            } catch (Exception e) {
                showError("Error al abrir inventario", e.getMessage());
            }
        }
    }

    private void openInventoryInNewWindow(String filePath) {
        try {
            // Importar los datos del archivo Excel
            List<Product> inventoryProducts = excelService.importFromExcel(new File(filePath));

            // Crear una nueva ventana para mostrar el inventario
            Stage inventoryStage = new Stage();
            inventoryStage.setTitle("Inventario: " + new File(filePath).getName());

            // Crear una nueva tabla para mostrar los productos del inventario
            TableView<Product> inventoryTable = new TableView<>();
            inventoryTable.setEditable(false);

            // Crear las mismas columnas que la tabla principal
            TableColumn<Product, String> productoCol = new TableColumn<>("Producto");
            productoCol.setCellValueFactory(new PropertyValueFactory<>("producto"));
            productoCol.setPrefWidth(200);

            TableColumn<Product, Double> invInicialCol = new TableColumn<>("Inv. Inicial");
            invInicialCol.setCellValueFactory(data -> data.getValue().invInicialProperty().asObject());
            invInicialCol.setPrefWidth(120);

            TableColumn<Product, Double> entradaCol = new TableColumn<>("Entrada");
            entradaCol.setCellValueFactory(data -> data.getValue().entradaProperty().asObject());
            entradaCol.setPrefWidth(100);

            TableColumn<Product, Double> ventaCol = new TableColumn<>("Venta");
            ventaCol.setCellValueFactory(data -> data.getValue().ventaProperty().asObject());
            ventaCol.setPrefWidth(100);

            TableColumn<Product, Double> mermaCol = new TableColumn<>("Merma");
            mermaCol.setCellValueFactory(data -> data.getValue().mermaProperty().asObject());
            mermaCol.setPrefWidth(100);

            TableColumn<Product, Double> invFinalCol = new TableColumn<>("Inv. Final");
            invFinalCol.setCellValueFactory(data -> data.getValue().invFinalProperty().asObject());
            invFinalCol.setPrefWidth(120);

            TableColumn<Product, Double> precioCCol = new TableColumn<>("Precio C.");
            precioCCol.setCellValueFactory(data -> data.getValue().precioCProperty().asObject());
            precioCCol.setPrefWidth(120);

            TableColumn<Product, Double> precioVCol = new TableColumn<>("Precio V.");
            precioVCol.setCellValueFactory(data -> data.getValue().precioVProperty().asObject());
            precioVCol.setPrefWidth(120);

            TableColumn<Product, Double> utilidadUnidadCol = new TableColumn<>("Utilidad Unidad");
            utilidadUnidadCol.setCellValueFactory(data -> data.getValue().utilidadUnidadProperty().asObject());
            utilidadUnidadCol.setPrefWidth(140);

            TableColumn<Product, Double> utilidadVentaCol = new TableColumn<>("Utilidad Venta");
            utilidadVentaCol.setCellValueFactory(data -> data.getValue().utilidadVentaProperty().asObject());
            utilidadVentaCol.setPrefWidth(140);

            inventoryTable.getColumns().addAll(productoCol, invInicialCol, entradaCol, ventaCol,
                    mermaCol, invFinalCol, precioCCol, precioVCol, utilidadUnidadCol, utilidadVentaCol);
            inventoryTable.setItems(FXCollections.observableArrayList(inventoryProducts));

            // Calcular totales para este inventario (sin los campos ignorados)
            double totalPrecioC = inventoryProducts.stream().mapToDouble(p -> (p.getInvInicial() + p.getEntrada()) * p.getPrecioC()).sum();
            double totalPrecioV = inventoryProducts.stream().mapToDouble(p -> (p.getInvInicial() + p.getEntrada()) * p.getPrecioV()).sum();
            double gananciaTotal = totalPrecioV - totalPrecioC;
            double utilidadGananciasTotal = inventoryProducts.stream().mapToDouble(Product::getUtilidadUnidad).sum();

            // Crear labels para los totales
            Label precioCTotalLabel = new Label("Precio C. total: " + String.format("%.2f", totalPrecioC));
            Label precioVTotalLabel = new Label("Precio V. total: " + String.format("%.2f", totalPrecioV));
            Label gananciaTotalLabel = new Label("Ganancia Total: " + String.format("%.2f", gananciaTotal));

            VBox inventoryVBox = new VBox(10);
            inventoryVBox.setStyle("-fx-padding: 20;");
            inventoryVBox.getChildren().add(inventoryTable);

            VBox totalsBox = new VBox(5);
            totalsBox.getChildren().addAll(precioCTotalLabel, precioVTotalLabel, gananciaTotalLabel);
            inventoryVBox.getChildren().add(totalsBox);

            Scene inventoryScene = new Scene(inventoryVBox, 1200, 600);
            inventoryScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            inventoryStage.setScene(inventoryScene);
            inventoryStage.show();

        } catch (Exception e) {
            showError("Error al cargar inventario", e.getMessage());
        }
    }

    private void updateTotals() {
        totalProductosLabel.setText("Total de productos añadidos: " + products.size());

        double precioCompraTotal = products.stream().mapToDouble(p -> (p.getInvInicial() + p.getEntrada()) * p.getPrecioC()).sum();
        double precioVentaTotal = products.stream().mapToDouble(p -> (p.getInvInicial() + p.getEntrada()) * p.getPrecioV()).sum();
        double utilidadTotal = products.stream().mapToDouble(Product::getUtilidadVenta).sum();
        double gananciaTotal = precioVentaTotal - precioCompraTotal;
        double utilidadGananciasTotal = products.stream().mapToDouble(p -> p.getUtilidadUnidad() * p.getInvFinal()).sum();

        precioCTotalLabel.setText("Precio C. total: " + String.format("%.2f", precioCompraTotal));
        precioVTotalLabel.setText("Precio V. total: " + String.format("%.2f", precioVentaTotal));
        gananciaTotalLabel.setText("Ganancia Total: " + String.format("%.2f", gananciaTotal));
        utilidadDiaLabel.setText("Utilidad del Día: " + String.format("%.2f", utilidadTotal));

        updateInventoryListView();
    }

    private void updateInventoryListView() {
        List<String> productsWithZeroInventory = products.stream()
            .filter(p -> p.getInvFinal() == 0)
            .map(Product::getProducto)
            .collect(Collectors.toList());
        inventoryListView.setItems(FXCollections.observableArrayList(productsWithZeroInventory));
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

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadBackup() {
        try {
            File latestBackup = autoSaveService.getLatestBackup();
            if (latestBackup == null) {
                showError("Cargar Respaldo", "No se encontró ningún archivo de respaldo.");
                return;
            }

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Cargar Respaldo");
            confirmAlert.setHeaderText("¿Cargar respaldo?");
            confirmAlert.setContentText("Se cargará el respaldo más reciente: " + latestBackup.getName() + "\n\nLos datos actuales se perderán. ¿Continuar?");

            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                autoSaveService.loadBackup(latestBackup, products);
                table.refresh();
                updateTotals();
                updateInventoryListView();
                showInfo("Cargar Respaldo", "Respaldo cargado exitosamente desde: " + latestBackup.getName());
            }

        } catch (Exception e) {
            showError("Error", "Error al cargar el respaldo: " + e.getMessage());
        }
    }

    private void analyzeProfitDiscrepancy() {
        ProfitDiscrepancyDialogController dialogController = new ProfitDiscrepancyDialogController(products);
        dialogController.showDialog();
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    private void showProductActions() {
        String selectedFile = inventoryListView.getSelectionModel().getSelectedItem();
        if (selectedFile != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Acciones para " + selectedFile);
            alert.setHeaderText("¿Qué acción deseas realizar?");

            ButtonType abrirButton = new ButtonType("Abrir");
            ButtonType eliminarButton = new ButtonType("Eliminar");
            ButtonType cancelarButton = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(abrirButton, eliminarButton, cancelarButton);

            alert.showAndWait().ifPresent(button -> {
                if (button == abrirButton) {
                    openInventoryFile();
                } else if (button == eliminarButton) {
                    showError("No implementado", "Eliminar archivo no está implementado aún.");
                }
            });
        }
    }

    private void editSelectedProduct() {
        Product selectedProduct = table.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            return;
        }

        // Crear el diálogo modal para editar
        Dialog<Product> editDialog = new Dialog<>();
        editDialog.setTitle("Editar Producto");
        editDialog.setHeaderText("Modifique los datos del producto");

        // Crear los campos del formulario con valores pre-llenados
        TextField nombreField = new TextField(selectedProduct.getProducto());
        nombreField.setPromptText("Nombre del producto");

        TextField cantidadField = new TextField(String.valueOf(selectedProduct.getInvInicial()));
        cantidadField.setPromptText("Cantidad/Stock");

        TextField precioCField = new TextField(String.valueOf(selectedProduct.getPrecioC()));
        precioCField.setPromptText("Precio de compra");

        TextField precioVField = new TextField(String.valueOf(selectedProduct.getPrecioV()));
        precioVField.setPromptText("Precio de venta");

        TextField descripcionField = new TextField(selectedProduct.getDescripcion());
        descripcionField.setPromptText("Descripción (opcional)");

        ComboBox<String> unidadComboBox = new ComboBox<>();
        unidadComboBox.getItems().addAll("Unidades", "Libras", "Kg", "Litros", "Paquetes", "libras", "Sin unidad de venta", "Otro tipo de unidad…");

        // Determinar la unidad inicial
        String initialUnit = determinarUnidadVenta(selectedProduct.getUnidadVenta(), selectedProduct.getProducto(), unidadesSistema);
        unidadComboBox.setValue(initialUnit);

        // Listener para auto-detectar unidad basada en el nombre del producto
        nombreField.textProperty().addListener((obs, oldVal, newVal) -> {
            String detectedUnit = determinarUnidadVenta(null, newVal.trim(), unidadesSistema);
            unidadComboBox.setValue(detectedUnit);
        });

        // Listener para manejar selección de "Otro tipo de unidad…"
        unidadComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if ("Otro tipo de unidad…".equals(newVal)) {
                TextInputDialog inputDialog = new TextInputDialog();
                inputDialog.setTitle("Unidad personalizada");
                inputDialog.setHeaderText("Ingrese la unidad de venta personalizada");
                inputDialog.setContentText("Unidad:");
                Optional<String> result = inputDialog.showAndWait();
                if (result.isPresent() && !result.get().trim().isEmpty()) {
                    String customUnit = result.get().trim();
                    unidadComboBox.getItems().add(customUnit);
                    unidadComboBox.setValue(customUnit);
                } else {
                    unidadComboBox.setValue(oldVal);
                }
            }
        });

        // Crear el layout del formulario
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Nombre del producto:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Cantidad / Stock:"), 0, 1);
        grid.add(cantidadField, 1, 1);
        grid.add(new Label("Precio C:"), 0, 2);
        grid.add(precioCField, 1, 2);
        grid.add(new Label("Precio V:"), 0, 3);
        grid.add(precioVField, 1, 3);
        grid.add(new Label("Descripción:"), 0, 4);
        grid.add(descripcionField, 1, 4);
        grid.add(new Label("Cómo se venderá:"), 0, 5);
        grid.add(unidadComboBox, 1, 5);

        editDialog.getDialogPane().setContent(grid);

        // Botones del diálogo
        ButtonType confirmarButtonType = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelarButtonType = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        editDialog.getDialogPane().getButtonTypes().addAll(confirmarButtonType, cancelarButtonType);

        // Validación de campos
        Button confirmarButton = (Button) editDialog.getDialogPane().lookupButton(confirmarButtonType);
        confirmarButton.addEventFilter(ActionEvent.ACTION, event -> {
            String nombre = nombreField.getText().trim();
            String cantidadText = cantidadField.getText().trim().replace(',', '.');
            String precioCText = precioCField.getText().trim().replace(',', '.');
            String precioVText = precioVField.getText().trim().replace(',', '.');

            // Validaciones
            if (nombre.isEmpty()) {
                showError("Error de validación", "El nombre del producto es obligatorio.");
                event.consume();
                return;
            }

            try {
                Double.parseDouble(cantidadText);
            } catch (NumberFormatException e) {
                showError("Error de validación", "La cantidad debe ser un número válido.");
                event.consume();
                return;
            }

            try {
                Double.parseDouble(precioCText);
            } catch (NumberFormatException e) {
                showError("Error de validación", "El precio de compra debe ser un número válido.");
                event.consume();
                return;
            }

            try {
                Double.parseDouble(precioVText);
            } catch (NumberFormatException e) {
                showError("Error de validación", "El precio de venta debe ser un número válido.");
                event.consume();
                return;
            }
        });

        // Resultado del diálogo
        editDialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmarButtonType) {
                String nombre = capitalizeFirstLetter(nombreField.getText().trim());
                double cantidad = Double.parseDouble(cantidadField.getText().trim().replace(',', '.'));
                double precioC = Double.parseDouble(precioCField.getText().trim().replace(',', '.'));
                double precioV = Double.parseDouble(precioVField.getText().trim().replace(',', '.'));
                String descripcion = descripcionField.getText().trim();
                String unidad = unidadComboBox.getValue();

                // Actualizar el producto existente
                selectedProduct.setProducto(nombre);
                selectedProduct.setInvInicial(cantidad);
                selectedProduct.setPrecioC(precioC);
                selectedProduct.setPrecioV(precioV);
                selectedProduct.setDescripcion(descripcion);
                selectedProduct.setUnidadVenta(unidad);
                selectedProduct.recalcular();

                return selectedProduct;
            }
            return null;
        });

        // Mostrar el diálogo y procesar el resultado
        editDialog.showAndWait().ifPresent(product -> {
            table.refresh();
            updateTotals();
        });
    }

    private String determinarUnidadVenta(String currentUnit, String productName, List<String> unidadesSistema) {
        // If currentUnit is not null and not empty, return it
        if (currentUnit != null && !currentUnit.trim().isEmpty()) {
            return currentUnit;
        }

        // If productName is null or empty, return default
        if (productName == null || productName.trim().isEmpty()) {
            return "Sin unidad de venta";
        }

        // Normalize productName to lower case
        String lowerName = productName.toLowerCase();

        // Check for specific keywords and map to units
        if (lowerName.contains("libra") || lowerName.contains("lb")) {
            return "Libras";
        } else if (lowerName.contains("kg") || lowerName.contains("kilo")) {
            return "Kg";
        } else if (lowerName.contains("litro") || lowerName.contains("l")) {
            return "Litros";
        } else if (lowerName.contains("paquete") || lowerName.contains("pack")) {
            return "Paquetes";
        } else if (lowerName.contains("unidad") || lowerName.contains("pieza")) {
            return "Unidades";
        } else {
            return "Sin unidad de venta";
        }
    }

    private double parseDoubleSafe(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        String normalized = text.trim().replace(',', '.');
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int parseIntSafe(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        String normalized = text.trim().replace(',', '.');
        try {
            return Integer.parseInt(normalized);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

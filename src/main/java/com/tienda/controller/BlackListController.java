package com.tienda.controller;

import com.tienda.BlackListEntry;
import com.tienda.Product;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class BlackListController {

    private TableView<BlackListEntry> table;
    private ObservableList<BlackListEntry> blackListEntries = FXCollections.observableArrayList();
    private ObservableList<Product> availableProducts;

    private TextField searchField;
    private VBox detailsPanel;
    private boolean detailsVisible = false;

    private static final String BLACKLIST_FOLDER = "TiendaAPP";
    private static final String BLACKLIST_FILE = "blacklist.dat";

    /** Conéctalo con tu botón real de tema */
    private static boolean isDarkTheme = false;

    // =====================================================
    //                    CONSTRUCTOR
    // =====================================================
    public BlackListController(ObservableList<Product> products) {
        this.availableProducts = products;
        loadBlackList();
    }

    // =====================================================
    //                VENTANA PRINCIPAL
    // =====================================================
    public void showBlackListWindow() {

        Stage stage = new Stage();
        stage.setTitle("Lista Negra");
        stage.initModality(Modality.APPLICATION_MODAL);

        searchField = new TextField();
        searchField.setPromptText("Buscar por nombre...");

        initializeTable();

        FilteredList<BlackListEntry> filtered =
                new FilteredList<>(blackListEntries, p -> true);

        searchField.textProperty().addListener((obs, o, txt) -> {
            filtered.setPredicate(entry -> {
                if (txt == null || txt.isBlank()) return true;
                return entry.getNombre().toLowerCase().contains(txt.toLowerCase());
            });
        });

        table.setItems(filtered);

        Button addBtn = new Button("Añadir");
        addBtn.setOnAction(e -> addEntry());

        Button editBtn = new Button("Editar");
        editBtn.setOnAction(e -> editEntry());

        Button deleteBtn = new Button("Eliminar");
        deleteBtn.setOnAction(e -> deleteEntry());

        HBox buttons = new HBox(10, addBtn, editBtn, deleteBtn);

        detailsPanel = new VBox(8);
        detailsPanel.setPadding(new Insets(10));
        detailsPanel.setVisible(false);
        detailsPanel.setManaged(false);
        detailsPanel.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-border-radius: 5;");

        VBox layout = new VBox(10, searchField, table, detailsPanel, buttons);
        layout.setPadding(new Insets(15));

        Scene scene = new Scene(layout, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/dark.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }

    // =====================================================
    //                      TABLA
    // =====================================================
    private void initializeTable() {

        table = new TableView<>();

        TableColumn<BlackListEntry, String> nombreCol =
                new TableColumn<>("Nombre");
        nombreCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNombre()));
        nombreCol.setPrefWidth(200);

        TableColumn<BlackListEntry, LocalDate> diaCol =
                new TableColumn<>("Día");
        diaCol.setCellValueFactory(d ->
                new SimpleObjectProperty<>(d.getValue().getDia()));
        diaCol.setPrefWidth(120);

        TableColumn<BlackListEntry, Double> totalCol =
                new TableColumn<>("Importe");
        totalCol.setCellValueFactory(d ->
                new SimpleDoubleProperty(d.getValue().getImporteTotal()).asObject());
        totalCol.setPrefWidth(120);

        table.getColumns().addAll(nombreCol, diaCol, totalCol);

        // ⭐ SELECCIÓN DE FILA
        table.setRowFactory(tv -> {
            TableRow<BlackListEntry> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (row.getItem() != null) {
                    table.getSelectionModel().select(row.getItem());
                    showDetails(row.getItem());
                }
            });
            return row;
        });
    }



    // =====================================================
    //                 MOSTRAR DETALLES
    // =====================================================
    private void showDetails(BlackListEntry entry) {
        detailsPanel.getChildren().clear();

        Label title = new Label("Productos pendientes");
        title.setStyle("-fx-font-weight:bold;");

        TableView<BlackListEntry.BlackListItem> prodTable = new TableView<>();
        prodTable.setPrefHeight(150);

        TableColumn<BlackListEntry.BlackListItem, String> prod =
                new TableColumn<>("Producto");
        prod.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getProducto()));

        TableColumn<BlackListEntry.BlackListItem, Double> cant =
                new TableColumn<>("Cantidad");
        cant.setCellValueFactory(d ->
                new SimpleDoubleProperty(d.getValue().getCantidad()).asObject());

        TableColumn<BlackListEntry.BlackListItem, Double> precio =
                new TableColumn<>("Precio");
        precio.setCellValueFactory(d ->
                new SimpleDoubleProperty(d.getValue().getPrecioUnitario()).asObject());

        TableColumn<BlackListEntry.BlackListItem, Double> sub =
                new TableColumn<>("Subtotal");
        sub.setCellValueFactory(d ->
                new SimpleDoubleProperty(d.getValue().getSubtotal()).asObject());

        prodTable.getColumns().addAll(prod, cant, precio, sub);
        prodTable.getItems().addAll(entry.getProductos());

        Label total = new Label("Total: $" + entry.getImporteTotal());
        total.setStyle("-fx-font-weight:bold;");

        detailsPanel.getChildren().addAll(title, prodTable, total);

        if (!detailsVisible) {
            detailsPanel.setVisible(true);
            detailsPanel.setManaged(true);
            detailsPanel.setOpacity(0.0);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), detailsPanel);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

            detailsVisible = true;
        }
    }

    // =====================================================
    //                    AÑADIR
    // =====================================================
    private void addEntry() {

        Dialog<BlackListEntry> dialog = crearDialogo("Añadir a Lista Negra", null);

        dialog.showAndWait().ifPresent(entry -> {
            blackListEntries.add(entry);
            saveBlackList();
        });
    }

    // =====================================================
    //                     EDITAR
    // =====================================================
    private void editEntry() {

        BlackListEntry selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Seleccione una entrada para editar");
            return;
        }

        Dialog<BlackListEntry> dialog = crearDialogo("Editar Lista Negra", selected);

        dialog.showAndWait().ifPresent(updated -> {
            table.refresh();
            saveBlackList();
        });
    }

    // =====================================================
    //                    ELIMINAR
    // =====================================================
    private void deleteEntry() {

        BlackListEntry selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Seleccione una entrada para eliminar");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar esta entrada?",
                ButtonType.OK, ButtonType.CANCEL);

        alert.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                blackListEntries.remove(selected);
                saveBlackList();
            }
        });
    }

    // =====================================================
    //             DIÁLOGO ADD / EDIT (COMÚN)
    // =====================================================
    private Dialog<BlackListEntry> crearDialogo(String titulo, BlackListEntry edit) {

        Dialog<BlackListEntry> dialog = new Dialog<>();
        dialog.setTitle(titulo);

        TextField nombreField = new TextField(edit != null ? edit.getNombre() : "");
        DatePicker diaPicker = new DatePicker(edit != null ? edit.getDia() : LocalDate.now());

        ObservableList<BlackListEntry.BlackListItem> items =
                FXCollections.observableArrayList(
                        edit != null ? edit.getProductos() : List.of()
                );

        TableView<BlackListEntry.BlackListItem> prodTable = new TableView<>(items);

        TableColumn<BlackListEntry.BlackListItem, String> prod =
                new TableColumn<>("Producto");
        prod.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getProducto()));

        TableColumn<BlackListEntry.BlackListItem, Double> cant =
                new TableColumn<>("Cantidad");
        cant.setCellValueFactory(d ->
                new SimpleDoubleProperty(d.getValue().getCantidad()).asObject());

        prodTable.getColumns().addAll(prod, cant);

        ComboBox<String> prodBox = new ComboBox<>();
        prodBox.getItems().addAll(
                availableProducts.stream()
                        .map(Product::getProducto)
                        .collect(Collectors.toList())
        );

        TextField cantField = new TextField();
        cantField.setPromptText("Cantidad");

        Button addProd = new Button("Añadir");
        addProd.setOnAction(e -> {
            try {
                Product p = availableProducts.stream()
                        .filter(x -> x.getProducto().equals(prodBox.getValue()))
                        .findFirst().orElse(null);
                if (p == null) return;
                items.add(new BlackListEntry.BlackListItem(
                        p.getProducto(),
                        Double.parseDouble(cantField.getText()),
                        p.getPrecioV()
                ));
                cantField.clear();
            } catch (Exception ex) {
                showError("Cantidad inválida");
            }
        });

        VBox content = new VBox(10,
                new Label("Nombre:"), nombreField,
                new Label("Día:"), diaPicker,
                prodTable,
                new HBox(10, prodBox, cantField, addProd)
        );
        content.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes()
                .addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                if (edit != null) {
                    edit.setNombre(nombreField.getText());
                    edit.setDia(diaPicker.getValue());
                    edit.setProductos(new ArrayList<>(items));
                    return edit;
                }
                BlackListEntry e = new BlackListEntry(
                        nombreField.getText(), diaPicker.getValue());
                e.setProductos(new ArrayList<>(items));
                return e;
            }
            return null;
        });

        return dialog;
    }

    // =====================================================
    //                 SERIALIZACIÓN
    // =====================================================
    private void saveBlackList() {
        try {
            Path folder = Paths.get(System.getProperty("user.home"),
                    "Desktop", BLACKLIST_FOLDER);
            Files.createDirectories(folder);
            try (ObjectOutputStream oos =
                         new ObjectOutputStream(
                                 new FileOutputStream(folder.resolve(BLACKLIST_FILE).toFile()))) {
                oos.writeObject(new ArrayList<>(blackListEntries));
            }
        } catch (IOException e) {
            showError("Error guardando lista negra");
        }
    }

    @SuppressWarnings("unchecked")
    private void loadBlackList() {
        try {
            Path file = Paths.get(System.getProperty("user.home"),
                    "Desktop", BLACKLIST_FOLDER, BLACKLIST_FILE);
            if (!Files.exists(file)) return;
            try (ObjectInputStream ois =
                         new ObjectInputStream(new FileInputStream(file.toFile()))) {
                blackListEntries.addAll((List<BlackListEntry>) ois.readObject());
            }
        } catch (Exception e) {
            showError("Error cargando lista negra");
        }
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}

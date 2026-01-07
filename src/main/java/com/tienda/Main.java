package com.tienda;

import com.tienda.controller.CalculatorViewController;
import com.tienda.controller.MainViewController;
import com.tienda.controller.TotalUtilidadesController;
import com.tienda.service.LicenseService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    private Scene scene;

    @Override
    public void start(Stage primaryStage) {
        // Verificar licencia antes de mostrar la interfaz
        if (!LicenseService.checkLicense()) {
            System.exit(0);
        }

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add("main-tab-pane");

        // Tab Principal
        Tab mainTab = new Tab("📦 Inventario");
        mainTab.setTooltip(new javafx.scene.control.Tooltip("Gestión de productos e inventario"));
        MainViewController mainController = new MainViewController();
        mainTab.setContent(mainController.createView());

        // Tab Calculadora Efectivo
        Tab calculatorTab = new Tab("🧮 Calculadora");
        calculatorTab.setTooltip(new javafx.scene.control.Tooltip("Calculadora de efectivo"));
        CalculatorViewController calculatorController = new CalculatorViewController();
        calculatorTab.setContent(calculatorController.createView());

        // Tab Total Utilidades
        Tab utilitiesTab = new Tab("📊 Utilidades");
        utilitiesTab.setTooltip(new javafx.scene.control.Tooltip("Análisis de utilidades totales"));
        TotalUtilidadesController utilitiesController = new TotalUtilidadesController();
        utilitiesController.setScene(scene);
        utilitiesTab.setContent(utilitiesController.createView());

        tabPane.getTabs().addAll(mainTab, calculatorTab, utilitiesTab);

        // Toggle para tema
        CheckBox themeToggle = new CheckBox("🌙 Modo Oscuro");
        themeToggle.getStyleClass().add("theme-toggle-checkbox");
        themeToggle.setSelected(true); // Inicia en modo oscuro
        themeToggle.setOnAction(e -> {
            if (themeToggle.isSelected()) {
                themeToggle.setText("🌙 Modo Oscuro");
                scene.getStylesheets().remove(getClass().getResource("/light.css").toExternalForm());
                scene.getStylesheets().add(getClass().getResource("/dark.css").toExternalForm());
            } else {
                themeToggle.setText("☀️ Modo Claro");
                scene.getStylesheets().remove(getClass().getResource("/dark.css").toExternalForm());
                scene.getStylesheets().add(getClass().getResource("/light.css").toExternalForm());
            }
        });

        // Botón para Lista Negra
        Button blackListBtn = new Button("Ver lista negra");
        blackListBtn.setOnAction(e -> {
            com.tienda.controller.BlackListController blackListController =
                new com.tienda.controller.BlackListController(mainController.getProducts());
            blackListController.showBlackListWindow();
        });

        // Contenedor para el toggle y botón de lista negra
        javafx.scene.layout.HBox toggleContainer = new javafx.scene.layout.HBox(20);
        toggleContainer.getStyleClass().add("toggle-container");
        toggleContainer.getChildren().addAll(themeToggle, blackListBtn);
        toggleContainer.setAlignment(javafx.geometry.Pos.CENTER);

        // Etiqueta discreta del creador
        Label creatorLabel = new Label("by José J. Batista de Paz");
        creatorLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: gray; -fx-padding: 5 0 0 0;");
        HBox creatorContainer = new HBox();
        creatorContainer.setAlignment(javafx.geometry.Pos.CENTER);
        creatorContainer.getChildren().add(creatorLabel);

        // Layout principal con mejor espaciado
        VBox mainLayout = new VBox(15);
        mainLayout.getStyleClass().add("main-layout");
        mainLayout.setPadding(new javafx.geometry.Insets(20));
        mainLayout.getChildren().addAll(toggleContainer, tabPane, creatorContainer);

        Scene scene = new Scene(mainLayout, 1300, 850);
        this.scene = scene;
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/dark.css").toExternalForm());

        primaryStage.setTitle("🛒 Tienda Desktop App - Sistema de Gestión");
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);

        // Cargar icono de la aplicación
        try {
            primaryStage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/icon.ico")));
        } catch (Exception e) {
            System.out.println("No se pudo cargar el icono: " + e.getMessage());
        }

        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
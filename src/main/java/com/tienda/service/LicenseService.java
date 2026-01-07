package com.tienda.service;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Servicio para gestionar el sistema de licencias de la aplicación.
 * Permite un período de prueba de 45 días desde la primera ejecución.
 */
public class LicenseService {

    private static final String LICENSE_FILE = "Tienda APP/first_run.dat";
    private static final String PASSWORD = "Jose312005*";
    private static final int TRIAL_DAYS = 30;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Verifica si la licencia es válida. Si no existe, crea la fecha de primera ejecución.
     * Si han pasado más de 45 días, solicita la contraseña.
     * @return true si la aplicación puede continuar, false si debe cerrarse.
     */
    public static boolean checkLicense() {
        Path licensePath = getLicensePath();

        try {
            // Si el archivo no existe, crearlo con la fecha actual
            if (!Files.exists(licensePath)) {
                saveFirstRunDate(licensePath);
                return true;
            }

            // Leer y validar la fecha guardada
            String storedData = Files.readString(licensePath);
            if (!isValidData(storedData)) {
                // Archivo corrupto, recrear
                saveFirstRunDate(licensePath);
                return true;
            }

            LocalDate firstRunDate = LocalDate.parse(storedData.split("\\|")[0], FORMATTER);
            long daysPassed = ChronoUnit.DAYS.between(firstRunDate, LocalDate.now());

            if (daysPassed > TRIAL_DAYS) {
                // Período de prueba expirado, solicitar contraseña
                return promptPassword();
            }

            return true;

        } catch (Exception e) {
            // En caso de error, asumir válido para no bloquear la app
            System.err.println("Error checking license: " + e.getMessage());
            return true;
        }
    }

    /**
     * Guarda la fecha de primera ejecución en el archivo de licencia.
     */
    private static void saveFirstRunDate(Path licensePath) throws IOException {
        Files.createDirectories(licensePath.getParent());
        LocalDate now = LocalDate.now();
        String data = now.format(FORMATTER) + "|" + generateHash(now.format(FORMATTER));
        Files.writeString(licensePath, data);
    }

    /**
     * Valida la integridad de los datos guardados usando hash.
     */
    private static boolean isValidData(String data) {
        try {
            String[] parts = data.split("\\|");
            if (parts.length != 2) return false;
            String dateStr = parts[0];
            String hash = parts[1];
            return hash.equals(generateHash(dateStr));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Genera un hash SHA-256 para verificar la integridad de la fecha.
     */
    private static String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Muestra un diálogo modal para solicitar la contraseña.
     * No permite cerrar la ventana hasta que se ingrese correctamente.
     */
    private static boolean promptPassword() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Licencia Expirada");
        dialog.setResizable(false);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Ingrese la contraseña");

        Button submitButton = new Button("Aceptar");
        submitButton.setOnAction(e -> {
            if (PASSWORD.equals(passwordField.getText())) {
                dialog.close();
            } else {
                passwordField.clear();
                passwordField.setPromptText("Contraseña incorrecta. Intente de nuevo.");
            }
        });

        // Evitar que se cierre la ventana con ESC o X
        dialog.setOnCloseRequest(e -> e.consume());

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(
            new Label("El período de prueba ha expirado."),
            new Label("Ingrese la contraseña para continuar:"),
            passwordField,
            submitButton
        );

        Scene scene = new Scene(layout, 300, 150);
        dialog.setScene(scene);
        dialog.showAndWait();

        // Si el diálogo se cerró, significa que la contraseña fue correcta
        return !dialog.isShowing();
    }

    /**
     * Obtiene la ruta del archivo de licencia en el directorio del usuario.
     */
    private static Path getLicensePath() {
        return Paths.get(System.getProperty("user.home"), "Desktop", LICENSE_FILE);
    }
}

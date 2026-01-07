package com.tienda.service;

import com.tienda.Product;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AutoSaveService {

    private Timer autoSaveTimer;
    private List<Product> products;
    private boolean isRunning = false;

    public AutoSaveService(List<Product> products) {
        this.products = products;
    }

    public void startAutoSave() {
        if (isRunning) {
            return;
        }

        isRunning = true;
        autoSaveTimer = new Timer(true); // Daemon thread

        // Programar autoguardado cada 45 segundos
        autoSaveTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> saveBackup());
            }
        }, 45000, 45000); // 45 segundos inicial, luego cada 45 segundos
    }

    public void stopAutoSave() {
        if (autoSaveTimer != null) {
            autoSaveTimer.cancel();
            autoSaveTimer = null;
        }
        isRunning = false;
    }

    private void saveBackup() {
        try {
            // Crear estructura de directorios
            String desktopPath = System.getProperty("user.home") + "/Desktop";
            LocalDate now = LocalDate.now();
            String day = String.format("%02d", now.getDayOfMonth());

            Path backupDir = Paths.get(desktopPath, "Tienda APP", "backup", day);
            Files.createDirectories(backupDir);

            // Nombre del archivo con timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH-mm-ss"));
            String fileName = "backup_" + timestamp + ".csv";
            File backupFile = new File(backupDir.toFile(), fileName);

            // Guardar como CSV
            try (FileWriter writer = new FileWriter(backupFile)) {
                // Encabezados
                writer.write("Producto,Cantidad,PrecioC,PrecioV,InvInicial,Entrada,Venta,Merma,UtilidadUnidad,UtilidadVenta\n");

                // Datos
                for (Product product : products) {
                    writer.write(String.format("%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f\n",
                        escapeCSV(product.getProducto()),
                        product.getInvFinal(),
                        product.getPrecioC(),
                        product.getPrecioV(),
                        product.getInvInicial(),
                        product.getEntrada(),
                        product.getVenta(),
                        product.getMerma(),
                        product.getUtilidadUnidad(),
                        product.getUtilidadVenta()
                    ));
                }
            }

            System.out.println("Backup guardado: " + backupFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("Error al guardar backup automático: " + e.getMessage());
        }
    }

    private String escapeCSV(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public File getLatestBackup() {
        try {
            String desktopPath = System.getProperty("user.home") + "/Desktop";
            LocalDate now = LocalDate.now();
            String day = String.format("%02d", now.getDayOfMonth());

            Path backupDir = Paths.get(desktopPath, "Tienda APP", "backup", day);
            if (!Files.exists(backupDir)) {
                return null;
            }

            File[] backupFiles = backupDir.toFile().listFiles((dir, name) -> name.startsWith("backup_") && name.endsWith(".csv"));
            if (backupFiles == null || backupFiles.length == 0) {
                return null;
            }

            // Encontrar el archivo más reciente
            File latest = null;
            long latestTime = 0;
            for (File file : backupFiles) {
                if (file.lastModified() > latestTime) {
                    latest = file;
                    latestTime = file.lastModified();
                }
            }

            return latest;

        } catch (Exception e) {
            System.err.println("Error al buscar backup más reciente: " + e.getMessage());
            return null;
        }
    }

    public void loadBackup(File backupFile, List<Product> targetProducts) {
        try {
            targetProducts.clear();
            List<String> lines = Files.readAllLines(backupFile.toPath());

            for (int i = 1; i < lines.size(); i++) { // Saltar encabezado
                String[] parts = parseCSVLine(lines.get(i));
                if (parts.length >= 10) {
                    String producto = parts[0];
                    double cantidad = Double.parseDouble(parts[1]);
                    double precioC = Double.parseDouble(parts[2]);
                    double precioV = Double.parseDouble(parts[3]);
                    double invInicial = Double.parseDouble(parts[4]);
                    double entrada = Double.parseDouble(parts[5]);
                    double venta = Double.parseDouble(parts[6]);
                    double merma = Double.parseDouble(parts[7]);
                    double utilidadUnidad = Double.parseDouble(parts[8]);
                    double utilidadVenta = Double.parseDouble(parts[9]);

                    Product product = new Product(producto, invInicial, entrada, venta, merma, precioC, precioV);
                    targetProducts.add(product);
                }
            }

        } catch (Exception e) {
            System.err.println("Error al cargar backup: " + e.getMessage());
            throw new RuntimeException("Error al cargar el backup", e);
        }
    }

    private String[] parseCSVLine(String line) {
        // Parser simple de CSV que maneja comillas
        if (!line.contains("\"")) {
            return line.split(",");
        }

        java.util.List<String> result = new java.util.ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++; // Saltar el siguiente
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());

        return result.toArray(new String[0]);
    }
}
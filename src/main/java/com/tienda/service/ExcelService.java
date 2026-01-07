package com.tienda.service;

import com.tienda.Product;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class ExcelService {

    // Lista de unidades del sistema
    private final List<String> unidadesSistema = List.of("Unidades", "Libras", "Kg", "Litros", "Paquetes", "libras");

    public File exportToExcel(List<Product> products) throws IOException {
        System.out.println("=== INICIANDO EXPORTACIÓN A EXCEL ===");
        System.out.println("Número de productos a exportar: " + products.size());

        // Crear estructura de directorios
        String userHome = System.getProperty("user.home");
        System.out.println("User home: " + userHome);

        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        String day = String.format("%02d", now.getDayOfMonth());

        System.out.println("Fecha para directorio: " + year + "/" + month + "/" + day);

        // Crear la estructura de carpetas dentro de "Tienda APP" en el escritorio
        Path dirPath = Paths.get(userHome, "Desktop", "Tienda APP", year, month, day);
        try {
            Files.createDirectories(dirPath);
        } catch (Exception e) {
            System.err.println("Error al crear directorios: " + e.getMessage());
            throw new IOException("Error al crear la estructura de directorios", e);
        }
        System.out.println("Ruta completa del directorio: " + dirPath.toString());

        String fileName = String.format("%02d-%02d-%04d.xlsx", now.getDayOfMonth(), now.getMonthValue(), now.getYear());
        File file = new File(dirPath.toFile(), fileName);
        System.out.println("Archivo a crear: " + file.getAbsolutePath());

        // Verificar si el archivo ya existe
        if (file.exists()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Archivo existente");
            alert.setHeaderText("Ya existe un archivo con este nombre.");
            alert.setContentText("¿Desea reemplazarlo?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Usuario elige Sí, continuar
            } else {
                // Usuario elige No, cancelar
                System.out.println("Guardado cancelado por el usuario.");
                return null;
            }
        }

        try {
            System.out.println("Creando workbook...");
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Productos");

                // Encabezados
                Row headerRow = sheet.createRow(0);
                String[] headers = {"Productos", "Unidad", "InvInicial", "Entrada", "Venta", "Merma", "InvFinal", "PrecioC", "PrecioV", "UtilidadUnidad", "UtilidadVenta"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                }

                // Datos
                int rowNum = 1;
                for (Product p : products) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(p.getProducto());
                    row.createCell(1).setCellValue(p.getUnidadVenta());
                    row.createCell(2).setCellValue(p.getInvInicial());
                    row.createCell(3).setCellValue(p.getEntrada());
                    row.createCell(4).setCellValue(p.getVenta());
                    row.createCell(5).setCellValue(p.getMerma());
                    row.createCell(6).setCellValue(p.getInvFinal());
                    row.createCell(7).setCellValue(p.getPrecioC());
                    row.createCell(8).setCellValue(p.getPrecioV());
                    row.createCell(9).setCellValue(p.getUtilidadUnidad());
                    row.createCell(10).setCellValue(p.getUtilidadVenta());
                }

                // Fila de separación
                sheet.createRow(rowNum++);

                // Resumen Totales (sin productos, unidades vendidas y ganancia de ventas)
                Row resumenRow = sheet.createRow(rowNum++);
                resumenRow.createCell(0).setCellValue("Resumen Totales:");

                double totalPrecioC = products.stream().mapToDouble(p -> (p.getInvInicial() + p.getEntrada()) * p.getPrecioC()).sum();
                double totalPrecioV = products.stream().mapToDouble(p -> (p.getInvInicial() + p.getEntrada()) * p.getPrecioV()).sum();
                double gananciaTotal = totalPrecioV - totalPrecioC;
                double utilidadTotal = products.stream().mapToDouble(Product::getUtilidadVenta).sum();

                sheet.createRow(rowNum++).createCell(0).setCellValue("Precio C.Total: " + String.format("%.2f", totalPrecioC));
                sheet.createRow(rowNum++).createCell(0).setCellValue("Precio V.Total: " + String.format("%.2f", totalPrecioV));
                sheet.createRow(rowNum++).createCell(0).setCellValue("Ganancia Total: " + String.format("%.2f", gananciaTotal));
                sheet.createRow(rowNum++).createCell(0).setCellValue("Utilidad Total: " + String.format("%.2f", utilidadTotal));

                System.out.println("Escribiendo archivo...");
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }
                System.out.println("Archivo escrito exitosamente: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Error durante la creación del archivo Excel: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error al crear el archivo Excel", e);
        }

        System.out.println("=== EXPORTACIÓN COMPLETADA ===");
        return file;
    }

    public List<Product> importFromExcel(File file) throws IOException {
        List<Product> products = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file); Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null && row.getCell(0) != null) {
                    String cellValue = row.getCell(0).getStringCellValue().trim();

                    // Ignorar filas no deseadas y filas de resumen
                    if (cellValue.equalsIgnoreCase("Total de productos") ||
                        cellValue.equalsIgnoreCase("Unidades vendidas") ||
                        cellValue.equalsIgnoreCase("Ganancia en ventas") ||
                        cellValue.equalsIgnoreCase("Resumen Totales") ||
                        cellValue.toLowerCase().contains("resumen totales:") ||
                        cellValue.toLowerCase().contains("productos:") ||
                        cellValue.toLowerCase().contains("unidades vendidas") ||
                        cellValue.toLowerCase().contains("ganancia de ventas") ||
                        cellValue.toLowerCase().contains("precio c total") ||
                        cellValue.toLowerCase().contains("precio v total") ||
                        cellValue.toLowerCase().contains("ganancia total") ||
                        cellValue.toLowerCase().contains("utilidad total") ||
                        cellValue.isEmpty() ||
                        cellValue.matches(".*:\\s*\\d+.*")) { // Ignorar filas con formato "Etiqueta: número"
                        continue;
                    }

                    if (!cellValue.isEmpty()) {
                        try {
                            String producto;
                            String unidad;

                            // Check if column 1 (Unidad) has a value (new format)
                            Cell unitCell = row.getCell(1);
                            if (unitCell != null && unitCell.getCellType() != CellType.BLANK && !unitCell.getStringCellValue().trim().isEmpty()) {
                                // New format: separate columns
                                producto = cellValue;
                                unidad = unitCell.getStringCellValue().trim();
                            } else {
                                // Old format: combined in column 0, split by space
                                String[] parts = cellValue.trim().split("\\s+");
                                unidad = parts.length > 1 ? parts[parts.length - 1] : "Unidades";
                                producto = String.join(" ", java.util.Arrays.copyOf(parts, parts.length - 1));
                                if (producto.isEmpty()) {
                                    producto = unidad;
                                    unidad = "Unidades";
                                }
                            }

                            producto = capitalizeFirstLetter(producto);
                            int invInicial = (int) getNumericCellValue(row.getCell(2));
                            int entrada = (int) getNumericCellValue(row.getCell(3));
                            int venta = (int) getNumericCellValue(row.getCell(4));
                            int merma = (int) getNumericCellValue(row.getCell(5));
                            double precioC = getNumericCellValue(row.getCell(7));
                            double precioV = getNumericCellValue(row.getCell(8));
                            products.add(new Product(producto, invInicial, entrada, venta, merma, precioC, precioV, "", unidad));
                        } catch (Exception ex) {
                            // Ignorar fila con error
                        }
                    }
                }
            }
        }
        return products;
    }

    public void importNewExcel(File file, List<Product> products) throws IOException {
        try (FileInputStream fis = new FileInputStream(file); Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null && row.getCell(0) != null) {
                    String cellValue = row.getCell(0).getStringCellValue().trim();

                    // Ignorar filas no deseadas y filas de resumen
                    if (cellValue.equalsIgnoreCase("Total de productos") ||
                        cellValue.equalsIgnoreCase("Unidades vendidas") ||
                        cellValue.equalsIgnoreCase("Ganancia en ventas") ||
                        cellValue.equalsIgnoreCase("Resumen Totales") ||
                        cellValue.toLowerCase().contains("resumen totales:") ||
                        cellValue.toLowerCase().contains("productos:") ||
                        cellValue.toLowerCase().contains("unidades vendidas") ||
                        cellValue.toLowerCase().contains("ganancia de ventas") ||
                        cellValue.toLowerCase().contains("precio c total") ||
                        cellValue.toLowerCase().contains("precio v total") ||
                        cellValue.toLowerCase().contains("ganancia total") ||
                        cellValue.toLowerCase().contains("utilidad total") ||
                        cellValue.isEmpty() ||
                        cellValue.matches(".*:\\s*\\d+.*")) { // Ignorar filas con formato "Etiqueta: número"
                        continue;
                    }

                    if (!cellValue.isEmpty()) {
                        String producto;
                        String unidad;

                        // Check if column 1 (Unidad) has a value (new format)
                        Cell unitCell = row.getCell(1);
                        if (unitCell != null && unitCell.getCellType() != CellType.BLANK && !unitCell.getStringCellValue().trim().isEmpty()) {
                            // New format: separate columns
                            producto = cellValue;
                            unidad = unitCell.getStringCellValue().trim();
                        } else {
                            // Old format: combined in column 0, split by space
                            String[] parts = cellValue.trim().split("\\s+");
                            unidad = parts.length > 1 ? parts[parts.length - 1] : "Unidades";
                            producto = String.join(" ", java.util.Arrays.copyOf(parts, parts.length - 1));
                            if (producto.isEmpty()) {
                                producto = unidad;
                                unidad = "Unidades";
                            }
                        }

                        producto = capitalizeFirstLetter(producto);
                        int invFinal = (int) getNumericCellValue(row.getCell(6));
                        double precioC = getNumericCellValue(row.getCell(7));
                        double precioV = getNumericCellValue(row.getCell(8));
                        products.add(new Product(producto, invFinal, 0, 0, 0, precioC, precioV, "", unidad));
                    }
                }
            }
        }
    }

    public File chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Excel");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Excel Files", "*.xlsx"));
        return fileChooser.showOpenDialog(null);
    }

    private double getNumericCellValue(Cell cell) {
        if (cell == null) return 0.0;
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            default:
                return 0.0;
        }
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}
package com.tienda.service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FileTrackingService {

    private static final int MAX_FILES = 10;
    private static final String TRACKING_FILE = "exported_files.txt";
    private LinkedList<String> exportedFiles = new LinkedList<>();

    public FileTrackingService() {
        loadExportedFiles();
    }

    public void addExportedFile(String filePath) {
        File file = new File(filePath);
        String displayName = file.getName() + " - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        exportedFiles.addFirst(displayName + "|" + filePath);

        if (exportedFiles.size() > MAX_FILES) {
            exportedFiles.removeLast();
        }

        saveExportedFiles();
    }

    public List<String> getLastExportedFiles() {
        List<String> displayNames = new ArrayList<>();
        for (String entry : exportedFiles) {
            displayNames.add(entry.split("\\|")[0]);
        }
        return displayNames;
    }

    public String getFilePathFromDisplay(String displayName) {
        for (String entry : exportedFiles) {
            if (entry.startsWith(displayName + "|")) {
                return entry.split("\\|")[1];
            }
        }
        return null;
    }

    private void loadExportedFiles() {
        try {
            Path path = Paths.get(TRACKING_FILE);
            if (Files.exists(path)) {
                exportedFiles = new LinkedList<>(Files.readAllLines(path));
            }
        } catch (IOException e) {
            // Archivo no existe o error, usar lista vacía
        }
    }

    private void saveExportedFiles() {
        try {
            Files.write(Paths.get(TRACKING_FILE), exportedFiles);
        } catch (IOException e) {
            // Error al guardar, ignorar
        }
    }
}
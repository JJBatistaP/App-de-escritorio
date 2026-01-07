package com.tienda;

import javafx.util.StringConverter;

/**
 * Custom StringConverter for Double values that accepts both comma and dot as decimal separators.
 * This allows users to input decimal numbers using either European (comma) or American (dot) notation.
 */
public class DecimalStringConverter extends StringConverter<Double> {

    @Override
    public String toString(Double value) {
        if (value == null) {
            return "";
        }
        // Always format with dot for display consistency
        return String.format("%.2f", value);
    }

    @Override
    public Double fromString(String string) {
        if (string == null || string.trim().isEmpty()) {
            return 0.0;
        }

        String trimmed = string.trim();

        // Replace comma with dot to standardize decimal separator
        String normalized = trimmed.replace(',', '.');

        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            // If parsing fails, try to extract a valid number
            // Remove any non-numeric characters except dot
            String cleaned = normalized.replaceAll("[^0-9.]", "");
            try {
                return Double.parseDouble(cleaned);
            } catch (NumberFormatException ex) {
                // Return 0.0 as fallback
                return 0.0;
            }
        }
    }
}

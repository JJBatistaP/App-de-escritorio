package com.tienda.service;

import com.tienda.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.List;
import java.util.function.Predicate;

public class SearchService {

    private ObservableList<Product> products;
    private FilteredList<Product> filteredProducts;

    public SearchService(ObservableList<Product> products) {
        this.products = products;
        this.filteredProducts = new FilteredList<>(products);
    }

    /**
     * Search products by name (case-insensitive)
     */
    public void searchByName(String query) {
        if (query == null || query.trim().isEmpty()) {
            filteredProducts.setPredicate(null);
            return;
        }

        String lowerQuery = query.toLowerCase().trim();
        filteredProducts.setPredicate(product ->
            product.getProducto().toLowerCase().contains(lowerQuery) ||
            product.getDescripcion().toLowerCase().contains(lowerQuery)
        );
    }

    /**
     * Filter products by various criteria
     */
    public void applyFilters(Predicate<Product> filter) {
        filteredProducts.setPredicate(filter);
    }

    /**
     * Clear all filters and show all products
     */
    public void clearFilters() {
        filteredProducts.setPredicate(null);
    }

    /**
     * Get filtered products list
     */
    public FilteredList<Product> getFilteredProducts() {
        return filteredProducts;
    }

    /**
     * Get products with zero inventory
     */
    public ObservableList<Product> getProductsWithZeroInventory() {
        return products.filtered(product -> product.getInvFinal() == 0);
    }

    /**
     * Get products with positive inventory
     */
    public ObservableList<Product> getProductsWithPositiveInventory() {
        return products.filtered(product -> product.getInvFinal() > 0);
    }

    /**
     * Get products with zero prices
     */
    public ObservableList<Product> getProductsWithZeroPrices() {
        return products.filtered(product -> product.getPrecioC() == 0 || product.getPrecioV() == 0);
    }

    /**
     * Get products with recent entries
     */
    public ObservableList<Product> getProductsWithRecentEntries() {
        return products.filtered(product -> product.getEntrada() > 0);
    }

    /**
     * Advanced search with multiple criteria
     */
    public void advancedSearch(String nameQuery, Double minPrice, Double maxPrice,
                             Integer minInventory, Integer maxInventory, String unitFilter) {
        Predicate<Product> combinedPredicate = product -> {
            // Name filter
            if (nameQuery != null && !nameQuery.trim().isEmpty()) {
                String lowerQuery = nameQuery.toLowerCase().trim();
                if (!product.getProducto().toLowerCase().contains(lowerQuery) &&
                    !product.getDescripcion().toLowerCase().contains(lowerQuery)) {
                    return false;
                }
            }

            // Price range filter
            if (minPrice != null && product.getPrecioC() < minPrice) return false;
            if (maxPrice != null && product.getPrecioC() > maxPrice) return false;

            // Inventory range filter
            if (minInventory != null && product.getInvFinal() < minInventory) return false;
            if (maxInventory != null && product.getInvFinal() > maxInventory) return false;

            // Unit filter
            if (unitFilter != null && !unitFilter.isEmpty() && !product.getUnidadVenta().equals(unitFilter)) {
                return false;
            }

            return true;
        };

        filteredProducts.setPredicate(combinedPredicate);
    }
}

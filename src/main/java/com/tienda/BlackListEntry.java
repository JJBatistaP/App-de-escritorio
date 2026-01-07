package com.tienda;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BlackListEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombre;
    private LocalDate dia;
    private List<BlackListItem> productos;

    public BlackListEntry(String nombre, LocalDate dia) {
        this.nombre = nombre;
        this.dia = dia;
        this.productos = new ArrayList<>();
    }

    public BlackListEntry(String nombre, LocalDate dia, List<BlackListItem> productos) {
        this.nombre = nombre;
        this.dia = dia;
        this.productos = productos != null ? new ArrayList<>(productos) : new ArrayList<>();
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public LocalDate getDia() { return dia; }
    public void setDia(LocalDate dia) { this.dia = dia; }

    public List<BlackListItem> getProductos() { return productos; }
    public void setProductos(List<BlackListItem> productos) {
        this.productos = productos != null ? new ArrayList<>(productos) : new ArrayList<>();
    }

    public void addProducto(String producto, double cantidad, double precioUnitario) {
        this.productos.add(new BlackListItem(producto, cantidad, precioUnitario));
    }

    public double getImporteTotal() {
        return productos.stream()
                .mapToDouble(i -> i.getCantidad() * i.getPrecioUnitario())
                .sum();
    }

    public String getProducto() {
        return productos.stream()
                .map(i -> i.getProducto() + " (" + i.getCantidad() + ")")
                .collect(java.util.stream.Collectors.joining(", "));
    }

    public double getImporte() {
        return getImporteTotal();
    }

    public static class BlackListItem implements Serializable {
        private static final long serialVersionUID = 1L;

        private String producto;
        private double cantidad;
        private double precioUnitario;

        public BlackListItem(String producto, double cantidad, double precioUnitario) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
        }

        public String getProducto() { return producto; }
        public double getCantidad() { return cantidad; }
        public double getPrecioUnitario() { return precioUnitario; }
        public double getSubtotal() { return cantidad * precioUnitario; }
    }
}

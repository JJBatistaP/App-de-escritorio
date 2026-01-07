package com.tienda;

import javafx.beans.property.*;
import java.io.Serializable;

public class Product implements Serializable {
    private final StringProperty producto = new SimpleStringProperty();
    private final DoubleProperty invInicial = new SimpleDoubleProperty();
    private final DoubleProperty entrada = new SimpleDoubleProperty();
    private final DoubleProperty venta = new SimpleDoubleProperty();
    private final DoubleProperty merma = new SimpleDoubleProperty();
    private final DoubleProperty invFinal = new SimpleDoubleProperty();
    private final DoubleProperty precioC = new SimpleDoubleProperty();
    private final DoubleProperty precioV = new SimpleDoubleProperty();
    private final DoubleProperty utilidadUnidad = new SimpleDoubleProperty();
    private final DoubleProperty utilidadVenta = new SimpleDoubleProperty();
    private final DoubleProperty percentage = new SimpleDoubleProperty();
    private final DoubleProperty valorInvInicial = new SimpleDoubleProperty();
    private final StringProperty descripcion = new SimpleStringProperty();
    private final StringProperty unidadVenta = new SimpleStringProperty();

    public Product(String producto, double invInicial, double entrada, double venta, double merma, double precioC, double precioV) {
        this(producto, invInicial, entrada, venta, merma, precioC, precioV, "", "Unidades");
    }

    public Product(String producto, double invInicial, double entrada, double venta, double merma, double precioC, double precioV, String descripcion, String unidadVenta) {
        setProducto(producto);
        setInvInicial(invInicial);
        setEntrada(entrada);
        setVenta(venta);
        setMerma(merma);
        setPrecioC(precioC);
        setPrecioV(precioV);
        setDescripcion(descripcion);
        setUnidadVenta(unidadVenta);
        recalcular();
    }

    public void recalcular() {
        double invFinalCalc = Math.max(getInvInicial() + getEntrada() - getVenta() - getMerma(), 0.0);
        setInvFinal(invFinalCalc);
        double utilidadUnidadCalc = getPrecioV() - getPrecioC();
        setUtilidadUnidad(utilidadUnidadCalc);
        double utilidadVentaCalc = (getPrecioV() - getPrecioC()) * getVenta();
        setUtilidadVenta(utilidadVentaCalc);
        double valorInvInicialCalc = getInvFinal() * getPrecioV();
        setValorInvInicial(valorInvInicialCalc);
    }

    // Getters and setters
    public StringProperty productoProperty() { return producto; }
    public String getProducto() { return producto.get(); }
    public void setProducto(String producto) { this.producto.set(producto); }

    public DoubleProperty invInicialProperty() { return invInicial; }
    public double getInvInicial() { return invInicial.get(); }
    public void setInvInicial(double invInicial) { this.invInicial.set(invInicial); recalcular(); }

    public DoubleProperty entradaProperty() { return entrada; }
    public double getEntrada() { return entrada.get(); }
    public void setEntrada(double entrada) { this.entrada.set(entrada); recalcular(); }

    public DoubleProperty ventaProperty() { return venta; }
    public double getVenta() { return venta.get(); }
    public void setVenta(double venta) { this.venta.set(venta); recalcular(); }

    public DoubleProperty mermaProperty() { return merma; }
    public double getMerma() { return merma.get(); }
    public void setMerma(double merma) { this.merma.set(merma); recalcular(); }

    public DoubleProperty invFinalProperty() { return invFinal; }
    public double getInvFinal() { return invFinal.get(); }
    public void setInvFinal(double invFinal) { this.invFinal.set(invFinal); }

    public DoubleProperty precioCProperty() { return precioC; }
    public double getPrecioC() { return precioC.get(); }
    public void setPrecioC(double precioC) { this.precioC.set(precioC); recalcular(); }

    public DoubleProperty precioVProperty() { return precioV; }
    public double getPrecioV() { return precioV.get(); }
    public void setPrecioV(double precioV) { this.precioV.set(precioV); recalcular(); }

    public DoubleProperty utilidadUnidadProperty() { return utilidadUnidad; }
    public double getUtilidadUnidad() { return utilidadUnidad.get(); }
    public void setUtilidadUnidad(double utilidadUnidad) { this.utilidadUnidad.set(utilidadUnidad); }

    public DoubleProperty utilidadVentaProperty() { return utilidadVenta; }
    public double getUtilidadVenta() { return utilidadVenta.get(); }
    public void setUtilidadVenta(double utilidadVenta) { this.utilidadVenta.set(utilidadVenta); }

    public DoubleProperty percentageProperty() { return percentage; }
    public double getPercentage() { return percentage.get(); }
    public void setPercentage(double percentage) { this.percentage.set(percentage); }

    public DoubleProperty valorInvInicialProperty() { return valorInvInicial; }
    public double getValorInvInicial() { return valorInvInicial.get(); }
    public void setValorInvInicial(double valorInvInicial) { this.valorInvInicial.set(valorInvInicial); }

    public StringProperty descripcionProperty() { return descripcion; }
    public String getDescripcion() { return descripcion.get(); }
    public void setDescripcion(String descripcion) { this.descripcion.set(descripcion); }

    public StringProperty unidadVentaProperty() { return unidadVenta; }
    public String getUnidadVenta() { return unidadVenta.get(); }
    public void setUnidadVenta(String unidadVenta) { this.unidadVenta.set(unidadVenta); }
}
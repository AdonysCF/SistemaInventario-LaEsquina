package modelo;

import java.math.BigDecimal;

public class VentaDetalleRegistro {

    private final String sku;
    private final String producto;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal subtotal;

    public VentaDetalleRegistro(String sku, String producto, int cantidad,
            BigDecimal precioUnitario, BigDecimal subtotal) {
        this.sku = sku;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public String getSku() {
        return sku;
    }

    public String getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}

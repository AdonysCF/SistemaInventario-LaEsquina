package modelo;

import java.math.BigDecimal;

public class ReporteVentaFila {

    private final String sku;
    private final String producto;
    private final String categoria;
    private final long unidades;
    private final int tickets;
    private final BigDecimal monto;

    public ReporteVentaFila(String sku, String producto, String categoria,
            long unidades, int tickets, BigDecimal monto) {
        this.sku = sku;
        this.producto = producto;
        this.categoria = categoria;
        this.unidades = unidades;
        this.tickets = tickets;
        this.monto = monto;
    }

    public String getSku() {
        return sku;
    }

    public String getProducto() {
        return producto;
    }

    public String getCategoria() {
        return categoria;
    }

    public long getUnidades() {
        return unidades;
    }

    public int getTickets() {
        return tickets;
    }

    public BigDecimal getMonto() {
        return monto;
    }
}

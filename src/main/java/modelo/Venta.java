package modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Venta {
    private String numeroTicket;
    private BigDecimal totalVenta;
    private int idUsuario;
    private List<DetalleVenta> detalles = new ArrayList<>();

    // Getters y Setters
    public String getNumeroTicket() { return numeroTicket; }
    public void setNumeroTicket(String numeroTicket) { this.numeroTicket = numeroTicket; }
    public BigDecimal getTotalVenta() { return totalVenta; }
    public void setTotalVenta(BigDecimal totalVenta) { this.totalVenta = totalVenta; }
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }
    public void agregarDetalle(DetalleVenta det) { this.detalles.add(det); }
}
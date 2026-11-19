package modelo;

import java.math.BigDecimal;

public class VentaResumen {

    private final int idVenta;
    private final String numeroTicket;
    private final String fechaHora;
    private final String responsable;
    private final String usuario;
    private final String estado;
    private final BigDecimal total;

    public VentaResumen(int idVenta, String numeroTicket, String fechaHora,
            String responsable, String usuario, String estado, BigDecimal total) {
        this.idVenta = idVenta;
        this.numeroTicket = numeroTicket;
        this.fechaHora = fechaHora;
        this.responsable = responsable;
        this.usuario = usuario;
        this.estado = estado;
        this.total = total;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public String getNumeroTicket() {
        return numeroTicket;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public String getResponsable() {
        return responsable;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getEstado() {
        return estado;
    }

    public BigDecimal getTotal() {
        return total;
    }
}

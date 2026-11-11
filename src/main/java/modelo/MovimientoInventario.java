package modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MovimientoInventario {
    
    private int idMovimiento;
    private String codigoMovimiento;
    private String tipoMovimiento;
    private String documentoReferencia;
    private LocalDateTime fechaHora;
    private String observacion;
    private int idUsuario;
    private int idVentaOrigen; // Para vincularlo a una venta, si aplica

    
    private List<DetalleMovimiento> detalles;
    // Constructor vacío
    public MovimientoInventario() {
        this.detalles = new ArrayList<>();
    }
    public void agregarDetalle(DetalleMovimiento det) {
        this.detalles.add(det);
    }
    public List<DetalleMovimiento> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleMovimiento> detalles) {
        this.detalles = detalles;
    }

    // Getters y Setters
    public int getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(int idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public String getCodigoMovimiento() {
        return codigoMovimiento;
    }

    public void setCodigoMovimiento(String codigoMovimiento) {
        this.codigoMovimiento = codigoMovimiento;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public String getDocumentoReferencia() {
        return documentoReferencia;
    }

    public void setDocumentoReferencia(String documentoReferencia) {
        this.documentoReferencia = documentoReferencia;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdVentaOrigen() {
        return idVentaOrigen;
    }

    public void setIdVentaOrigen(int idVentaOrigen) {
        this.idVentaOrigen = idVentaOrigen;
    }
    
    
}
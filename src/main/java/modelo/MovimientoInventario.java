package modelo;

import java.util.ArrayList;
import java.util.List;

public class MovimientoInventario {
    private String codigoMovimiento;
    private String tipoMovimiento; // "ENTRADA_REPOSICION"
    private String documentoReferencia;
    private String observacion;
    private int idUsuario;
    private List<DetalleMovimiento> detalles = new ArrayList<>();

    // Getters y Setters
    public String getCodigoMovimiento() { return codigoMovimiento; }
    public void setCodigoMovimiento(String codigoMovimiento) { this.codigoMovimiento = codigoMovimiento; }
    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
    public String getDocumentoReferencia() { return documentoReferencia; }
    public void setDocumentoReferencia(String documentoReferencia) { this.documentoReferencia = documentoReferencia; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public List<DetalleMovimiento> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleMovimiento> detalles) { this.detalles = detalles; }
    public void agregarDetalle(DetalleMovimiento det) { this.detalles.add(det); }
}
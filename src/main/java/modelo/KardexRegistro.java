package modelo;

public class KardexRegistro {

    private final String fechaHora;
    private final String codigoMovimiento;
    private final String tipoMovimiento;
    private final String sku;
    private final String producto;
    private final int cantidad;
    private final int stockAnterior;
    private final int stockResultante;
    private final String responsable;
    private final String usuario;
    private final String documentoReferencia;
    private final String observacion;

    public KardexRegistro(String fechaHora, String codigoMovimiento, String tipoMovimiento,
            String sku, String producto, int cantidad, int stockAnterior, int stockResultante,
            String responsable, String usuario, String documentoReferencia, String observacion) {
        this.fechaHora = fechaHora;
        this.codigoMovimiento = codigoMovimiento;
        this.tipoMovimiento = tipoMovimiento;
        this.sku = sku;
        this.producto = producto;
        this.cantidad = cantidad;
        this.stockAnterior = stockAnterior;
        this.stockResultante = stockResultante;
        this.responsable = responsable;
        this.usuario = usuario;
        this.documentoReferencia = documentoReferencia;
        this.observacion = observacion;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public String getCodigoMovimiento() {
        return codigoMovimiento;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
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

    public int getStockAnterior() {
        return stockAnterior;
    }

    public int getStockResultante() {
        return stockResultante;
    }

    public String getResponsable() {
        return responsable;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getDocumentoReferencia() {
        return documentoReferencia;
    }

    public String getObservacion() {
        return observacion;
    }
}

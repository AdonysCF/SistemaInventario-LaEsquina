package modelo;

public class DetalleMovimiento {
    
    private int idDetalleMov;
    private int idMovimiento;
    private int idProducto;
    private int cantidad;
    private int stockAnterior;
    private int stockResultante;

    public DetalleMovimiento() {
    }

    public int getIdDetalleMov() {
        return idDetalleMov;
    }

    public void setIdDetalleMov(int idDetalleMov) {
        this.idDetalleMov = idDetalleMov;
    }

    public int getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(int idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getStockAnterior() {
        return stockAnterior;
    }

    public void setStockAnterior(int stockAnterior) {
        this.stockAnterior = stockAnterior;
    }

    public int getStockResultante() {
        return stockResultante;
    }

    public void setStockResultante(int stockResultante) {
        this.stockResultante = stockResultante;
    }
}
package modelo;

public class ReporteReposicionFila {

    private final String sku;
    private final String producto;
    private final String categoria;
    private final int stockActual;
    private final int stockMinimo;
    private final long unidadesVendidas;

    public ReporteReposicionFila(String sku, String producto, String categoria,
            int stockActual, int stockMinimo, long unidadesVendidas) {
        this.sku = sku;
        this.producto = producto;
        this.categoria = categoria;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.unidadesVendidas = unidadesVendidas;
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

    public int getStockActual() {
        return stockActual;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public int getDeficit() {
        return Math.max(0, stockMinimo - stockActual);
    }

    public boolean isAgotado() {
        return stockActual == 0;
    }

    public long getUnidadesVendidas() {
        return unidadesVendidas;
    }
}

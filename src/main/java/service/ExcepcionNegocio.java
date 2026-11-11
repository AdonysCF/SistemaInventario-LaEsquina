
package service;

public class ExcepcionNegocio extends Exception {

    public enum Tipo {
        DATOS_INVALIDOS,
        PRODUCTO_NO_ENCONTRADO,
        STOCK_INSUFICIENTE,
        OPERACION_NO_PERMITIDA
    }

    private final Tipo tipo;

    public ExcepcionNegocio(Tipo tipo, String mensaje) {
        super(mensaje);
        this.tipo = tipo;
    }

    public ExcepcionNegocio(Tipo tipo, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.tipo = tipo;
    }

    public Tipo getTipo() {
        return tipo;
    }
}

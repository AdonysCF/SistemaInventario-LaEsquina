
package service;

public class ExcepcionPersistencia extends RuntimeException {

    public ExcepcionPersistencia(String mensaje) {
        super(mensaje);
    }

    public ExcepcionPersistencia(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

package service;

import dao.MovimientoDAO;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import modelo.KardexRegistro;

public class KardexService {

    private static final int FILAS_POR_PAGINA = 50;
    private static final Set<String> TIPOS = Set.of("", "ENTRADA_REPOSICION",
            "SALIDA_VENTA", "AJUSTE", "ANULACION");
    private static final LocalDate PRIMERA_FECHA_SQL = LocalDate.of(1753, 1, 1);
    private static final LocalDate ULTIMA_FECHA_SQL = LocalDate.of(9999, 12, 31);

    private final MovimientoDAO movimientoDAO = new MovimientoDAO();

    public Pagina consultar(String desde, String hasta, String sku, String tipo,
            String responsable, String paginaSolicitada) {

        if (!TIPOS.contains(tipo)) {
            throw new IllegalArgumentException("Seleccione un tipo de movimiento válido.");
        }
        if (sku.length() > 30 || responsable.length() > 50) {
            throw new IllegalArgumentException("Revise la longitud del SKU o del nombre de usuario.");
        }

        LocalDate inicio = leerFecha(desde);
        LocalDate fin = leerFecha(hasta);
        if (inicio != null && fin != null && inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha Desde debe ser anterior o igual a Hasta.");
        }

        // El límite exclusivo incluye todos los movimientos del último día seleccionado.
        LocalDate finExclusivo = fin == null || ULTIMA_FECHA_SQL.equals(fin)
                ? null : fin.plusDays(1);

        int pagina = leerPagina(paginaSolicitada);
        long desplazamiento = (pagina - 1L) * FILAS_POR_PAGINA;
        List<KardexRegistro> registros = movimientoDAO.consultarKardex(
                inicio, finExclusivo, sku, tipo, responsable,
                desplazamiento, FILAS_POR_PAGINA + 1);

        boolean haySiguiente = registros.size() > FILAS_POR_PAGINA;
        List<KardexRegistro> visibles = haySiguiente
                ? registros.subList(0, FILAS_POR_PAGINA) : registros;
        return new Pagina(pagina, List.copyOf(visibles), haySiguiente);
    }

    private static LocalDate leerFecha(String valor) {
        if (valor.isEmpty()) {
            return null;
        }

        LocalDate fecha;
        try {
            fecha = LocalDate.parse(valor);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Ingrese fechas válidas.", ex);
        }
        if (fecha.isBefore(PRIMERA_FECHA_SQL) || fecha.isAfter(ULTIMA_FECHA_SQL)) {
            throw new IllegalArgumentException("Ingrese fechas entre 1753 y 9999.");
        }
        return fecha;
    }

    private static int leerPagina(String valor) {
        if (valor.isEmpty()) {
            return 1;
        }
        try {
            int pagina = Integer.parseInt(valor);
            if (pagina >= 1 && pagina < Integer.MAX_VALUE) {
                return pagina;
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El número de página no es válido.", ex);
        }
        throw new IllegalArgumentException("El número de página no es válido.");
    }

    public record Pagina(int numero, List<KardexRegistro> registros, boolean haySiguiente) {
    }
}

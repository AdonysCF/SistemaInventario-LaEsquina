package service;

import dao.VentaDAO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import modelo.VentaResumen;

public class HistorialVentaService {

    private static final int POR_PAGINA = 25;
    private static final LocalDate FECHA_MINIMA = LocalDate.of(1753, 1, 1);
    private static final LocalDate FECHA_MAXIMA = LocalDate.of(9999, 12, 31);

    private final VentaDAO ventaDAO = new VentaDAO();

    public Pagina buscar(String desde, String hasta, String ticket, String responsable,
            String paginaSolicitada) {
        if (ticket.length() > 25 || responsable.length() > 50) {
            throw new IllegalArgumentException("Revisa la longitud del ticket y del nombre de usuario.");
        }
        LocalDate inicio = leerFecha(desde);
        LocalDate fin = leerFecha(hasta);
        if (inicio != null && fin != null && inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha Desde debe ser anterior o igual a Hasta.");
        }

        LocalDateTime inicioInclusivo = inicio == null ? null : inicio.atStartOfDay();
        LocalDateTime finExclusivo = fin == null || fin.equals(FECHA_MAXIMA)
                ? null : fin.plusDays(1).atStartOfDay();
        int pagina = leerIdPositivo(paginaSolicitada.isEmpty() ? "1" : paginaSolicitada,
                "El número de página no es válido.");
        List<VentaResumen> ventas = ventaDAO.listarHistorial(inicioInclusivo, finExclusivo,
                ticket, responsable, (pagina - 1L) * POR_PAGINA, POR_PAGINA + 1);
        boolean haySiguiente = ventas.size() > POR_PAGINA && pagina < Integer.MAX_VALUE;
        return new Pagina(pagina, List.copyOf(ventas.subList(0, Math.min(ventas.size(), POR_PAGINA))),
                haySiguiente);
    }

    public Optional<VentaDAO.DetalleConsultado> buscarDetalle(String idVenta) {
        return ventaDAO.consultarDetalle(leerIdPositivo(idVenta, "El identificador de venta no es válido."));
    }

    private static LocalDate leerFecha(String valor) {
        if (valor.isEmpty()) {
            return null;
        }
        try {
            LocalDate fecha = LocalDate.parse(valor);
            if (fecha.isBefore(FECHA_MINIMA) || fecha.isAfter(FECHA_MAXIMA)) {
                throw new IllegalArgumentException("Ingresa fechas entre 1753 y 9999.");
            }
            return fecha;
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Ingresa fechas válidas.", ex);
        }
    }

    private static int leerIdPositivo(String valor, String mensaje) {
        try {
            int numero = Integer.parseInt(valor);
            if (numero > 0) {
                return numero;
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(mensaje, ex);
        }
        throw new IllegalArgumentException(mensaje);
    }

    public record Pagina(int numero, List<VentaResumen> ventas, boolean haySiguiente) {
    }
}

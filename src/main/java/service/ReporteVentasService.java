package service;

import dao.VentaDAO;
import dao.VentaDAO.ReporteConsolidado;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class ReporteVentasService {

    private static final LocalDate FECHA_MINIMA = LocalDate.of(1753, 1, 1);
    private static final LocalDate FECHA_MAXIMA = LocalDate.of(9999, 12, 31);
    private final VentaDAO ventaDAO = new VentaDAO();

    public ReporteConsolidado generar(String desde, String hasta) {
        if (desde == null || desde.isBlank() || hasta == null || hasta.isBlank()) {
            throw new IllegalArgumentException("Selecciona la fecha de inicio y la fecha de fin.");
        }

        LocalDate inicio = leerFecha(desde);
        LocalDate fin = leerFecha(hasta);
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha Desde debe ser anterior o igual a Hasta.");
        }

        // Consultar hasta la medianoche siguiente incluye todo el último día.
        LocalDateTime finExclusivo = fin.equals(FECHA_MAXIMA)
                ? null : fin.plusDays(1).atStartOfDay();
        return ventaDAO.consultarReporteVentas(inicio.atStartOfDay(), finExclusivo);
    }

    private LocalDate leerFecha(String valor) {
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(valor.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Ingresa fechas válidas.", ex);
        }

        if (fecha.isBefore(FECHA_MINIMA) || fecha.isAfter(FECHA_MAXIMA)) {
            throw new IllegalArgumentException("Ingresa fechas entre 1753 y 9999.");
        }
        return fecha;
    }
}

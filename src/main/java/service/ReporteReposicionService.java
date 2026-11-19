package service;

import dao.ReporteReposicionDAO;
import java.util.List;
import modelo.ReporteReposicionFila;

public class ReporteReposicionService {

    private static final int DIAS_ROTACION = 30;
    private final ReporteReposicionDAO reporteDAO = new ReporteReposicionDAO();

    public Reporte generar() {
        return new Reporte(DIAS_ROTACION, reporteDAO.consultarProductosCriticos(DIAS_ROTACION));
    }

    public record Reporte(int diasRotacion, List<ReporteReposicionFila> filas) {
    }
}

package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.Usuario;
import service.ExcepcionPersistencia;
import service.ReporteReposicionService;

@WebServlet(name = "ReporteReposicionServlet", urlPatterns = "/ReporteReposicionServlet")
public class ReporteReposicionServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ReporteReposicionServlet.class.getName());
    private final ReporteReposicionService reporteService = new ReporteReposicionService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession sesion = request.getSession(false);
        if (sesion == null || !(sesion.getAttribute("usuarioSesion") instanceof Usuario usuario)) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }
        if (usuario.getRol() == null || !"GERENTE".equalsIgnoreCase(usuario.getRol().trim())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            var reporte = reporteService.generar();
            request.setAttribute("diasRotacion", reporte.diasRotacion());
            request.setAttribute("filas", reporte.filas());
        } catch (ExcepcionPersistencia ex) {
            LOGGER.log(Level.SEVERE, "Error al generar el reporte de reposiciones.", ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("errorReporte", "No se pudo consultar el reporte. Intenta nuevamente.");
        }

        request.getRequestDispatcher("/reporteReposiciones.jsp").forward(request, response);
    }
}

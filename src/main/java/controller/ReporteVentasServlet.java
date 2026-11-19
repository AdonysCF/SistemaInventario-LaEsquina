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
import service.ReporteVentasService;

@WebServlet(name = "ReporteVentasServlet", urlPatterns = "/ReporteVentasServlet")
public class ReporteVentasServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ReporteVentasServlet.class.getName());
    private final ReporteVentasService reporteService = new ReporteVentasService();

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

        String desde = request.getParameter("desde");
        String hasta = request.getParameter("hasta");
        if (desde != null || hasta != null) {
            request.setAttribute("desde", desde);
            request.setAttribute("hasta", hasta);
            try {
                var reporte = reporteService.generar(desde, hasta);
                request.setAttribute("resumen", reporte.total());
                request.setAttribute("porProducto", reporte.porProducto());
                request.setAttribute("porCategoria", reporte.porCategoria());
            } catch (IllegalArgumentException ex) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                request.setAttribute("errorReporte", ex.getMessage());
            } catch (ExcepcionPersistencia ex) {
                LOGGER.log(Level.SEVERE, "Error al generar el reporte de ventas.", ex);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                request.setAttribute("errorReporte", "No se pudo generar el reporte. Intenta nuevamente.");
            }
        }

        request.getRequestDispatcher("/reporteVentas.jsp").forward(request, response);
    }
}

package controller;

import dao.VentaDAO.DetalleConsultado;
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
import service.HistorialVentaService;

@WebServlet(name = "HistorialVentaServlet", urlPatterns = "/HistorialVentaServlet")
public class HistorialVentaServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(HistorialVentaServlet.class.getName());
    private final HistorialVentaService historialService = new HistorialVentaService();

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

        String desde = parametro(request, "desde");
        String hasta = parametro(request, "hasta");
        String ticket = parametro(request, "ticket");
        String responsable = parametro(request, "responsable");
        String pagina = parametro(request, "pagina");
        request.setAttribute("desde", desde);
        request.setAttribute("hasta", hasta);
        request.setAttribute("ticket", ticket);
        request.setAttribute("responsable", responsable);
        request.setAttribute("pagina", pagina.isEmpty() ? "1" : pagina);

        try {
            if (request.getParameter("idVenta") != null) {
                DetalleConsultado detalle = historialService
                        .buscarDetalle(parametro(request, "idVenta")).orElse(null);
                if (detalle == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    request.setAttribute("errorHistorial", "No se encontró la venta solicitada.");
                } else {
                    request.setAttribute("venta", detalle.venta());
                    request.setAttribute("productos", detalle.productos());
                    if ("CONFIRMADA".equals(detalle.venta().getEstado()) && !detalle.productos().isEmpty()) {
                        request.setAttribute("tokenAnulacion", AnulacionVentaServlet.obtenerToken(sesion));
                    }
                    var resultado = AnulacionVentaServlet.consumirResultado(sesion, detalle.venta().getIdVenta());
                    if (resultado != null) {
                        request.setAttribute("anulacionExitosa", resultado.exito());
                        request.setAttribute("mensajeAnulacion", resultado.mensaje());
                    }
                    request.getRequestDispatcher("/detalleVenta.jsp").forward(request, response);
                    return;
                }
            } else {
                HistorialVentaService.Pagina resultado =
                        historialService.buscar(desde, hasta, ticket, responsable, pagina);
                request.setAttribute("ventas", resultado.ventas());
                request.setAttribute("pagina", resultado.numero());
                request.setAttribute("haySiguiente", resultado.haySiguiente());
            }
        } catch (IllegalArgumentException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("errorHistorial", ex.getMessage());
        } catch (ExcepcionPersistencia ex) {
            LOGGER.log(Level.SEVERE, "Error al consultar las ventas.", ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("errorHistorial", "No se pudo consultar la información. Intenta nuevamente.");
        }

        request.getRequestDispatcher("/historialVentas.jsp").forward(request, response);
    }

    private static String parametro(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        return valor == null ? "" : valor.trim();
    }
}

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
import service.KardexService;

@WebServlet(name = "KardexServlet", urlPatterns = "/KardexServlet")
public class KardexServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(KardexServlet.class.getName());

    private final KardexService kardexService = new KardexService();

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
        String sku = parametro(request, "sku");
        String tipo = parametro(request, "tipo");
        String responsable = parametro(request, "responsable");

        request.setAttribute("desde", desde);
        request.setAttribute("hasta", hasta);
        request.setAttribute("sku", sku);
        request.setAttribute("tipo", tipo);
        request.setAttribute("responsable", responsable);

        try {
            KardexService.Pagina resultado = kardexService.consultar(
                    desde, hasta, sku, tipo, responsable, parametro(request, "pagina"));
            request.setAttribute("pagina", resultado.numero());
            request.setAttribute("haySiguiente", resultado.haySiguiente());
            request.setAttribute("filas", resultado.registros());
        } catch (IllegalArgumentException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("errorKardex", ex.getMessage());
        } catch (ExcepcionPersistencia ex) {
            LOGGER.log(Level.SEVERE, "Error al consultar el Kardex.", ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("errorKardex", "No se pudo cargar el historial. Intente nuevamente.");
        }

        request.getRequestDispatcher("/kardex.jsp").forward(request, response);
    }

    private static String parametro(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        return valor == null ? "" : valor.trim();
    }
}

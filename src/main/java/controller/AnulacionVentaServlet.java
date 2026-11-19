package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.Usuario;
import service.AnulacionVentaService;
import service.ExcepcionNegocio;
import service.ExcepcionPersistencia;

@WebServlet(name = "AnulacionVentaServlet", urlPatterns = "/AnulacionVentaServlet")
public class AnulacionVentaServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AnulacionVentaServlet.class.getName());
    private static final String TOKEN_SESION = "tokenAnulacionVenta";
    private static final String RESULTADO_SESION = "resultadoAnulacionVenta";

    private final AnulacionVentaService anulacionService = new AnulacionVentaService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
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

        request.setCharacterEncoding("UTF-8");
        Object token = sesion.getAttribute(TOKEN_SESION);
        if (!(token instanceof String) || !token.equals(request.getParameter("tokenAnulacion"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Vuelve a abrir el detalle de la venta.");
            return;
        }

        int idVenta;
        try {
            idVenta = Integer.parseInt(parametro(request, "idVenta"));
            if (idVenta <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "El identificador de venta no es válido.");
            return;
        }

        Resultado resultado;
        try {
            anulacionService.anularVenta(idVenta, usuario.getIdUsuario(), request.getParameter("motivo"));
            resultado = new Resultado(idVenta, true,
                    "Venta anulada. El stock se devolvió y la operación quedó registrada en Kardex.");
        } catch (ExcepcionNegocio ex) {
            resultado = new Resultado(idVenta, false, ex.getMessage());
        } catch (ExcepcionPersistencia ex) {
            LOGGER.log(Level.SEVERE, "Error al anular la venta " + idVenta, ex);
            resultado = new Resultado(idVenta, false,
                    "No se pudo confirmar el resultado de la anulación. Revisa el estado de la venta.");
        }
        synchronized (sesion) {
            sesion.setAttribute(RESULTADO_SESION, resultado);
        }

        StringBuilder destino = new StringBuilder(request.getContextPath())
                .append("/HistorialVentaServlet?idVenta=").append(idVenta);
        agregarFiltro(destino, "desde", parametro(request, "desde"), 10);
        agregarFiltro(destino, "hasta", parametro(request, "hasta"), 10);
        agregarFiltro(destino, "ticket", parametro(request, "ticket"), 25);
        agregarFiltro(destino, "responsable", parametro(request, "responsable"), 50);
        agregarFiltro(destino, "pagina", parametro(request, "pagina"), 10);
        response.sendRedirect(destino.toString());
    }

    static String obtenerToken(HttpSession sesion) {
        synchronized (sesion) {
            Object valor = sesion.getAttribute(TOKEN_SESION);
            if (valor instanceof String token) {
                return token;
            }
            String token = UUID.randomUUID().toString();
            sesion.setAttribute(TOKEN_SESION, token);
            return token;
        }
    }

    static Resultado consumirResultado(HttpSession sesion, int idVenta) {
        synchronized (sesion) {
            Object valor = sesion.getAttribute(RESULTADO_SESION);
            if (valor instanceof Resultado resultado && resultado.idVenta() == idVenta) {
                sesion.removeAttribute(RESULTADO_SESION);
                return resultado;
            }
            return null;
        }
    }

    private static String parametro(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        return valor == null ? "" : valor.trim();
    }

    private static void agregarFiltro(StringBuilder url, String nombre, String valor, int maximo) {
        if (!valor.isEmpty() && valor.length() <= maximo) {
            url.append('&').append(nombre).append('=')
                    .append(URLEncoder.encode(valor, StandardCharsets.UTF_8));
        }
    }

    record Resultado(int idVenta, boolean exito, String mensaje) {
    }
}

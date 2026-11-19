package controller;

import dao.ProductoDAO;
import java.io.IOException;
import java.util.Set;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import modelo.Usuario;
import service.ExcepcionPersistencia;

@WebServlet(name = "ConsultaServlet", urlPatterns = {"/ConsultaServlet"})
public class ConsultaServlet extends HttpServlet {

    private static final Set<String> ESTADOS_VALIDOS = Set.of(
            "", "DISPONIBLE", "PROXIMO_A_AGOTARSE", "AGOTADO");

    private final ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sesion = request.getSession(false);
        if (sesion == null || !(sesion.getAttribute("usuarioSesion") instanceof Usuario)) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        String buscar = request.getParameter("txtBuscar");
        buscar = buscar == null ? "" : buscar.trim();

        String estado = request.getParameter("estado");
        estado = estado == null ? "" : estado.trim();

        request.setAttribute("busquedaAnterior", buscar);
        if (!ESTADOS_VALIDOS.contains(estado)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("errorConsulta", "Seleccione un estado de stock válido.");
        } else {
            request.setAttribute("estadoSeleccionado", estado);
            try {
                request.setAttribute("listaResultados",
                        productoDAO.consultarExistencias(buscar, estado));
            } catch (ExcepcionPersistencia ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                request.setAttribute("errorConsulta",
                        "No se pudieron consultar las existencias. Intente nuevamente.");
            }
        }

        request.getRequestDispatcher("/consultarProductos.jsp").forward(request, response);
    }
}

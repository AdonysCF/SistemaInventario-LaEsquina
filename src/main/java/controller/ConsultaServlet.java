package controller;

import dao.ProductoDAO;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modelo.Producto;
import modelo.Usuario;

@WebServlet(name = "ConsultaServlet", urlPatterns = {"/ConsultaServlet"})
public class ConsultaServlet extends HttpServlet {

    private ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Verificación de seguridad
        Usuario usr = (Usuario) request.getSession().getAttribute("usuarioSesion");
        if (usr == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        String buscar = request.getParameter("txtBuscar");
        List<Producto> resultados;

        if (buscar != null && !buscar.trim().isEmpty()) {
            resultados = productoDAO.buscarPorCriterio(buscar.trim());
        } else {
            // Si no hay búsqueda, mostramos todo el catálogo activo 
            resultados = productoDAO.listarTodosActivos();
        }

        request.setAttribute("listaResultados", resultados);
        request.setAttribute("busquedaAnterior", buscar);
        
        request.getRequestDispatcher("consultarProductos.jsp").forward(request, response);
    }
}
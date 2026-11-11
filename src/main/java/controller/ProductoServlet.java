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

@WebServlet(name = "ProductoServlet", urlPatterns = {"/ProductoServlet"})
public class ProductoServlet extends HttpServlet {

    ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String accion = request.getParameter("accion");
        
        if (accion == null || accion.isEmpty()) {
            accion = "listar";
        }
        
        switch (accion) {
            case "listar":
                List<Producto> lista = productoDAO.listarTodosActivos();
                request.setAttribute("listaProductos", lista);
                // Redirige a la vista del catálogo
                request.getRequestDispatcher("catalogoProductos.jsp").forward(request, response);
                break;
            // Aquí agregarás "editar" y "eliminar"
            default:
                response.sendRedirect("panelGerente.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Aquí recibirás los datos del formulario (POST) para llamar a registrarProducto()
    }
}
package controller;

import dao.ProductoDAO;
import service.InventarioService;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modelo.DetalleMovimiento;
import modelo.MovimientoInventario;
import modelo.Usuario;

@WebServlet(name = "EntradaServlet", urlPatterns = {"/EntradaServlet"})
public class EntradaServlet extends HttpServlet {

    private ProductoDAO productoDAO = new ProductoDAO();
    private InventarioService inventarioService = new InventarioService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Carga los productos activos para mostrarlos en el desplegable del formulario
        request.setAttribute("listaProductos", productoDAO.listarTodosActivos());
        request.getRequestDispatcher("registrarEntrada.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Usuario usr = (Usuario) request.getSession().getAttribute("usuarioSesion");
        if (usr == null) {
            response.sendRedirect("index.jsp"); return;
        }

        // Recuperar datos del formulario
        String docRef = request.getParameter("txtDocumento");
        String observacion = request.getParameter("txtObservacion");
        String[] idsProductos = request.getParameterValues("idProducto[]");
        String[] cantidades = request.getParameterValues("cantidad[]");

        // Armar el objeto Movimiento
        MovimientoInventario mov = new MovimientoInventario();
        mov.setCodigoMovimiento("ENT-" + System.currentTimeMillis());
        mov.setTipoMovimiento("ENTRADA_REPOSICION");
        mov.setDocumentoReferencia(docRef);
        mov.setObservacion(observacion);
        mov.setIdUsuario(usr.getIdUsuario());

        // Llenar el detalle
        if (idsProductos != null) {
            for (int i = 0; i < idsProductos.length; i++) {
                DetalleMovimiento det = new DetalleMovimiento();
                det.setIdProducto(Integer.parseInt(idsProductos[i]));
                det.setCantidad(Integer.parseInt(cantidades[i]));
                mov.agregarDetalle(det);
            }
        }

        // Ejecutar transacción
        boolean exito = inventarioService.registrarEntrada(mov);

        if (exito) {
            request.setAttribute("mensajeExito", "Entrada registrada exitosamente. Inventario actualizado.");
        } else {
            request.setAttribute("mensajeError", "Error al procesar la entrada. Revise los datos.");
        }
        
        doGet(request, response); // Recargar la página
    }
}
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

@WebServlet(name = "AjusteServlet", urlPatterns = {"/AjusteServlet"})
public class AjusteServlet extends HttpServlet {

    private ProductoDAO productoDAO = new ProductoDAO();
    private InventarioService inventarioService = new InventarioService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Usuario usr = (Usuario) request.getSession().getAttribute("usuarioSesion");
        if (usr == null || !usr.getRol().equals("GERENTE")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso Denegado. Solo Gerencia.");
            return;
        }

        request.setAttribute("listaProductos", productoDAO.listarTodosActivos());
        request.getRequestDispatcher("registrarAjuste.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Usuario usr = (Usuario) request.getSession().getAttribute("usuarioSesion");
        if (usr == null || !usr.getRol().equals("GERENTE")) {
            response.sendRedirect("index.jsp"); return;
        }

        String tipoAjuste = request.getParameter("cboTipoAjuste");
        String motivo = request.getParameter("txtMotivo");
        String idProducto = request.getParameter("cboProducto");
        String cantidadStr = request.getParameter("txtCantidad");

        if (motivo == null || motivo.trim().isEmpty() || cantidadStr == null || cantidadStr.trim().isEmpty()) {
             request.setAttribute("mensajeError", "El motivo y la cantidad son obligatorios.");
             doGet(request, response);
             return;
        }

        int cantidad = Integer.parseInt(cantidadStr);
        if (cantidad <= 0) {
             request.setAttribute("mensajeError", "La cantidad debe ser mayor a cero.");
             doGet(request, response);
             return;
        }

        MovimientoInventario mov = new MovimientoInventario();
        mov.setCodigoMovimiento("AJU-" + System.currentTimeMillis());
        mov.setTipoMovimiento(tipoAjuste); // AJUSTE_POSITIVO o AJUSTE_NEGATIVO
        mov.setDocumentoReferencia("Ajuste Manual");
        mov.setObservacion(motivo);
        mov.setIdUsuario(usr.getIdUsuario());

        DetalleMovimiento det = new DetalleMovimiento();
        det.setIdProducto(Integer.parseInt(idProducto));
        det.setCantidad(cantidad);
        mov.agregarDetalle(det);

        String resultado = inventarioService.registrarAjuste(mov);

        if (resultado.startsWith("EXITO")) {
            request.setAttribute("mensajeExito", "Ajuste registrado. Inventario actualizado.");
        } else {
            request.setAttribute("mensajeError", resultado);
        }
        
        doGet(request, response);
    }
}
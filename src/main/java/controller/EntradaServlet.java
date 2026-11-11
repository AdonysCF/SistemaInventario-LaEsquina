package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modelo.DetalleMovimiento;
import modelo.MovimientoInventario;
import modelo.Usuario;
import service.InventarioService;

@WebServlet(name = "EntradaServlet", urlPatterns = {"/EntradaServlet"})
public class EntradaServlet extends HttpServlet {

    private InventarioService inventarioService = new InventarioService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Validar que exista la sesión
        if (request.getSession().getAttribute("usuarioSesion") == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        // 1. Instanciar el DAO de productos
        dao.ProductoDAO productoDAO = new dao.ProductoDAO();
        
        // 2. Obtener la lista de todos los productos activos
        List<modelo.Producto> listaProductos = productoDAO.listarTodosActivos();
        
        // 3. Guardar la lista en el request para que el JSP la pueda leer
        request.setAttribute("listaProductos", listaProductos);
        
        // 4. Enviar (forward) a la página registrarEntrada.jsp junto con los datos
        request.getRequestDispatcher("registrarEntrada.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Validar la sesión
        Usuario usr = (Usuario) request.getSession().getAttribute("usuarioSesion");
        if (usr == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        // 2. Obtener la cabecera del formulario
        String documento = request.getParameter("txtDocumento");
        String observacion = request.getParameter("txtObservacion");

        // 3. Obtener los detalles del ingreso
        String[] idProductos = request.getParameterValues("idProducto[]");
        String[] cantidades = request.getParameterValues("cantidad[]");

        if (documento == null || documento.trim().isEmpty() || idProductos == null || idProductos.length == 0) {
            request.getSession().setAttribute("mensajeError", "Debe indicar el documento de referencia y agregar al menos un producto.");
            response.sendRedirect("registrarEntrada.jsp");
            return;
        }

        try {
            // 4. Crear cabecera del Movimiento
            MovimientoInventario mov = new MovimientoInventario();
            mov.setCodigoMovimiento("E-" + System.currentTimeMillis());
            mov.setTipoMovimiento("ENTRADA_REPOSICION");
            mov.setDocumentoReferencia(documento);
            mov.setObservacion(observacion != null ? observacion : "");
            mov.setIdUsuario(usr.getIdUsuario());

            // 5. Llenar los detalles
            List<DetalleMovimiento> listaDetalles = new ArrayList<>();
            for (int i = 0; i < idProductos.length; i++) {
                int idProd = Integer.parseInt(idProductos[i]);
                int cant = Integer.parseInt(cantidades[i]);

                DetalleMovimiento det = new DetalleMovimiento();
                det.setIdProducto(idProd);
                det.setCantidad(cant);
                listaDetalles.add(det);
            }

            // 6. Ejecutar el Servicio Transaccional (AC-02)
            boolean exito = inventarioService.registrarEntradaTransaccional(mov, listaDetalles);

            // 7. Enviar la respuesta a la vista
            if (exito) {
                request.getSession().setAttribute("mensajeExito", "Entrada de productos registrada exitosamente. Movimiento N°: " + mov.getCodigoMovimiento());
            } else {
                request.getSession().setAttribute("mensajeError", "Error al procesar el ingreso. No se aplicaron cambios en el inventario.");
            }

        } catch (Exception e) {
            request.getSession().setAttribute("mensajeError", "Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }

        response.sendRedirect("registrarEntrada.jsp");
    }
}
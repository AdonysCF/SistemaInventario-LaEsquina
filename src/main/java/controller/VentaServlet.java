package controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modelo.DetalleVenta;
import modelo.Usuario;
import modelo.Venta;
import service.VentaService;

@WebServlet(name = "VentaServlet", urlPatterns = {"/VentaServlet"})
public class VentaServlet extends HttpServlet {

    private VentaService ventaService = new VentaService();

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
        
        // 4. Enviar (forward) a la página registrarVenta.jsp junto con los datos
        request.getRequestDispatcher("registrarVenta.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Validar la sesión del usuario (cajero)
        Usuario usr = (Usuario) request.getSession().getAttribute("usuarioSesion");
        if (usr == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        // 2. Recibir los arrays de datos desde el formulario (registrarVenta.jsp)
        String[] idProductos = request.getParameterValues("idProducto[]");
        String[] cantidades = request.getParameterValues("cantidad[]");
        String[] precios = request.getParameterValues("precioUnitario[]");

        if (idProductos == null || idProductos.length == 0) {
            request.getSession().setAttribute("mensajeError", "El carrito de ventas está vacío.");
            response.sendRedirect("registrarVenta.jsp");
            return;
        }

        try {
            // 3. Crear el objeto Venta (Cabecera)
            Venta venta = new Venta();
            venta.setNumeroTicket("V-" + System.currentTimeMillis()); // Ticket único
            venta.setIdUsuario(usr.getIdUsuario());
            
            BigDecimal totalVenta = BigDecimal.ZERO;
            List<DetalleVenta> listaDetalles = new ArrayList<>();

            // 4. Llenar los detalles de la venta
            for (int i = 0; i < idProductos.length; i++) {
                int idProd = Integer.parseInt(idProductos[i]);
                int cant = Integer.parseInt(cantidades[i]);
                BigDecimal precioU = new BigDecimal(precios[i]);
                BigDecimal subtotal = precioU.multiply(new BigDecimal(cant));
                
                totalVenta = totalVenta.add(subtotal);

                DetalleVenta det = new DetalleVenta();
                det.setIdProducto(idProd);
                det.setCantidad(cant);
                det.setPrecioUnitario(precioU);
                det.setSubtotal(subtotal);
                
                listaDetalles.add(det);
            }
            venta.setTotalVenta(totalVenta);

            // 5. Ejecutar el Servicio Transaccional (AC-02)
            boolean exito = ventaService.registrarVentaTransaccional(venta, listaDetalles, usr.getIdUsuario());

            // 6. Enviar la respuesta a la vista
            if (exito) {
                request.getSession().setAttribute("mensajeExito", "Venta registrada correctamente. Ticket: " + venta.getNumeroTicket());
            } else {
                request.getSession().setAttribute("mensajeError", "Error al procesar la venta. Verifique el stock disponible (simultaneidad).");
            }

        } catch (Exception e) {
            request.getSession().setAttribute("mensajeError", "Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }

        response.sendRedirect("registrarVenta.jsp");
    }
}
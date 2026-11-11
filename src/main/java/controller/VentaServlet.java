package controller;

import dao.ProductoDAO;
import service.VentaService;
import java.io.IOException;
import java.math.BigDecimal;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modelo.DetalleVenta;
import modelo.Usuario;
import modelo.Venta;

@WebServlet(name = "VentaServlet", urlPatterns = {"/VentaServlet"})
public class VentaServlet extends HttpServlet {

    private ProductoDAO productoDAO = new ProductoDAO();
    private VentaService ventaService = new VentaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Verificamos sesión (AC-03)
        Usuario usr = (Usuario) request.getSession().getAttribute("usuarioSesion");
        if (usr == null) {
            response.sendRedirect("index.jsp"); 
            return;
        }

        // Cargamos productos activos para el buscador de caja
        request.setAttribute("listaProductos", productoDAO.listarTodosActivos());
        request.getRequestDispatcher("registrarVenta.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Usuario usr = (Usuario) request.getSession().getAttribute("usuarioSesion");
        if (usr == null) {
            response.sendRedirect("index.jsp"); 
            return;
        }

        // Recuperar arrays de productos y cantidades desde el formulario
        String[] idsProductos = request.getParameterValues("idProducto[]");
        String[] cantidades = request.getParameterValues("cantidad[]");
        String[] precios = request.getParameterValues("precioUnitario[]");
        
        if (idsProductos == null || idsProductos.length == 0) {
             request.setAttribute("mensajeError", "El carrito de ventas está vacío.");
             doGet(request, response);
             return;
        }

        // Armar cabecera de la venta
        Venta venta = new Venta();
        venta.setNumeroTicket("T-" + System.currentTimeMillis());
        venta.setIdUsuario(usr.getIdUsuario());
        BigDecimal totalVenta = BigDecimal.ZERO;

        // Llenar detalles y calcular total
        for (int i = 0; i < idsProductos.length; i++) {
            DetalleVenta det = new DetalleVenta();
            det.setIdProducto(Integer.parseInt(idsProductos[i]));
            
            int cantidad = Integer.parseInt(cantidades[i]);
            BigDecimal precioU = new BigDecimal(precios[i]);
            BigDecimal subtotal = precioU.multiply(new BigDecimal(cantidad));
            
            det.setCantidad(cantidad);
            det.setPrecioUnitario(precioU);
            det.setSubtotal(subtotal);
            
            venta.agregarDetalle(det);
            totalVenta = totalVenta.add(subtotal);
        }
        
        venta.setTotalVenta(totalVenta);

        // Ejecutar transacción atómica (AC-02)
        String resultado = ventaService.registrarVenta(venta);

        if (resultado.startsWith("EXITO")) {
            String ticket = resultado.split(":")[1];
            request.setAttribute("mensajeExito", "Venta registrada con éxito. Ticket N.° " + ticket);
        } else {
            // El mensaje traerá el error de stock insuficiente (RN-02)
            request.setAttribute("mensajeError", resultado);
        }
        
        doGet(request, response);
    }
}
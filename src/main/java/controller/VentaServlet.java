
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
import jakarta.servlet.http.HttpSession;

import modelo.DetalleVenta;
import modelo.Producto;
import modelo.Usuario;
import modelo.Venta;

import service.ExcepcionNegocio;
import service.VentaService;

@WebServlet(name = "VentaServlet", urlPatterns = {"/VentaServlet"})
public class VentaServlet extends HttpServlet {

    private final VentaService ventaService = new VentaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("usuarioSesion") == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        cargarProductos(request);
        request.getRequestDispatcher("registrarVenta.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        Usuario usr = (Usuario) session.getAttribute("usuarioSesion");

        if (usr == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        String[] idProductos = request.getParameterValues("idProducto[]");
        String[] cantidades = request.getParameterValues("cantidad[]");
        String[] precios = request.getParameterValues("precioUnitario[]");

        if (idProductos == null || idProductos.length == 0
                || cantidades == null || precios == null) {

            session.setAttribute("mensajeError", "El carrito de ventas está vacío.");
            response.sendRedirect("registrarVenta.jsp");
            return;
        }

        if (idProductos.length != cantidades.length
                || idProductos.length != precios.length) {

            session.setAttribute(
                    "mensajeError",
                    "Los datos de los productos no coinciden. Verifique el carrito."
            );

            response.sendRedirect("registrarVenta.jsp");
            return;
        }

        try {
            Venta venta = new Venta();

            venta.setNumeroTicket("V-" + System.currentTimeMillis());
            venta.setIdUsuario(usr.getIdUsuario());

            BigDecimal totalVenta = BigDecimal.ZERO;
            List<DetalleVenta> listaDetalles = new ArrayList<>();

            for (int i = 0; i < idProductos.length; i++) {

                if (idProductos[i] == null || idProductos[i].trim().isEmpty()
                        || cantidades[i] == null || cantidades[i].trim().isEmpty()
                        || precios[i] == null || precios[i].trim().isEmpty()) {

                    throw new IllegalArgumentException(
                            "Todos los datos de los productos son obligatorios."
                    );
                }

                int idProd = Integer.parseInt(idProductos[i].trim());
                int cantidad = Integer.parseInt(cantidades[i].trim());
                BigDecimal precioUnitario = new BigDecimal(precios[i].trim());

                if (idProd <= 0 || cantidad <= 0 || precioUnitario.signum() < 0) {
                    throw new IllegalArgumentException(
                            "Verifique el identificador, cantidad y precio de los productos."
                    );
                }

                BigDecimal subtotal = precioUnitario.multiply(
                        BigDecimal.valueOf(cantidad)
                );

                totalVenta = totalVenta.add(subtotal);

                DetalleVenta detalle = new DetalleVenta();
                detalle.setIdProducto(idProd);
                detalle.setCantidad(cantidad);
                detalle.setPrecioUnitario(precioUnitario);
                detalle.setSubtotal(subtotal);

                listaDetalles.add(detalle);
            }

            venta.setTotalVenta(totalVenta);

            boolean exito = ventaService.registrarVentaTransaccional(
                    venta,
                    listaDetalles,
                    usr.getIdUsuario()
            );

            if (exito) {
                session.setAttribute(
                        "mensajeExito",
                        "Venta registrada correctamente. Ticket: "
                        + venta.getNumeroTicket()
                );
            } else {
                session.setAttribute(
                        "mensajeError",
                        "No se pudo registrar la venta debido a un error de persistencia."
                );
            }

        } catch (ExcepcionNegocio ex) {

            session.setAttribute("mensajeError", obtenerMensajeNegocio(ex));

        } catch (NumberFormatException ex) {

            session.setAttribute(
                    "mensajeError",
                    "Los identificadores, cantidades y precios deben tener un formato válido."
            );

        } catch (IllegalArgumentException ex) {

            session.setAttribute("mensajeError", ex.getMessage());

        } catch (Exception ex) {

            session.setAttribute(
                    "mensajeError",
                    "Ocurrió un error inesperado al procesar la venta."
            );

            getServletContext().log("Error inesperado en VentaServlet", ex);
        }

        response.sendRedirect("registrarVenta.jsp");
    }

    private void cargarProductos(HttpServletRequest request) {

        dao.ProductoDAO productoDAO = new dao.ProductoDAO();
        List<Producto> listaProductos = productoDAO.listarTodosActivos();

        request.setAttribute("listaProductos", listaProductos);
    }

    private String obtenerMensajeNegocio(ExcepcionNegocio ex) {

        switch (ex.getTipo()) {
            case DATOS_INVALIDOS:
                return ex.getMessage();

            case PRODUCTO_NO_ENCONTRADO:
                return "Uno de los productos seleccionados ya no existe o no está disponible.";

            case STOCK_INSUFICIENTE:
                return ex.getMessage();

            case OPERACION_NO_PERMITIDA:
                return ex.getMessage();

            default:
                return "No se pudo completar la venta por una regla de negocio.";
        }
    }
}

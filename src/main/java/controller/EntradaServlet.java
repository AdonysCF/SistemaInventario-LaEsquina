
package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import modelo.DetalleMovimiento;
import modelo.MovimientoInventario;
import modelo.Producto;
import modelo.Usuario;

import service.ExcepcionNegocio;
import service.InventarioService;

@WebServlet(name = "EntradaServlet", urlPatterns = {"/EntradaServlet"})
public class EntradaServlet extends HttpServlet {

    private final InventarioService inventarioService = new InventarioService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("usuarioSesion") == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        cargarProductos(request);
        request.getRequestDispatcher("registrarEntrada.jsp").forward(request, response);
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

        String documento = request.getParameter("txtDocumento");
        String observacion = request.getParameter("txtObservacion");

        String[] idProductos = request.getParameterValues("idProducto[]");
        String[] cantidades = request.getParameterValues("cantidad[]");

        if (documento == null || documento.trim().isEmpty()) {
            session.setAttribute(
                    "mensajeError",
                    "Debe indicar el documento de referencia."
            );

            response.sendRedirect("registrarEntrada.jsp");
            return;
        }

        if (idProductos == null || cantidades == null
                || idProductos.length == 0) {

            session.setAttribute(
                    "mensajeError",
                    "Debe agregar al menos un producto al ingreso."
            );

            response.sendRedirect("registrarEntrada.jsp");
            return;
        }

        if (idProductos.length != cantidades.length) {
            session.setAttribute(
                    "mensajeError",
                    "Los productos y sus cantidades no coinciden."
            );

            response.sendRedirect("registrarEntrada.jsp");
            return;
        }

        try {
            MovimientoInventario movimiento = new MovimientoInventario();

            movimiento.setCodigoMovimiento("E-" + System.currentTimeMillis());
            movimiento.setTipoMovimiento("ENTRADA_REPOSICION");
            movimiento.setDocumentoReferencia(documento.trim());
            movimiento.setObservacion(
                    observacion != null ? observacion.trim() : ""
            );
            movimiento.setIdUsuario(usr.getIdUsuario());

            List<DetalleMovimiento> listaDetalles = new ArrayList<>();

            for (int i = 0; i < idProductos.length; i++) {

                if (idProductos[i] == null || idProductos[i].trim().isEmpty()
                        || cantidades[i] == null || cantidades[i].trim().isEmpty()) {

                    throw new IllegalArgumentException(
                            "Todos los productos deben tener una cantidad válida."
                    );
                }

                int idProducto = Integer.parseInt(idProductos[i].trim());
                int cantidad = Integer.parseInt(cantidades[i].trim());

                if (idProducto <= 0 || cantidad <= 0) {
                    throw new IllegalArgumentException(
                            "El identificador y la cantidad deben ser mayores a cero."
                    );
                }

                DetalleMovimiento detalle = new DetalleMovimiento();
                detalle.setIdProducto(idProducto);
                detalle.setCantidad(cantidad);

                listaDetalles.add(detalle);
            }

            boolean exito = inventarioService.registrarEntradaTransaccional(
                    movimiento,
                    listaDetalles
            );

            if (exito) {
                session.setAttribute(
                        "mensajeExito",
                        "Entrada de productos registrada exitosamente. Movimiento N°: "
                        + movimiento.getCodigoMovimiento()
                );
            } else {
                session.setAttribute(
                        "mensajeError",
                        "No se pudo registrar la entrada. No se aplicaron cambios en el inventario."
                );
            }

        } catch (ExcepcionNegocio ex) {

            session.setAttribute("mensajeError", obtenerMensajeNegocio(ex));

        } catch (NumberFormatException ex) {

            session.setAttribute(
                    "mensajeError",
                    "Los identificadores y las cantidades deben ser números enteros válidos."
            );

        } catch (IllegalArgumentException ex) {

            session.setAttribute("mensajeError", ex.getMessage());

        } catch (Exception ex) {

            session.setAttribute(
                    "mensajeError",
                    "Ocurrió un error inesperado al procesar la entrada."
            );

            getServletContext().log("Error inesperado en EntradaServlet", ex);
        }

        response.sendRedirect("registrarEntrada.jsp");
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
                return "Uno de los productos seleccionados no existe o no está disponible.";

            case STOCK_INSUFICIENTE:
                return ex.getMessage();

            case OPERACION_NO_PERMITIDA:
                return ex.getMessage();

            default:
                return "No se pudo completar la entrada por una regla de negocio.";
        }
    }
}


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

    private static final String CLAVE_AVISO = "avisoRegistroEntrada";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("usuarioSesion") == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        cargarProductos(request);
        Aviso aviso;
        synchronized (session) {
            aviso = (Aviso) session.getAttribute(CLAVE_AVISO);
            session.removeAttribute(CLAVE_AVISO);
        }
        if (aviso != null) {
            request.setAttribute(aviso.exito() ? "mensajeExito" : "mensajeError", aviso.mensaje());
        }
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
            guardarAviso(session, false, "Debe indicar el documento de referencia.");

            response.sendRedirect(request.getContextPath() + "/EntradaServlet");
            return;
        }

        if (idProductos == null || cantidades == null
                || idProductos.length == 0) {

            guardarAviso(session, false, "Debe agregar al menos un producto al ingreso.");

            response.sendRedirect(request.getContextPath() + "/EntradaServlet");
            return;
        }

        if (idProductos.length != cantidades.length) {
            guardarAviso(session, false, "Los productos y sus cantidades no coinciden.");

            response.sendRedirect(request.getContextPath() + "/EntradaServlet");
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
                guardarAviso(session, true, "Entrada de productos registrada exitosamente. Movimiento N°: "
                        + movimiento.getCodigoMovimiento());
            } else {
                guardarAviso(session, false, "No se pudo registrar la entrada. No se aplicaron cambios en el inventario.");
            }

        } catch (ExcepcionNegocio ex) {

            guardarAviso(session, false, obtenerMensajeNegocio(ex));

        } catch (NumberFormatException ex) {

            guardarAviso(session, false, "Los identificadores y las cantidades deben ser números enteros válidos.");

        } catch (IllegalArgumentException ex) {

            guardarAviso(session, false, ex.getMessage());

        } catch (Exception ex) {

            guardarAviso(session, false, "Ocurrió un error inesperado al procesar la entrada.");

            getServletContext().log("Error inesperado en EntradaServlet", ex);
        }

        response.sendRedirect(request.getContextPath() + "/EntradaServlet");
    }

    private static void guardarAviso(HttpSession session, boolean exito, String mensaje) {
        synchronized (session) {
            session.setAttribute(CLAVE_AVISO, new Aviso(exito, mensaje));
        }
    }

    private record Aviso(boolean exito, String mensaje) {
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

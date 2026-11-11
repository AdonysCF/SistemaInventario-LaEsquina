
package controller;

import dao.ProductoDAO;
import service.ExcepcionNegocio;
import service.InventarioService;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import modelo.DetalleMovimiento;
import modelo.MovimientoInventario;
import modelo.Usuario;

@WebServlet(name = "AjusteServlet", urlPatterns = {"/AjusteServlet"})
public class AjusteServlet extends HttpServlet {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final InventarioService inventarioService = new InventarioService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario usr = obtenerUsuarioGerente(request);

        if (usr == null) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Acceso denegado. Solo puede ingresar Gerencia."
            );
            return;
        }

        mostrarFormulario(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        Usuario usr = obtenerUsuarioGerente(request);

        if (usr == null) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Acceso denegado. Solo puede registrar ajustes Gerencia."
            );
            return;
        }

        String tipoAjuste = request.getParameter("txtTipoAjusteFinal");

        if (tipoAjuste == null || tipoAjuste.trim().isEmpty()) {
            tipoAjuste = request.getParameter("selTipoAjuste");
        }

        String documento = request.getParameter("txtDocumento");
        String observacion = request.getParameter("txtObservacion");

        String[] idsProductos = request.getParameterValues("idProducto[]");
        String[] cantidades = request.getParameterValues("cantidad[]");

        if (!"AJUSTE_POSITIVO".equals(tipoAjuste)
                && !"AJUSTE_NEGATIVO".equals(tipoAjuste)) {

            mostrarError(
                    request,
                    response,
                    "Seleccione un tipo de ajuste válido."
            );
            return;
        }

        if (documento == null || documento.trim().isEmpty()) {
            mostrarError(
                    request,
                    response,
                    "El documento de sustento es obligatorio."
            );
            return;
        }

        if (observacion == null || observacion.trim().isEmpty()) {
            mostrarError(
                    request,
                    response,
                    "Debe ingresar el motivo detallado del ajuste."
            );
            return;
        }

        if (idsProductos == null || cantidades == null
                || idsProductos.length == 0
                || cantidades.length == 0) {

            mostrarError(
                    request,
                    response,
                    "Debe agregar al menos un producto al ajuste."
            );
            return;
        }

        if (idsProductos.length != cantidades.length) {
            mostrarError(
                    request,
                    response,
                    "Los productos y sus cantidades no coinciden."
            );
            return;
        }

        MovimientoInventario movimiento = new MovimientoInventario();

        movimiento.setCodigoMovimiento("AJU-" + System.currentTimeMillis());
        movimiento.setTipoMovimiento(tipoAjuste);
        movimiento.setDocumentoReferencia(documento.trim());
        movimiento.setObservacion(observacion.trim());
        movimiento.setIdUsuario(usr.getIdUsuario());

        Set<Integer> productosRegistrados = new HashSet<>();

        try {

            for (int i = 0; i < idsProductos.length; i++) {

                if (idsProductos[i] == null
                        || idsProductos[i].trim().isEmpty()
                        || cantidades[i] == null
                        || cantidades[i].trim().isEmpty()) {

                    mostrarError(
                            request,
                            response,
                            "Se encontró un producto o cantidad vacía."
                    );
                    return;
                }

                int idProducto = Integer.parseInt(idsProductos[i].trim());
                int cantidad = Integer.parseInt(cantidades[i].trim());

                if (idProducto <= 0) {
                    mostrarError(
                            request,
                            response,
                            "Se encontró un identificador de producto inválido."
                    );
                    return;
                }

                if (cantidad <= 0) {
                    mostrarError(
                            request,
                            response,
                            "Todas las cantidades deben ser mayores a cero."
                    );
                    return;
                }

                if (!productosRegistrados.add(idProducto)) {
                    mostrarError(
                            request,
                            response,
                            "No puede agregar el mismo producto más de una vez."
                    );
                    return;
                }

                DetalleMovimiento detalle = new DetalleMovimiento();
                detalle.setIdProducto(idProducto);
                detalle.setCantidad(cantidad);

                movimiento.agregarDetalle(detalle);
            }

            String resultado = inventarioService.registrarAjuste(movimiento);

            if (resultado != null && resultado.startsWith("EXITO")) {
                request.setAttribute("mensajeExito", resultado);
            } else {
                request.setAttribute(
                        "mensajeError",
                        resultado != null
                                ? resultado
                                : "No se pudo completar el ajuste."
                );
            }

        } catch (ExcepcionNegocio ex) {

            request.setAttribute("mensajeError", obtenerMensajeNegocio(ex));

        } catch (NumberFormatException ex) {

            request.setAttribute(
                    "mensajeError",
                    "Los identificadores y las cantidades deben ser números enteros válidos."
            );

        } catch (Exception ex) {

            request.setAttribute(
                    "mensajeError",
                    "Ocurrió un error inesperado al procesar el ajuste."
            );

            getServletContext().log("Error inesperado en AjusteServlet", ex);
        }

        mostrarFormulario(request, response);
    }

    private Usuario obtenerUsuarioGerente(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        Usuario usr = (Usuario) session.getAttribute("usuarioSesion");

        if (usr == null || !"GERENTE".equals(usr.getRol())) {
            return null;
        }

        return usr;
    }

    private void mostrarFormulario(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute(
                "listaProductos",
                productoDAO.listarTodosActivos()
        );

        request.getRequestDispatcher("registrarAjuste.jsp")
                .forward(request, response);
    }

    private void mostrarError(
            HttpServletRequest request,
            HttpServletResponse response,
            String mensaje)
            throws ServletException, IOException {

        request.setAttribute("mensajeError", mensaje);
        mostrarFormulario(request, response);
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
                return "No se pudo completar el ajuste por una regla de negocio.";
        }
    }
}

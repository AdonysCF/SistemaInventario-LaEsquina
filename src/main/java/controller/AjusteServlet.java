
package controller;

import dao.ProductoDAO;
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

        // 1. Obtener los datos generales del formulario.
        String tipoAjuste = request.getParameter("txtTipoAjusteFinal");

        // Si no existe el campo oculto, intentar leer el select.
        if (tipoAjuste == null || tipoAjuste.trim().isEmpty()) {
            tipoAjuste = request.getParameter("selTipoAjuste");
        }

        String documento = request.getParameter("txtDocumento");
        String observacion = request.getParameter("txtObservacion");

        // 2. Obtener los arreglos de productos y cantidades.
        String[] idsProductos = request.getParameterValues("idProducto[]");
        String[] cantidades = request.getParameterValues("cantidad[]");

        // 3. Validar el tipo de ajuste.
        if (!"AJUSTE_POSITIVO".equals(tipoAjuste)
                && !"AJUSTE_NEGATIVO".equals(tipoAjuste)) {

            mostrarError(request, response,
                    "Seleccione un tipo de ajuste válido.");
            return;
        }

        // 4. Validar el documento de sustento.
        if (documento == null || documento.trim().isEmpty()) {
            mostrarError(request, response,
                    "El documento de sustento es obligatorio.");
            return;
        }

        // 5. Validar la observación.
        if (observacion == null || observacion.trim().isEmpty()) {
            mostrarError(request, response,
                    "Debe ingresar el motivo detallado del ajuste.");
            return;
        }

        // 6. Comprobar que se hayan enviado productos.
        if (idsProductos == null || cantidades == null
                || idsProductos.length == 0
                || cantidades.length == 0) {

            mostrarError(request, response,
                    "Debe agregar al menos un producto al ajuste.");
            return;
        }

        // Ambos arreglos deben tener la misma cantidad de elementos.
        if (idsProductos.length != cantidades.length) {
            mostrarError(request, response,
                    "Los productos y sus cantidades no coinciden.");
            return;
        }

        // 7. Crear la cabecera del movimiento.
        MovimientoInventario mov = new MovimientoInventario();

        mov.setCodigoMovimiento("AJU-" + System.currentTimeMillis());
        mov.setTipoMovimiento(tipoAjuste);
        mov.setDocumentoReferencia(documento.trim());
        mov.setObservacion(observacion.trim());
        mov.setIdUsuario(usr.getIdUsuario());

        // 8. Procesar todos los productos enviados.
        Set<Integer> productosRegistrados = new HashSet<>();

        try {

            for (int i = 0; i < idsProductos.length; i++) {

                // Validar que los campos no estén vacíos.
                if (idsProductos[i] == null
                        || idsProductos[i].trim().isEmpty()
                        || cantidades[i] == null
                        || cantidades[i].trim().isEmpty()) {

                    mostrarError(request, response,
                            "Se encontró un producto o cantidad vacía.");
                    return;
                }

                int idProducto = Integer.parseInt(idsProductos[i].trim());
                int cantidad = Integer.parseInt(cantidades[i].trim());

                // Validar el identificador del producto.
                if (idProducto <= 0) {
                    mostrarError(request, response,
                            "Se encontró un identificador de producto inválido.");
                    return;
                }

                // Validar que la cantidad sea positiva.
                if (cantidad <= 0) {
                    mostrarError(request, response,
                            "Todas las cantidades deben ser mayores a cero.");
                    return;
                }

                // Evitar que un producto se envíe varias veces.
                if (!productosRegistrados.add(idProducto)) {
                    mostrarError(request, response,
                            "No puede agregar el mismo producto más de una vez.");
                    return;
                }

                // Crear el detalle del movimiento.
                DetalleMovimiento detalle = new DetalleMovimiento();

                detalle.setIdProducto(idProducto);
                detalle.setCantidad(cantidad);

                mov.agregarDetalle(detalle);
            }

        } catch (NumberFormatException ex) {

            mostrarError(request, response,
                    "Los identificadores y las cantidades deben ser números enteros válidos.");
            return;
        }

        // 9. Enviar el movimiento completo al servicio.
        String resultado = inventarioService.registrarAjuste(mov);

        // 10. Mostrar el resultado de la operación.
        if (resultado != null && resultado.startsWith("EXITO")) {

            request.setAttribute("mensajeExito", resultado);

        } else {

            request.setAttribute("mensajeError",
                    resultado != null
                            ? resultado
                            : "No se pudo completar el ajuste.");
        }

        // Volver a mostrar el formulario con la lista de productos.
        mostrarFormulario(request, response);
    }

    /**
     * Comprueba que exista una sesión con un usuario de rol GERENTE.
     */
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

    /**
     * Carga los productos activos y muestra el formulario.
     */
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

    /**
     * Establece un mensaje de error y vuelve a mostrar el formulario.
     */
    private void mostrarError(
            HttpServletRequest request,
            HttpServletResponse response,
            String mensaje)
            throws ServletException, IOException {

        request.setAttribute("mensajeError", mensaje);
        mostrarFormulario(request, response);
    }
}

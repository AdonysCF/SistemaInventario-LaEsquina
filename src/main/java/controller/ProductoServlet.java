
package controller;

import dao.CategoriaDAO;
import dao.ProductoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import modelo.Categoria;
import modelo.Producto;
import service.ProductoService;

@WebServlet(name = "ProductoServlet", urlPatterns = {"/ProductoServlet"})
public class ProductoServlet extends HttpServlet {

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ProductoService productoService = new ProductoService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");

        if (accion == null || accion.trim().isEmpty()) {
            accion = "listar";
        }

        if ("listar".equalsIgnoreCase(accion)) {
            List<Producto> lista = productoDAO.listarTodosActivos();
            List<Categoria> listaCategorias = categoriaDAO.listarActivas();

            request.setAttribute("listaProductos", lista);
            request.setAttribute("listaCategorias", listaCategorias);

            request.getRequestDispatcher(
                    "catalogoProductos.jsp"
            ).forward(request, response);

        } else {
            response.sendRedirect("ProductoServlet?accion=listar");
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String accion = request.getParameter("accion");

        try {
            if ("agregar".equalsIgnoreCase(accion)) {
                registrarProducto(request);

            } else if ("editar".equalsIgnoreCase(accion)) {
                editarProducto(request);

            } else {
                throw new IllegalArgumentException(
                        "La operación solicitada no es válida.");
            }

        } catch (IllegalArgumentException ex) {
            request.getSession().setAttribute(
                    "mensajeError", ex.getMessage());

        } catch (Exception ex) {
            getServletContext().log(
                    "Error al gestionar el catálogo de productos.", ex);

            request.getSession().setAttribute(
                    "mensajeError",
                    "Ocurrió un error al procesar el producto. "
                    + "Verifique los datos e intente nuevamente.");
        }

        response.sendRedirect("ProductoServlet?accion=listar");
    }

    private void registrarProducto(HttpServletRequest request) {

        String sku = request.getParameter("txtSku");
        String nombre = request.getParameter("txtNombre");
        String categoriaStr = request.getParameter("selCategoria");
        String precioStr = request.getParameter("numPrecio");
        String stockMinStr = request.getParameter("numStockMin");
        String rutaImagen = request.getParameter("txtImagen");

        int idCategoria = convertirEntero(
                categoriaStr, "Debe seleccionar una categoría.");

        BigDecimal precio = convertirPrecio(precioStr);

        int stockMinimo = convertirEntero(
                stockMinStr, "El stock mínimo debe ser un número entero.");

        Producto producto = new Producto();
        producto.setCodigoSku(sku);
        producto.setNombre(nombre);
        producto.setIdCategoria(idCategoria);
        producto.setPrecioUnitario(precio);
        producto.setStockMinimo(stockMinimo);
        producto.setRutaImagen(rutaImagen);

        boolean exito = productoService.registrar(producto);

        if (!exito) {
            throw new IllegalArgumentException(
                    "No se pudo registrar el producto.");
        }

        request.getSession().setAttribute(
                "mensajeExito",
                "Producto " + producto.getCodigoSku()
                + " registrado correctamente con stock inicial 0.");
    }

    private void editarProducto(HttpServletRequest request) {

        int idProducto = convertirEntero(
                request.getParameter("idProducto"),
                "El identificador del producto no es válido.");

        String sku = request.getParameter("editSku");

        BigDecimal precio = convertirPrecio(
                request.getParameter("editPrecio"));

        int stockMinimo = convertirEntero(
                request.getParameter("editStockMin"),
                "El stock mínimo debe ser un número entero.");

        boolean exito = productoService.actualizar(
                idProducto, sku, precio, stockMinimo);

        if (!exito) {
            throw new IllegalArgumentException(
                    "No se pudo actualizar el producto.");
        }

        request.getSession().setAttribute(
                "mensajeExito",
                "El producto se actualizó correctamente.");
    }

    private int convertirEntero(String valor, String mensaje) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }

        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private BigDecimal convertirPrecio(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El precio del producto es obligatorio.");
        }

        try {
            return new BigDecimal(valor.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Ingrese un precio numérico válido.");
        }
    }
}

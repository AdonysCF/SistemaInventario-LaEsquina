package controller;

import dao.ProductoDAO;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modelo.Producto;

@WebServlet(name = "ProductoServlet", urlPatterns = {"/ProductoServlet"})
public class ProductoServlet extends HttpServlet {

    ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");

        if (accion == null || accion.isEmpty()) {
            accion = "listar";
        }

        switch (accion) {
            case "listar":
                List<Producto> lista = productoDAO.listarTodosActivos();
                request.setAttribute("listaProductos", lista);
                // Redirige a la vista del catálogo
                request.getRequestDispatcher("catalogoProductos.jsp").forward(request, response);
                break;
            // Aquí agregarás "eliminar" si es necesario
            default:
                response.sendRedirect("panelGerente.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");

        if (accion != null && accion.equalsIgnoreCase("agregar")) {
            // 1. Recibir los datos del formulario Modal
            String sku = request.getParameter("txtSku");
            String nombre = request.getParameter("txtNombre");
            int idCategoria = Integer.parseInt(request.getParameter("selCategoria"));
            java.math.BigDecimal precio = new java.math.BigDecimal(request.getParameter("numPrecio"));
            int stockMinimo = Integer.parseInt(request.getParameter("numStockMin"));
            String rutaImagen = request.getParameter("txtImagen");

            // 2. Llenar el objeto Producto
            Producto p = new Producto();
            p.setCodigoSku(sku);
            p.setNombre(nombre);
            p.setIdCategoria(idCategoria);
            p.setPrecioUnitario(precio);
            p.setStockMinimo(stockMinimo);
            p.setRutaImagen(rutaImagen);

            // 3. Guardar en la Base de Datos
            ProductoDAO dao = new ProductoDAO();
            boolean exito = dao.agregarProducto(p);

            // 4. Redirigir de vuelta al catálogo con un mensaje
            if (exito) {
                request.getSession().setAttribute("mensajeExito", "Producto " + sku + " registrado correctamente con stock inicial 0.");
            } else {
                request.getSession().setAttribute("mensajeError", "Error al registrar producto. Verifique que el SKU no esté duplicado.");
            }

            // Redirige al método listar
            response.sendRedirect("ProductoServlet?accion=listar");

        } else if ("editar".equalsIgnoreCase(accion)) {
            try {
                // 1. Recibir y validar los datos del formulario Modal de edición
                String idStr = request.getParameter("idProducto");
                String sku = request.getParameter("editSku");
                String precioStr = request.getParameter("editPrecio");
                String stockMinStr = request.getParameter("editStockMin");

                // Validación básica para evitar NullPointerExceptions
                if (idStr == null || sku == null || precioStr == null || stockMinStr == null) {
                    request.getSession().setAttribute("mensajeError", "Error: Faltan datos en el formulario de edición.");
                    response.sendRedirect("ProductoServlet?accion=listar");
                    return;
                }

                int idProducto = Integer.parseInt(idStr);
                java.math.BigDecimal precio = new java.math.BigDecimal(precioStr);
                int stockMinimo = Integer.parseInt(stockMinStr);

                // 2. Ejecutar la actualización en la Base de Datos (Modelo)
                ProductoDAO dao = new ProductoDAO();
                boolean exito = dao.actualizarProductoParcial(idProducto, sku, precio, stockMinimo);

                // 3. Establecer mensaje de éxito o error
                if (exito) {
                    request.getSession().setAttribute("mensajeExito", "El producto se actualizó correctamente.");
                } else {
                    request.getSession().setAttribute("mensajeError", "Error al actualizar el producto. Verifique que el SKU no esté duplicado.");
                }

            } catch (NumberFormatException e) {
                // Si el usuario ingresó texto en vez de números en precio o stock
                request.getSession().setAttribute("mensajeError", "Error: Formato de número inválido al editar producto.");
                e.printStackTrace();
            } catch (Exception e) {
                // Cualquier otro error imprevisto
                request.getSession().setAttribute("mensajeError", "Error inesperado: " + e.getMessage());
                e.printStackTrace();
            }

            // 4. LA CLAVE DE LA SOLUCIÓN: La redirección incondicional
            // Esto debe ejecutarse SIEMPRE al final de la acción, haya habido error o no.
            response.sendRedirect("ProductoServlet?accion=listar");
        }
    }
}
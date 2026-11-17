
package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import modelo.Categoria;
import service.CategoriaService;

@WebServlet(
        name = "CategoriaServlet",
        urlPatterns = {"/CategoriaServlet"}
)
public class CategoriaServlet extends HttpServlet {

    private final CategoriaService categoriaService =
            new CategoriaService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String accion = request.getParameter("accion");

        if (accion == null || accion.isEmpty()) {
            accion = "listar";
        }

        if ("listar".equalsIgnoreCase(accion)) {

            List<Categoria> categorias =
                    categoriaService.listarTodas();

            request.setAttribute("listaCategorias", categorias);

            request.getRequestDispatcher(
                    "/gestionarCategorias.jsp"
            ).forward(request, response);

        } else {

            response.sendRedirect(
                    request.getContextPath()
                    + "/CategoriaServlet?accion=listar");
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

                agregarCategoria(request);

            } else if ("editar".equalsIgnoreCase(accion)) {

                editarCategoria(request);

            } else if ("cambiarEstado".equalsIgnoreCase(accion)) {

                cambiarEstadoCategoria(request);

            } else {

                throw new IllegalArgumentException(
                        "La operacion solicitada no es valida.");
            }

        } catch (IllegalArgumentException ex) {

            request.getSession().setAttribute(
                    "mensajeErrorCategoria",
                    ex.getMessage()
            );

        } catch (Exception ex) {

            getServletContext().log(
                    "Error en el modulo de categorias.", ex);

            request.getSession().setAttribute(
                    "mensajeErrorCategoria",
                    "Ocurrio un error al procesar la operacion. "
                    + "Intente nuevamente."
            );
        }

        response.sendRedirect(
                request.getContextPath()
                + "/CategoriaServlet?accion=listar"
        );
    }

    // REGISTRAR
    private void agregarCategoria(HttpServletRequest request) {

        String nombre = request.getParameter("nombre");
        String descripcion = request.getParameter("descripcion");

        boolean resultado =
                categoriaService.registrar(nombre, descripcion);

        if (resultado) {

            request.getSession().setAttribute(
                    "mensajeExitoCategoria",
                    "La categoria se registro correctamente."
            );

        } else {

            throw new IllegalArgumentException(
                    "No se pudo registrar la categoria.");
        }
    }

    // EDITAR
    private void editarCategoria(HttpServletRequest request) {

        int idCategoria = obtenerId(request);

        String nombre = request.getParameter("nombre");
        String descripcion = request.getParameter("descripcion");

        boolean resultado = categoriaService.actualizar(
                idCategoria,
                nombre,
                descripcion
        );

        if (resultado) {

            request.getSession().setAttribute(
                    "mensajeExitoCategoria",
                    "La categoria se actualizo correctamente."
            );

        } else {

            throw new IllegalArgumentException(
                    "No se pudo actualizar la categoria.");
        }
    }

    // ACTIVAR / DESACTIVAR
    private void cambiarEstadoCategoria(HttpServletRequest request) {

        int idCategoria = obtenerId(request);

        String estado = request.getParameter("activo");

        if (!"true".equalsIgnoreCase(estado)
                && !"false".equalsIgnoreCase(estado)) {

            throw new IllegalArgumentException(
                    "El estado enviado no es valido.");
        }

        boolean activar = Boolean.parseBoolean(estado);

        boolean resultado = categoriaService.cambiarEstado(
                idCategoria,
                activar
        );

        if (resultado) {

            request.getSession().setAttribute(
                    "mensajeExitoCategoria",
                    activar
                    ? "La categoria se activo correctamente."
                    : "La categoria se desactivo correctamente."
            );

        } else {

            throw new IllegalArgumentException(
                    "No se pudo cambiar el estado de la categoria.");
        }
    }

    // VALIDAR IDENTIFICADOR
    private int obtenerId(HttpServletRequest request) {

        try {

            String id = request.getParameter("idCategoria");

            if (id == null || id.trim().isEmpty()) {
                throw new NumberFormatException();
            }

            return Integer.parseInt(id);

        } catch (NumberFormatException ex) {

            throw new IllegalArgumentException(
                    "El identificador de la categoria no es valido.");
        }
    }
}

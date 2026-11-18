
package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

import modelo.Usuario;
import service.UsuarioService;

@WebServlet(
        name = "UsuarioServlet",
        urlPatterns = {"/UsuarioServlet"}
)
public class UsuarioServlet extends HttpServlet {

    private final UsuarioService usuarioService =
            new UsuarioService();

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

            List<Usuario> usuarios =
                    usuarioService.listarTodos();

            request.setAttribute("listaUsuarios", usuarios);

            request.getRequestDispatcher(
                    "/gestionarUsuarios.jsp"
            ).forward(request, response);

        } else {

            response.sendRedirect(
                    request.getContextPath()
                    + "/UsuarioServlet?accion=listar");
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

                agregarUsuario(request);

            } else if ("cambiarEstado".equalsIgnoreCase(accion)) {

                cambiarEstado(request);

            } else {

                throw new IllegalArgumentException(
                        "La operacion solicitada no es valida.");
            }

        } catch (IllegalArgumentException ex) {

            request.getSession().setAttribute(
                    "mensajeErrorUsuario",
                    ex.getMessage()
            );

        } catch (Exception ex) {

            getServletContext().log(
                    "Error en el modulo de usuarios.", ex);

            request.getSession().setAttribute(
                    "mensajeErrorUsuario",
                    "Ocurrio un error al procesar la operacion. "
                    + "Intente nuevamente."
            );
        }

        response.sendRedirect(
                request.getContextPath()
                + "/UsuarioServlet?accion=listar"
        );
    }

    // REGISTRAR USUARIO
    private void agregarUsuario(HttpServletRequest request) {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String nombre = request.getParameter("nombreCompleto");
        String rol = request.getParameter("rol");

        boolean resultado =
                usuarioService.registrarNuevoUsuario(
                        username,
                        password,
                        nombre,
                        rol
                );

        if (!resultado) {
            throw new IllegalArgumentException(
                    "No se pudo registrar el usuario.");
        }

        request.getSession().setAttribute(
                "mensajeExitoUsuario",
                "La cuenta de usuario se registro correctamente."
        );
    }

    // ACTIVAR / DESACTIVAR
    private void cambiarEstado(HttpServletRequest request) {

        int idUsuario;

        try {

            String id = request.getParameter("idUsuario");

            if (id == null || id.trim().isEmpty()) {
                throw new NumberFormatException();
            }

            idUsuario = Integer.parseInt(id);

        } catch (NumberFormatException ex) {

            throw new IllegalArgumentException(
                    "El identificador del usuario no es valido.");
        }

        String estado = request.getParameter("activo");

        if (!"true".equalsIgnoreCase(estado)
                && !"false".equalsIgnoreCase(estado)) {

            throw new IllegalArgumentException(
                    "El estado enviado no es valido.");
        }

        HttpSession session = request.getSession(false);

        Usuario usuarioSesion = (session != null)
                ? (Usuario) session.getAttribute("usuarioSesion")
                : null;

        if (usuarioSesion == null) {
            throw new IllegalArgumentException(
                    "La sesion ha expirado. Inicie sesion nuevamente.");
        }

        boolean activar = Boolean.parseBoolean(estado);

        boolean resultado = usuarioService.cambiarEstado(
                idUsuario,
                activar,
                usuarioSesion.getIdUsuario()
        );

        if (!resultado) {
            throw new IllegalArgumentException(
                    "No se pudo cambiar el estado de la cuenta.");
        }

        request.getSession().setAttribute(
                "mensajeExitoUsuario",
                activar
                ? "La cuenta se activo correctamente."
                : "La cuenta se desactivo correctamente."
        );
    }
}

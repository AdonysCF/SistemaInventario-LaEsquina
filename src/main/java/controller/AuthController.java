
package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import modelo.Usuario;
import service.UsuarioService;

@WebServlet(name = "AuthController", urlPatterns = {"/login"})
public class AuthController extends HttpServlet {

    private final UsuarioService usuarioService = new UsuarioService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");

        if ("salir".equals(accion)) {

            // Obtener la sesión actual sin crear una nueva.
            HttpSession session = request.getSession(false);

            // Destruir la sesión si existe.
            if (session != null) {
                session.invalidate();
            }

            // Regresar al login.
            response.sendRedirect("index.jsp");

        } else {

            // Si ingresa por GET sin acción, regresar al login.
            response.sendRedirect("index.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String user = request.getParameter("txtUsuario");
        String pass = request.getParameter("txtPassword");

        // Validar las credenciales.
        Usuario usuarioLogueado = usuarioService.validarLogin(user, pass);

        if (usuarioLogueado != null) {

            // Obtener o crear la sesión actual.
            HttpSession session = request.getSession(true);

            // Regenerar el identificador de sesión.
            // Protege contra ataques de fijación de sesión.
            request.changeSessionId();

            // Guardar los datos del usuario autenticado.
            session.setAttribute("usuarioSesion", usuarioLogueado);

            // Redirigir según el rol del usuario.
            if ("GERENTE".equals(usuarioLogueado.getRol())) {

                response.sendRedirect("panelGerente.jsp");

            } else {

                response.sendRedirect("panelCajero.jsp");
            }

        } else {

            // Credenciales incorrectas.
            request.setAttribute(
                    "errorMensaje",
                    "Usuario o contraseña incorrectos"
            );

            request.getRequestDispatcher("index.jsp")
                    .forward(request, response);
        }
    }
}

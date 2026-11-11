
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

            HttpSession session = request.getSession(false);

            if (session != null) {
                session.invalidate();
            }

            response.sendRedirect("index.jsp");

        } else {

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

            HttpSession session = request.getSession(true);

            request.changeSessionId();

            session.setAttribute("usuarioSesion", usuarioLogueado);

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

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
     
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String accion = request.getParameter("accion");

        if ("salir".equals(accion)) {
            // Invalida (destruye) la sesión actual
            request.getSession().invalidate();
            
            // Redirige al login principal
            response.sendRedirect("index.jsp");
        } else {
            // Si entra por GET sin accion, lo manda al login
            response.sendRedirect("index.jsp");
        }
    }

    private UsuarioService usuarioService = new UsuarioService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String user = request.getParameter("txtUsuario");
        String pass = request.getParameter("txtPassword");
        
        Usuario usuarioLogueado = usuarioService.validarLogin(user, pass);
        
        if (usuarioLogueado != null) {
            // Iniciar sesión exitosa
            HttpSession session = request.getSession();
            session.setAttribute("usuarioSesion", usuarioLogueado);
            
            // Redirigir según el rol (según tu diagrama de navegación)
            if (usuarioLogueado.getRol().equals("GERENTE")) {
                response.sendRedirect("panelGerente.jsp");
            } else {
                response.sendRedirect("panelCajero.jsp");
            }
        } else {
            // Login fallido
            request.setAttribute("errorMensaje", "Usuario o contraseña incorrectos");
            request.getRequestDispatcher("index.jsp").forward(request, response);
        }
    }
    
}
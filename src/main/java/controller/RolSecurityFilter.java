package controller; // Asegúrate de que coincida con tu paquete

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import modelo.Usuario;


@WebFilter("/*")
public class RolSecurityFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Método de inicialización del filtro (se ejecuta una vez al arrancar)
        System.out.println(" RolSecurityFilter iniciado.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        String requestURI = req.getRequestURI();
        
        if (requestURI.endsWith("/") || 
            requestURI.endsWith("index.jsp") || 
            requestURI.endsWith("/login") || 
            requestURI.contains("/css/") || 
            requestURI.contains("/js/") || 
            requestURI.contains("/img/")) {
            
            chain.doFilter(request, response); 
            return;
        }


        Usuario usr = (session != null) ? (Usuario) session.getAttribute("usuarioSesion") : null;
        
        if (usr == null) {

            res.sendRedirect(req.getContextPath() + "/index.jsp");
            return;
        }


        String rol = usr.getRol();

        // 3 Restricciones para el CAJERO
        if (rol.equals("CAJERO")) {
            // Rutas PROHIBIDAS para el cajero (Módulos gerenciales, catálogo, ajustes y reportes)
            if (requestURI.contains("panelGerente.jsp") ||
                requestURI.contains("catalogoProductos.jsp") ||
                requestURI.contains("registrarAjuste.jsp") ||
                requestURI.contains("ProductoServlet") ||
                requestURI.contains("AjusteServlet") ||
                requestURI.contains("Reporte")) {
                
                // Devuelve Error HTTP 403 - Acceso Denegado
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso Denegado. Esta función es exclusiva de la Gerencia.");
                return;
            }
        }
        
        // 3.2. Restricciones para el GERENTE
        if (rol.equals("GERENTE")) {
             // El gerente, por definición del informe, tiene acceso total a los reportes y gestión.
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Se ejecuta al apagar el servidor
    }
}
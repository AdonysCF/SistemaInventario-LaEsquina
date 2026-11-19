
package controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

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

    /*
     * Recursos que pueden consultarse sin iniciar sesión.
     */
    private static final Set<String> RUTAS_PUBLICAS = new HashSet<>(
            Arrays.asList(
                    "/",
                    "/index.jsp",
                    "/login"
            )
    );

    /*
     * Rutas que puede utilizar el CAJERO.
     *
     * Incluye los Servlets y los JSP a los que estos
     * redirigen o desde los cuales se muestran las vistas.
     */
    private static final Set<String> RUTAS_CAJERO = new HashSet<>(
            Arrays.asList(
                    "/panelcajero.jsp",

                    // Ventas
                    "/ventaservlet",
                    "/registrarventa.jsp",

                    // Entradas de mercadería
                    "/entradaservlet",
                    "/registrarentrada.jsp",

                    // Consultas operativas
                    "/consultaservlet",
                    "/consultarproductos.jsp"
            )
    );

    /*
     * Rutas exclusivas de GERENCIA.
     *
     * Se mantienen explícitas para documentar los módulos
     * que no debe utilizar el cajero.
     */
    private static final Set<String> RUTAS_GERENCIA = new HashSet<>(
            Arrays.asList(
                    "/panelgerente.jsp",

                    // Productos
                    "/productoservlet",
                    "/catalogoproductos.jsp",

                    // Categorías
                    "/categoriaservlet",
                    "/gestionarcategorias.jsp",

                    // Usuarios
                    "/usuarioservlet",
                    "/gestionarusuarios.jsp",

                    // Ajustes de inventario
                    "/ajusteservlet",
                    "/registrarajuste.jsp",
                    
                    //kardex
                    "/KardexServlet",
                    "/kardex.jsp"
            )
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("RolSecurityFilter iniciado correctamente.");
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String contextPath = req.getContextPath();

        String uri = req.getRequestURI();

        /*
         * Obtener la ruta sin el contexto de la aplicación.
         */
        String ruta = uri.substring(contextPath.length());

        ruta = ruta.toLowerCase(Locale.ROOT);

        /*
         * Evitar que el navegador utilice respuestas
         * almacenadas después de cambiar de usuario o rol.
         */
        res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setDateHeader("Expires", 0);

        /*
         * Permitir recursos estáticos.
         */
        if (esRecursoEstatico(ruta)) {
            chain.doFilter(request, response);
            return;
        }

        /*
         * Permitir las rutas públicas.
         */
        if (RUTAS_PUBLICAS.contains(ruta)) {
            chain.doFilter(request, response);
            return;
        }

        /*
         * Obtener la sesión existente sin crear una nueva.
         */
        HttpSession session = req.getSession(false);

        if (session == null) {
            redirigirLogin(req, res);
            return;
        }

        Object usuarioSesion = session.getAttribute("usuarioSesion");

        if (!(usuarioSesion instanceof Usuario)) {
            session.invalidate();
            redirigirLogin(req, res);
            return;
        }

        Usuario usuario = (Usuario) usuarioSesion;

        String rol = usuario.getRol();

        if (rol == null) {
            res.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "El usuario no tiene un rol asignado."
            );
            return;
        }

        rol = rol.trim().toUpperCase(Locale.ROOT);

        /*
         * AUTORIZACIÓN DEL CAJERO
         */
        if ("CAJERO".equals(rol)) {

            if (!RUTAS_CAJERO.contains(ruta)) {

                res.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Acceso denegado. El cajero no tiene permisos "
                        + "para ingresar a este módulo."
                );

                return;
            }

            chain.doFilter(request, response);
            return;
        }

        /*
         * AUTORIZACIÓN DEL GERENTE
         *
         * El gerente tiene acceso a todos los módulos
         * del sistema.
         */
        if ("GERENTE".equals(rol)) {

            chain.doFilter(request, response);
            return;
        }

        /*
         * Rechazar cualquier rol que no esté reconocido.
         */
        res.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Acceso denegado. El rol del usuario no está autorizado."
        );
    }

    /*
     * Identifica recursos que no requieren autorización
     * por rol, como imágenes, CSS y JavaScript.
     */
    private boolean esRecursoEstatico(String ruta) {

        return ruta.startsWith("/css/")
                || ruta.startsWith("/js/")
                || ruta.startsWith("/img/")
                || ruta.startsWith("/fonts/")
                || ruta.startsWith("/assets/")
                || ruta.equals("/favicon.ico");
    }

    /*
     * Redirige al inicio de sesión.
     */
    private void redirigirLogin(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.sendRedirect(
                request.getContextPath() + "/index.jsp"
        );
    }

    @Override
    public void destroy() {
        System.out.println("RolSecurityFilter finalizado.");
    }
}

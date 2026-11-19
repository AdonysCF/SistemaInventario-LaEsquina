<%@page import="modelo.Usuario"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<%
    // VALIDACIÓN DE SEGURIDAD (Intocable)
    Usuario usr = (Usuario) session.getAttribute("usuarioSesion");
    if (usr == null || !usr.getRol().equals("GERENTE")) {
        response.sendRedirect("index.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Panel del gerente | La Esquina</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
        <!-- Integración de Bootstrap Icons para una interfaz más profesional -->
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
        <link rel="stylesheet" href="<c:url value='/css/operaciones-caja.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/gerencia.css'/>">
</head>
    <body class="gerencia">
<c:set var="tituloGerencia" value="Panel del gerente"/>
<c:set var="pantallaGerencia" value="Inicio"/>
<%@include file="/WEB-INF/encabezadoGerencia.jspf"%>
            <main class="cashier-main" id="contenidoGerencia" tabindex="-1">
                <section aria-labelledby="accesosGerencia">
                    <h2 class="gerencia-section-title" id="accesosGerencia">Accesos rápidos</h2>
                    <div class="gerencia-accesos">
                        <a class="gerencia-acceso gerencia-destacado" href="<c:out value='${urlReposiciones}'/>">
                            <i class="bi bi-box-seam gerencia-acceso-icono" aria-hidden="true"></i>
                            <div>
                                <h3>Productos por reponer</h3>
                                <p>Revisa los productos agotados o con stock bajo.</p>
                                <span class="gerencia-acceso-enlace">Ver reposiciones <i class="bi bi-arrow-right" aria-hidden="true"></i></span>
                            </div>
                        </a>
                        <a class="gerencia-acceso" href="<c:out value='${urlHistorial}'/>">
                            <i class="bi bi-receipt gerencia-acceso-icono" aria-hidden="true"></i>
                            <div>
                                <h3>Historial de ventas</h3>
                                <p>Busca un ticket y revisa los productos vendidos.</p>
                                <span class="gerencia-acceso-enlace">Ver ventas <i class="bi bi-arrow-right" aria-hidden="true"></i></span>
                            </div>
                        </a>
                        <a class="gerencia-acceso" href="<c:out value='${urlKardex}'/>">
                            <i class="bi bi-clock-history gerencia-acceso-icono" aria-hidden="true"></i>
                            <div>
                                <h3>Movimientos de inventario</h3>
                                <p>Consulta entradas, ventas y ajustes de stock.</p>
                                <span class="gerencia-acceso-enlace">Ver movimientos <i class="bi bi-arrow-right" aria-hidden="true"></i></span>
                            </div>
                        </a>
                    </div>
                </section>
            </main>
        </div>
    </div>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    
</body>
</html>

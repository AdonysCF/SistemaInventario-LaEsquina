<%@page import="modelo.Usuario"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%
    // VALIDACIÓN ESTRICTA DE SEGURIDAD (AC-03)
    // Verificamos si hay un usuario en sesión y si su rol es exclusivamente 'GERENTE'
    Usuario usrSesion = (Usuario) session.getAttribute("usuarioSesion");
    
    if (usrSesion == null || !usrSesion.getRol().equals("GERENTE")) {
        // Si es un Cajero o alguien que no se ha logueado, lo pateamos al Login con un error HTTP 403 (Acceso Denegado)
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso Denegado. Esta área es exclusiva para la Gerencia.");
        return; // Detiene la carga de la página
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Gestión de Catálogo - La Esquina</title>
    <!-- Incluimos Bootstrap 5 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

    <!-- Barra de Navegación del Gerente -->
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark mb-4">
        <div class="container-fluid">
            <a class="navbar-brand" href="panelGerente.jsp">La Esquina - Gerencia</a>
            <div class="collapse navbar-collapse">
                <ul class="navbar-nav me-auto">
                    <li class="nav-item">
                        <!-- El enlace apunta al Servlet para que cargue la lista antes de mostrar la página -->
                        <a class="nav-link active" href="ProductoServlet?accion=listar">Catálogo de Productos</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="registrarEntrada.jsp">Registrar Entrada</a>
                    </li>
                </ul>
                <span class="navbar-text me-3">
                    Usuario: <%= usrSesion.getNombreCompleto() %>
                </span>
                <a href="login?accion=salir" class="btn btn-outline-danger btn-sm">Cerrar Sesión</a>
            </div>
        </div>
    </nav>

    <div class="container">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h2>Catálogo Maestro de Productos</h2>
            <!-- Botón para abrir el Modal de Nuevo Producto -->
            <button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-toggle="modal" data-bs-target="#modalNuevoProducto">
                + Nuevo Producto
            </button>
        </div>

        <div class="card shadow-sm">
            <div class="card-body">
                <table class="table table-hover table-bordered align-middle">
                    <thead class="table-dark">
                        <tr>
                            <th>SKU</th>
                            <th>Nombre del Producto</th>
                            <th>Categoría (ID)</th>
                            <th>Precio (S/)</th>
                            <th>Stock Actual</th>
                            <th>Stock Mínimo</th>
                            <th>Estado</th>
                            <th>Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        <!-- Recorremos la lista enviada por el ProductoServlet -->
                        <c:forEach var="prod" items="${listaProductos}">
                            <tr>
                                <td>${prod.codigoSku}</td>
                                <td>${prod.nombre}</td>
                                <td>${prod.idCategoria}</td>
                                <td>${prod.precioUnitario}</td>
                                <td><strong>${prod.stockActual}</strong></td>
                                <td>${prod.stockMinimo}</td>
                                <td>
                                    <!-- Pintamos el estado tipo semáforo según las reglas del negocio -->
                                    <c:choose>
                                        <c:when test="${prod.estadoAlerta == 'DISPONIBLE'}">
                                            <span class="badge bg-success">Disponible</span>
                                        </c:when>
                                        <c:when test="${prod.estadoAlerta == 'PROXIMO_A_AGOTARSE'}">
                                            <span class="badge bg-warning text-dark">Alerta</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-danger">Agotado</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <button class="btn btn-sm btn-outline-secondary">Editar</button>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
                
                <!-- Validamos si la lista está vacía -->
                <c:if test="${empty listaProductos}">
                    <div class="alert alert-info text-center mt-3">
                        No hay productos registrados o el Servlet no envió los datos.
                    </div>
                </c:if>
            </div>
        </div>
    </div>

    <!-- Script de Bootstrap -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
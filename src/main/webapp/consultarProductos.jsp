<%@page import="modelo.Usuario"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%
    Usuario usr = (Usuario) session.getAttribute("usuarioSesion");
    if (usr == null) { response.sendRedirect("index.jsp"); return; }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Consulta Operativa - La Esquina</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

    <nav class="navbar navbar-dark bg-secondary mb-4">
        <div class="container-fluid">
            <a class="navbar-brand" href="${sessionScope.usuarioSesion.rol == 'GERENTE' ? 'panelGerente.jsp' : 'panelCajero.jsp'}">La Esquina - Consultas</a>
            <a href="${sessionScope.usuarioSesion.rol == 'GERENTE' ? 'panelGerente.jsp' : 'panelCajero.jsp'}" class="btn btn-outline-light btn-sm">Volver al Panel</a>
        </div>
    </nav>

    <div class="container">
        <div class="card shadow-sm mb-4">
            <div class="card-body">
                <form action="ConsultaServlet" method="GET" class="d-flex align-items-center">
                    <label class="fw-bold me-3 text-nowrap">Buscar Producto:</label>
                    <input type="text" name="txtBuscar" class="form-control me-2" 
                           placeholder="Ingrese código SKU o parte del nombre..." 
                           value="${busquedaAnterior}">
                    <button type="submit" class="btn btn-primary">Buscar</button>
                    <a href="ConsultaServlet" class="btn btn-outline-secondary ms-2">Limpiar</a>
                </form>
            </div>
        </div>

        <div class="card shadow-sm">
            <div class="card-header bg-dark text-white">
                <h5 class="mb-0">Resultados de Disponibilidad</h5>
            </div>
            <div class="card-body p-0">
                <table class="table table-hover mb-0">
                    <thead class="table-light">
                        <tr>
                            <th>Código SKU</th>
                            <th>Descripción</th>
                            <th>Precio Venta</th>
                            <th class="text-center">Stock Físico</th>
                            <th>Estado Alerta</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty listaResultados}">
                                <tr>
                                    <td colspan="5" class="text-center p-4 text-muted">
                                        No se encontraron productos con el criterio ingresado.
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="p" items="${listaResultados}">
                                    <tr>
                                        <td class="fw-bold">${p.codigoSku}</td>
                                        <td>${p.nombre}</td>
                                        <td class="text-success fw-bold">S/ ${p.precioUnitario}</td>
                                        <td class="text-center fw-bold fs-5">${p.stockActual}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${p.estadoAlerta == 'DISPONIBLE'}">
                                                    <span class="badge bg-success">Disponible</span>
                                                </c:when>
                                                <c:when test="${p.estadoAlerta == 'PROXIMO_A_AGOTARSE'}">
                                                    <span class="badge bg-warning text-dark">Poco Stock</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-danger">Agotado</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</body>
</html>
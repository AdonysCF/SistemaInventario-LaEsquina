<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<%@taglib uri="jakarta.tags.fmt" prefix="fmt"%>
<%
    if (request.getDispatcherType() == jakarta.servlet.DispatcherType.REQUEST) {
        response.sendRedirect(request.getContextPath() + "/ReporteReposicionServlet");
        return;
    }
%>
<fmt:setLocale value="es_PE" scope="page"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Planificación de reposiciones | La Esquina</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <link rel="stylesheet" href="<c:url value='/css/operaciones-caja.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/gerencia.css'/>">
</head>
<body class="gerencia">
<c:set var="tituloGerencia" value="Planificación de reposiciones"/>
<c:set var="pantallaGerencia" value="Reposiciones"/>
<%@include file="/WEB-INF/encabezadoGerencia.jspf"%>
    <main class="cashier-main" id="contenidoGerencia" tabindex="-1">

        <c:choose>
            <c:when test="${not empty errorReporte}">
                <p class="alert alert-danger" role="alert"><c:out value="${errorReporte}"/></p>
            </c:when>
            <c:when test="${empty filas}">
                <p class="alert alert-light border">No hay productos activos agotados ni en su nivel mínimo de stock o por debajo de él.</p>
            </c:when>
            <c:otherwise>
                <p class="text-muted mb-3">
                    El déficit indica cuántas unidades faltan para alcanzar el stock mínimo.
                    La rotación muestra las unidades de ventas confirmadas en los últimos
                    <c:out value="${diasRotacion}"/> días, incluido hoy.
                </p>
                <div class="table-responsive bg-white rounded shadow-sm">
                    <table class="table table-hover align-middle mb-0">
                        <caption class="visually-hidden">Productos priorizados para reposición</caption>
                        <thead class="table-light">
                            <tr>
                                <th scope="col">SKU</th>
                                <th scope="col">Producto</th>
                                <th scope="col">Categoría</th>
                                <th scope="col">Estado</th>
                                <th scope="col" class="text-end">Stock actual</th>
                                <th scope="col" class="text-end">Stock mínimo</th>
                                <th scope="col" class="text-end">Déficit (unid.)</th>
                                <th scope="col" class="text-end">Vendidas en <c:out value="${diasRotacion}"/> días</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="fila" items="${filas}">
                                <tr>
                                    <td class="text-nowrap"><c:out value="${fila.sku}"/></td>
                                    <th scope="row"><c:out value="${fila.producto}"/></th>
                                    <td><c:out value="${fila.categoria}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${fila.agotado}">
                                                <span class="badge text-bg-danger">Agotado</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge text-bg-warning">Poco stock</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-end"><fmt:formatNumber value="${fila.stockActual}"/></td>
                                    <td class="text-end"><fmt:formatNumber value="${fila.stockMinimo}"/></td>
                                    <td class="text-end fw-bold"><fmt:formatNumber value="${fila.deficit}"/></td>
                                    <td class="text-end"><fmt:formatNumber value="${fila.unidadesVendidas}"/></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </main>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

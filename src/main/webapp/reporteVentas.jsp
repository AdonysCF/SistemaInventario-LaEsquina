<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<%@taglib uri="jakarta.tags.fmt" prefix="fmt"%>
<%
    if (request.getDispatcherType() == jakarta.servlet.DispatcherType.REQUEST) {
        response.sendRedirect(request.getContextPath() + "/ReporteVentasServlet");
        return;
    }
%>
<c:url var="urlPanel" value="/panelGerente.jsp"/>
<c:url var="urlReporte" value="/ReporteVentasServlet"/>
<c:url var="urlLogo" value="/img/logo.png"/>
<fmt:setLocale value="es_PE" scope="page"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Ventas por período | La Esquina</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background-color: #f4f6f9; }
        .bg-navy { background-color: #10253f; }
        .text-navy { color: #10253f; }
        .btn-navy { background-color: #10253f; color: #fff; }
        .btn-navy:hover, .btn-navy:focus { background-color: #234367; color: #fff; }
    </style>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <link rel="stylesheet" href="<c:url value='/css/operaciones-caja.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/gerencia.css'/>">
</head>
<body class="gerencia">
<c:set var="tituloGerencia" value="Ventas por período"/>
<c:set var="pantallaGerencia" value="ReporteVentas"/>
<%@include file="/WEB-INF/encabezadoGerencia.jspf"%>
            <main class="cashier-main" id="contenidoGerencia" tabindex="-1">


        <form action="${urlReporte}" method="get" class="card card-body border-0 shadow-sm mb-4">
            <div class="row g-3 align-items-end">
                <div class="col-6 col-md-3">
                    <label for="desde" class="form-label">Desde</label>
                    <input id="desde" name="desde" type="date" class="form-control"
                           min="1753-01-01" max="9999-12-31" required value="<c:out value='${desde}'/>">
                </div>
                <div class="col-6 col-md-3">
                    <label for="hasta" class="form-label">Hasta</label>
                    <input id="hasta" name="hasta" type="date" class="form-control"
                           min="1753-01-01" max="9999-12-31" required value="<c:out value='${hasta}'/>">
                </div>
                <div class="col-12 col-md-auto d-flex gap-2 flex-wrap">
                    <button type="submit" class="btn btn-navy">Generar reporte</button>
                    <a href="${urlReporte}" class="btn btn-outline-secondary">Limpiar</a>
                </div>
            </div>
        </form>

        <c:if test="${not empty errorReporte}">
            <div class="alert alert-danger" role="alert"><c:out value="${errorReporte}"/></div>
        </c:if>
        <c:if test="${not empty resumen}">
            <section class="mb-4" aria-labelledby="resumenTitulo">
                <h2 class="h4 text-navy" id="resumenTitulo">Resumen del período</h2>
                <dl class="row g-3 mb-0">
                    <div class="col-md-4">
                        <div class="card card-body border-0 shadow-sm h-100">
                            <dt>Unidades vendidas</dt>
                            <dd class="fs-4 text-navy mb-0"><fmt:formatNumber value="${resumen.unidades}"/></dd>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="card card-body border-0 shadow-sm h-100">
                            <dt>Tickets distintos</dt>
                            <dd class="fs-4 text-navy mb-0"><fmt:formatNumber value="${resumen.tickets}"/></dd>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="card card-body border-0 shadow-sm h-100">
                            <dt>Monto vendido</dt>
                            <dd class="fs-4 text-navy mb-0">S/ <fmt:formatNumber value="${resumen.monto}" minFractionDigits="2" maxFractionDigits="2"/></dd>
                        </div>
                    </div>
                </dl>
            </section>

            <c:choose>
                <c:when test="${empty porProducto}">
                    <p class="alert alert-light border">No se encontraron ventas confirmadas en el período.</p>
                </c:when>
                <c:otherwise>
                    <p class="text-muted small">Un ticket puede aparecer en varias filas. El total del período cuenta cada ticket una sola vez.</p>
                <section class="mb-4" aria-labelledby="porProductoTitulo">
                    <h2 class="h4 text-navy" id="porProductoTitulo">Ventas por producto</h2>
                    <div class="table-responsive bg-white rounded shadow-sm">
                        <table class="table table-hover align-middle mb-0">
                            <thead class="table-light">
                                <tr>
                                <th scope="col">SKU</th>
                                <th scope="col">Producto</th>
                                <th scope="col">Categoría</th>
                                <th scope="col" class="text-end">Unidades vendidas</th>
                                <th scope="col" class="text-end">Tickets distintos</th>
                                <th scope="col" class="text-end">Monto vendido</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="fila" items="${porProducto}">
                                    <tr>
                                    <td><c:out value="${fila.sku}"/></td>
                                    <td><c:out value="${fila.producto}"/></td>
                                    <td><c:out value="${fila.categoria}"/></td>
                                    <td class="text-end text-nowrap"><fmt:formatNumber value="${fila.unidades}"/></td>
                                    <td class="text-end text-nowrap"><fmt:formatNumber value="${fila.tickets}"/></td>
                                    <td class="text-end text-nowrap">S/ <fmt:formatNumber value="${fila.monto}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </section>
                <section class="mb-4" aria-labelledby="porCategoriaTitulo">
                    <h2 class="h4 text-navy" id="porCategoriaTitulo">Ventas por categoría</h2>
                    <div class="table-responsive bg-white rounded shadow-sm">
                        <table class="table table-hover align-middle mb-0">
                            <thead class="table-light">
                                <tr>
                                <th scope="col">Categoría</th>
                                <th scope="col" class="text-end">Unidades vendidas</th>
                                <th scope="col" class="text-end">Tickets distintos</th>
                                <th scope="col" class="text-end">Monto vendido</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="fila" items="${porCategoria}">
                                    <tr>
                                    <td><c:out value="${fila.categoria}"/></td>
                                    <td class="text-end text-nowrap"><fmt:formatNumber value="${fila.unidades}"/></td>
                                    <td class="text-end text-nowrap"><fmt:formatNumber value="${fila.tickets}"/></td>
                                    <td class="text-end text-nowrap">S/ <fmt:formatNumber value="${fila.monto}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </section>
                </c:otherwise>
            </c:choose>
        </c:if>
            </main>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

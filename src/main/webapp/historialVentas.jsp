<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<%@taglib uri="jakarta.tags.fmt" prefix="fmt"%>
<%
    if (request.getDispatcherType() == jakarta.servlet.DispatcherType.REQUEST) {
        response.sendRedirect(request.getContextPath() + "/HistorialVentaServlet");
        return;
    }
%>
<c:url var="urlPanel" value="/panelGerente.jsp"/>
<c:url var="urlHistorial" value="/HistorialVentaServlet"/>
<c:url var="urlLogo" value="/img/logo.png"/>
<fmt:setLocale value="es_PE" scope="page"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Historial de ventas | La Esquina</title>
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
<c:set var="tituloGerencia" value="Historial de ventas"/>
<c:set var="pantallaGerencia" value="Historial"/>
<%@include file="/WEB-INF/encabezadoGerencia.jspf"%>
            <main class="cashier-main" id="contenidoGerencia" tabindex="-1">


        <form action="${urlHistorial}" method="get" class="card card-body border-0 shadow-sm mb-4">
            <div class="row g-3 align-items-end">
                <div class="col-6 col-md-3">
                    <label for="desde" class="form-label">Desde</label>
                    <input id="desde" name="desde" type="date" class="form-control"
                           min="1753-01-01" max="9999-12-31" value="<c:out value='${desde}'/>">
                </div>
                <div class="col-6 col-md-3">
                    <label for="hasta" class="form-label">Hasta</label>
                    <input id="hasta" name="hasta" type="date" class="form-control"
                           min="1753-01-01" max="9999-12-31" value="<c:out value='${hasta}'/>">
                </div>
                <div class="col-12 col-md-3">
                    <label for="ticket" class="form-label">Número de ticket</label>
                    <input id="ticket" name="ticket" type="text" maxlength="25" class="form-control"
                           value="<c:out value='${ticket}'/>">
                </div>
                <div class="col-12 col-md-3">
                    <label for="responsable" class="form-label">Usuario responsable</label>
                    <input id="responsable" name="responsable" type="text" maxlength="50" class="form-control"
                           value="<c:out value='${responsable}'/>">
                </div>
                <div class="col-12 d-flex gap-2">
                    <button type="submit" class="btn btn-navy">Filtrar</button>
                    <a href="${urlHistorial}" class="btn btn-outline-secondary">Limpiar</a>
                </div>
            </div>
        </form>

        <c:choose>
            <c:when test="${not empty errorHistorial}">
                <div class="alert alert-danger" role="alert"><c:out value="${errorHistorial}"/></div>
            </c:when>
            <c:otherwise>
                <c:choose>
                    <c:when test="${empty ventas}">
                        <p class="alert alert-light border">No se encontraron ventas con esos filtros.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="table-responsive bg-white rounded shadow-sm">
                            <table class="table table-hover align-middle mb-0">
                                <caption class="visually-hidden">Tickets de venta registrados</caption>
                                <thead class="table-light">
                                    <tr>
                                        <th scope="col">Ticket</th>
                                        <th scope="col">Fecha y hora</th>
                                        <th scope="col">Responsable</th>
                                        <th scope="col">Estado</th>
                                        <th scope="col" class="text-end">Total</th>
                                        <th scope="col" class="text-end">Detalle</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="venta" items="${ventas}">
                                        <c:url var="urlDetalle" value="/HistorialVentaServlet">
                                            <c:param name="desde" value="${desde}"/>
                                            <c:param name="hasta" value="${hasta}"/>
                                            <c:param name="ticket" value="${ticket}"/>
                                            <c:param name="responsable" value="${responsable}"/>
                                            <c:param name="pagina" value="${pagina}"/>
                                            <c:param name="idVenta" value="${venta.idVenta}"/>
                                        </c:url>
                                        <tr>
                                            <th scope="row"><c:out value="${venta.numeroTicket}"/></th>
                                            <td class="text-nowrap"><c:out value="${venta.fechaHora}"/></td>
                                            <td>
                                                <c:out value="${venta.responsable}"/>
                                                <small class="d-block text-muted"><c:out value="${venta.usuario}"/></small>
                                            </td>
                                            <td><c:out value="${venta.estado}"/></td>
                                            <td class="text-end text-nowrap">S/ <fmt:formatNumber value="${venta.total}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                            <td class="text-end">
                                                <a class="btn btn-sm btn-outline-primary text-nowrap"
                                                   href="<c:out value='${urlDetalle}'/>">Ver detalle</a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>

                <c:if test="${pagina gt 1 or haySiguiente}">
                    <nav class="d-flex flex-wrap align-items-center gap-3 mt-3" aria-label="Páginas del historial">
                        <c:if test="${pagina gt 1}">
                            <c:url var="urlAnterior" value="/HistorialVentaServlet">
                                <c:param name="desde" value="${desde}"/>
                                <c:param name="hasta" value="${hasta}"/>
                                <c:param name="ticket" value="${ticket}"/>
                                <c:param name="responsable" value="${responsable}"/>
                                <c:param name="pagina" value="${pagina - 1}"/>
                            </c:url>
                            <a class="btn btn-outline-secondary" href="<c:out value='${urlAnterior}'/>">Anterior</a>
                        </c:if>
                        <span>Página <c:out value="${pagina}"/></span>
                        <c:if test="${haySiguiente}">
                            <c:url var="urlSiguiente" value="/HistorialVentaServlet">
                                <c:param name="desde" value="${desde}"/>
                                <c:param name="hasta" value="${hasta}"/>
                                <c:param name="ticket" value="${ticket}"/>
                                <c:param name="responsable" value="${responsable}"/>
                                <c:param name="pagina" value="${pagina + 1}"/>
                            </c:url>
                            <a class="btn btn-outline-secondary" href="<c:out value='${urlSiguiente}'/>">Siguiente</a>
                        </c:if>
                    </nav>
                </c:if>
            </c:otherwise>
        </c:choose>
            </main>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

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
    <title>Detalle de la venta | La Esquina</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background-color: #f4f6f9; }
        .bg-navy { background-color: #10253f; }
        .text-navy { color: #10253f; }
        .btn-navy { background-color: #10253f; color: #fff; }
        .btn-navy:hover, .btn-navy:focus { background-color: #234367; color: #fff; }
    </style>
</head>
<body>
    <nav class="navbar bg-navy py-2">
        <div class="container-fluid px-4">
            <a class="navbar-brand" href="${urlPanel}">
                <img src="${urlLogo}" height="40" alt="La Esquina">
            </a>
            <a class="btn btn-outline-light btn-sm" href="${urlPanel}">Volver al panel</a>
        </div>
    </nav>

    <c:url var="urlVolver" value="/HistorialVentaServlet">
        <c:param name="desde" value="${desde}"/>
        <c:param name="hasta" value="${hasta}"/>
        <c:param name="ticket" value="${ticket}"/>
        <c:param name="responsable" value="${responsable}"/>
        <c:param name="pagina" value="${pagina}"/>
    </c:url>
    <main class="container py-4">
        <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
            <h1 class="h2 text-navy mb-0">Detalle de la venta</h1>
            <a class="btn btn-outline-secondary" href="<c:out value='${urlVolver}'/>">Volver al historial</a>
        </div>
        <div class="card card-body border-0 shadow-sm mb-4">
            <dl class="row g-3 mb-0">
                <div class="col-6 col-md-3">
                    <dt>Ticket</dt><dd class="mb-0"><c:out value="${venta.numeroTicket}"/></dd>
                </div>
                <div class="col-6 col-md-3">
                    <dt>Fecha y hora</dt><dd class="mb-0"><c:out value="${venta.fechaHora}"/></dd>
                </div>
                <div class="col-6 col-md-3">
                    <dt>Responsable</dt>
                    <dd class="mb-0"><c:out value="${venta.responsable}"/> (<c:out value="${venta.usuario}"/>)</dd>
                </div>
                <div class="col-6 col-md-3">
                    <dt>Estado</dt><dd class="mb-0"><c:out value="${venta.estado}"/></dd>
                </div>
            </dl>
        </div>

        <div class="table-responsive bg-white rounded shadow-sm">
            <table class="table align-middle mb-0">
                <caption class="visually-hidden">Productos del ticket</caption>
                <thead class="table-light">
                    <tr>
                        <th scope="col">SKU</th>
                        <th scope="col">Producto</th>
                        <th scope="col" class="text-end">Cantidad</th>
                        <th scope="col" class="text-end">Precio unitario</th>
                        <th scope="col" class="text-end">Subtotal</th>
                    </tr>
                </thead>
                <tbody>
                    <c:if test="${empty productos}">
                        <tr><td colspan="5" class="text-center py-3">No hay productos registrados para este ticket.</td></tr>
                    </c:if>
                    <c:forEach var="producto" items="${productos}">
                        <tr>
                            <td><c:out value="${producto.sku}"/></td>
                            <td><c:out value="${producto.producto}"/></td>
                            <td class="text-end"><c:out value="${producto.cantidad}"/></td>
                            <td class="text-end text-nowrap">S/ <fmt:formatNumber value="${producto.precioUnitario}" minFractionDigits="2" maxFractionDigits="2"/></td>
                            <td class="text-end text-nowrap">S/ <fmt:formatNumber value="${producto.subtotal}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        </tr>
                    </c:forEach>
                </tbody>
                <tfoot class="table-light">
                    <tr>
                        <th colspan="4" scope="row" class="text-end">Total registrado</th>
                        <td class="text-end fw-bold text-nowrap">S/ <fmt:formatNumber value="${venta.total}" minFractionDigits="2" maxFractionDigits="2"/></td>
                    </tr>
                </tfoot>
            </table>
        </div>
    </main>
</body>
</html>

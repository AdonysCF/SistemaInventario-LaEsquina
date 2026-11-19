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
<c:url var="urlAnular" value="/AnulacionVentaServlet"/>
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
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <link rel="stylesheet" href="<c:url value='/css/operaciones-caja.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/gerencia.css'/>">
</head>
<body class="gerencia">
<c:set var="tituloGerencia" value="Detalle de la venta"/>
<c:set var="pantallaGerencia" value="Historial"/>
<%@include file="/WEB-INF/encabezadoGerencia.jspf"%>
            <main class="cashier-main" id="contenidoGerencia" tabindex="-1">


    <c:url var="urlVolver" value="/HistorialVentaServlet">
        <c:param name="desde" value="${desde}"/>
        <c:param name="hasta" value="${hasta}"/>
        <c:param name="ticket" value="${ticket}"/>
        <c:param name="responsable" value="${responsable}"/>
        <c:param name="pagina" value="${pagina}"/>
    </c:url>

        <div class="d-flex justify-content-end mb-4">


            <a class="btn btn-outline-secondary" href="<c:out value='${urlVolver}'/>">Volver al historial</a>
        </div>
        <c:if test="${not empty mensajeAnulacion}">
            <div class="alert ${anulacionExitosa ? 'alert-success' : 'alert-danger'}" role="alert">
                <c:out value="${mensajeAnulacion}"/>
            </div>
        </c:if>
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
        <c:if test="${not empty tokenAnulacion}">
            <details class="card card-body border-0 shadow-sm mt-4">
                <summary class="fw-bold text-danger">Anular venta</summary>
                <form action="${urlAnular}" method="post" accept-charset="UTF-8" class="mt-3">
                    <p>Al confirmar, las cantidades de este ticket volverán al inventario.</p>
                    <input type="hidden" name="idVenta" value="<c:out value='${venta.idVenta}'/>">
                    <input type="hidden" name="tokenAnulacion" value="<c:out value='${tokenAnulacion}'/>">
                    <input type="hidden" name="desde" value="<c:out value='${desde}'/>">
                    <input type="hidden" name="hasta" value="<c:out value='${hasta}'/>">
                    <input type="hidden" name="ticket" value="<c:out value='${ticket}'/>">
                    <input type="hidden" name="responsable" value="<c:out value='${responsable}'/>">
                    <input type="hidden" name="pagina" value="<c:out value='${pagina}'/>">
                    <label for="motivo" class="form-label fw-bold">Motivo de la anulación</label>
                    <textarea id="motivo" name="motivo" class="form-control mb-3"
                              rows="3" maxlength="250" required></textarea>
                    <button type="submit" class="btn btn-danger">Confirmar anulación</button>
                </form>
            </details>
        </c:if>
            </main>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

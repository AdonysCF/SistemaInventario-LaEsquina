<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<c:url var="urlKardex" value="/KardexServlet"/>
<c:url var="urlPanel" value="/panelGerente.jsp"/>
<c:url var="urlLogo" value="/img/logo.png"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Historial de movimientos (Kardex) | La Esquina</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background-color: #f4f6f9; }
        .bg-navy { background-color: #10253f; }
        .text-navy { color: #10253f; }
        .btn-kardex { background-color: #10253f; color: #fff; }
        .btn-kardex:hover, .btn-kardex:focus { background-color: #234367; color: #fff; }
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

    <main class="container-fluid px-3 px-md-4 py-4">
        <h1 class="h2 text-navy mb-4">Historial de movimientos (Kardex)</h1>

        <form action="${urlKardex}" method="get" class="card card-body border-0 shadow-sm mb-4">
            <div class="row g-3 align-items-end">
                <div class="col-6 col-md-3 col-xl-2">
                    <label for="desde" class="form-label">Desde</label>
                    <input type="date" id="desde" name="desde" class="form-control"
                           min="1753-01-01" max="9999-12-31" value="<c:out value='${desde}'/>">
                </div>
                <div class="col-6 col-md-3 col-xl-2">
                    <label for="hasta" class="form-label">Hasta</label>
                    <input type="date" id="hasta" name="hasta" class="form-control"
                           min="1753-01-01" max="9999-12-31" value="<c:out value='${hasta}'/>">
                </div>
                <div class="col-6 col-md-3 col-xl-2">
                    <label for="sku" class="form-label">SKU del producto</label>
                    <input type="text" id="sku" name="sku" class="form-control" maxlength="30"
                           value="<c:out value='${sku}'/>">
                </div>
                <div class="col-6 col-md-3 col-xl-2">
                    <label for="tipo" class="form-label">Tipo de movimiento</label>
                    <select id="tipo" name="tipo" class="form-select">
                        <option value="">Todos</option>
                        <option value="ENTRADA_REPOSICION" ${tipo eq 'ENTRADA_REPOSICION' ? 'selected' : ''}>Entrada</option>
                        <option value="SALIDA_VENTA" ${tipo eq 'SALIDA_VENTA' ? 'selected' : ''}>Venta</option>
                        <option value="AJUSTE" ${tipo eq 'AJUSTE' ? 'selected' : ''}>Ajuste</option>
                        <option value="ANULACION" ${tipo eq 'ANULACION' ? 'selected' : ''}>Anulación</option>
                    </select>
                </div>
                <div class="col-12 col-md-6 col-xl-2">
                    <label for="responsable" class="form-label">Usuario responsable</label>
                    <input type="text" id="responsable" name="responsable" class="form-control"
                           maxlength="50" value="<c:out value='${responsable}'/>">
                </div>
                <div class="col-12 col-xl d-flex gap-2">
                    <button class="btn btn-kardex" type="submit">Filtrar</button>
                    <a class="btn btn-outline-secondary" href="${urlKardex}">Limpiar</a>
                </div>
            </div>
        </form>

        <c:choose>
            <c:when test="${not empty errorKardex}">
                <div class="alert alert-danger" role="alert"><c:out value="${errorKardex}"/></div>
            </c:when>
            <c:otherwise>
                <c:choose>
                    <c:when test="${empty filas}">
                        <p class="alert alert-light border">No se encontraron movimientos con esos filtros.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="table-responsive bg-white rounded shadow-sm">
                            <table class="table table-hover align-middle mb-0">
                                <caption class="visually-hidden">Detalle de movimientos por producto</caption>
                                <thead class="table-light">
                                    <tr>
                                        <th scope="col">Fecha y hora</th>
                                        <th scope="col">Movimiento</th>
                                        <th scope="col">Producto</th>
                                        <th scope="col" class="text-end">Variación (unid.)</th>
                                        <th scope="col" class="text-end">Stock anterior</th>
                                        <th scope="col" class="text-end">Stock resultante</th>
                                        <th scope="col">Responsable</th>
                                        <th scope="col">Documento de referencia</th>
                                        <th scope="col">Motivo / Observación</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="fila" items="${filas}">
                                        <tr>
                                            <td class="text-nowrap"><c:out value="${fila.fechaHora}"/></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${fila.tipoMovimiento eq 'ENTRADA_REPOSICION'}">Entrada</c:when>
                                                    <c:when test="${fila.tipoMovimiento eq 'SALIDA_VENTA'}">Venta</c:when>
                                                    <c:when test="${fila.tipoMovimiento eq 'AJUSTE_POSITIVO'}">Ajuste positivo</c:when>
                                                    <c:when test="${fila.tipoMovimiento eq 'AJUSTE_NEGATIVO'}">Ajuste negativo</c:when>
                                                    <c:when test="${fila.tipoMovimiento eq 'ANULACION'}">Anulación</c:when>
                                                    <c:otherwise><c:out value="${fila.tipoMovimiento}"/></c:otherwise>
                                                </c:choose>
                                                <small class="d-block text-muted"><c:out value="${fila.codigoMovimiento}"/></small>
                                            </td>
                                            <td>
                                                <c:out value="${fila.producto}"/>
                                                <small class="d-block text-muted"><c:out value="${fila.sku}"/></small>
                                            </td>
                                            <td class="text-end text-nowrap fw-bold">
                                                <c:out value="${fila.variacionStock gt 0 ? '+' : ''}"/><c:out value="${fila.variacionStock}"/>
                                            </td>
                                            <td class="text-end"><c:out value="${fila.stockAnterior}"/></td>
                                            <td class="text-end"><c:out value="${fila.stockResultante}"/></td>
                                            <td>
                                                <c:out value="${fila.responsable}"/>
                                                <small class="d-block text-muted"><c:out value="${fila.usuario}"/></small>
                                            </td>
                                            <td><c:out value="${fila.documentoReferencia}"/></td>
                                            <td><c:out value="${empty fila.observacion ? '—' : fila.observacion}"/></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>

                <c:if test="${pagina gt 1 or haySiguiente}">
                    <nav class="d-flex align-items-center gap-3 mt-3" aria-label="Páginas del Kardex">
                        <c:if test="${pagina gt 1}">
                            <c:url var="anterior" value="/KardexServlet">
                                <c:param name="desde" value="${desde}"/>
                                <c:param name="hasta" value="${hasta}"/>
                                <c:param name="sku" value="${sku}"/>
                                <c:param name="tipo" value="${tipo}"/>
                                <c:param name="responsable" value="${responsable}"/>
                                <c:param name="pagina" value="${pagina - 1}"/>
                            </c:url>
                            <a class="btn btn-outline-secondary" href="<c:out value='${anterior}'/>">Anterior</a>
                        </c:if>
                        <span>Página <c:out value="${pagina}"/></span>
                        <c:if test="${haySiguiente}">
                            <c:url var="siguiente" value="/KardexServlet">
                                <c:param name="desde" value="${desde}"/>
                                <c:param name="hasta" value="${hasta}"/>
                                <c:param name="sku" value="${sku}"/>
                                <c:param name="tipo" value="${tipo}"/>
                                <c:param name="responsable" value="${responsable}"/>
                                <c:param name="pagina" value="${pagina + 1}"/>
                            </c:url>
                            <a class="btn btn-outline-secondary" href="<c:out value='${siguiente}'/>">Siguiente</a>
                        </c:if>
                    </nav>
                </c:if>
            </c:otherwise>
        </c:choose>
    </main>
</body>
</html>

<%@page import="modelo.Usuario"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%
    // Seguridad: Acceso permitido para AMBOS roles (Cajero y Gerente)
    Usuario usr = (Usuario) session.getAttribute("usuarioSesion");
    if (usr == null) { response.sendRedirect("index.jsp"); return; }
    if (request.getDispatcherType() == jakarta.servlet.DispatcherType.REQUEST) {
        response.sendRedirect(request.getContextPath() + "/ConsultaServlet");
        return;
    }
%>
<c:set var="esGerente" value="${sessionScope.usuarioSesion.rol eq 'GERENTE'}"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Consultar existencias | La Esquina</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <link rel="stylesheet" href="<c:url value='/css/operaciones-caja.css'/>">
    <c:if test="${esGerente}">
        <link rel="stylesheet" href="<c:url value='/css/gerencia.css'/>">
    </c:if>
</head>
<body class="${esGerente ? 'gerencia' : ''}">
<c:set var="tituloOperacion" value="Consultar existencias"/>
<c:set var="operacionActiva" value="consulta"/>
<c:choose>
    <c:when test="${esGerente}">
        <c:set var="tituloGerencia" value="${tituloOperacion}"/>
        <c:set var="pantallaGerencia" value="Consulta"/>
        <%@include file="/WEB-INF/encabezadoGerencia.jspf"%>
    </c:when>
    <c:otherwise>
        <%@include file="/WEB-INF/encabezadoOperativo.jspf"%>
    </c:otherwise>
</c:choose>
            <main class="cashier-main"
                  id="${esGerente ? 'contenidoGerencia' : 'contenidoCajero'}" tabindex="-1">
                <div class="operation-intro">
                    <p>Busca por nombre o código SKU y filtra por estado de stock.</p>
                </div>

                <!-- Tarjeta del Buscador -->
                <div class="card card-custom p-4 mb-4">
                    <form action="<c:url value='/ConsultaServlet'/>" method="get"
                          class="row g-3 align-items-end">
                        <div class="col-12 col-md">
                            <label for="txtBuscar" class="form-label fw-bold">Nombre o código SKU</label>
                            <input type="text" id="txtBuscar" name="txtBuscar" class="form-control"
                                   value="<c:out value='${busquedaAnterior}'/>" autocomplete="off">
                        </div>
                        <div class="col-12 col-md-4">
                            <label for="estado" class="form-label fw-bold">Estado de stock</label>
                            <select id="estado" name="estado" class="form-select">
                                <option value="">Todos</option>
                                <option value="DISPONIBLE" ${estadoSeleccionado eq 'DISPONIBLE' ? 'selected' : ''}>Disponibles</option>
                                <option value="PROXIMO_A_AGOTARSE" ${estadoSeleccionado eq 'PROXIMO_A_AGOTARSE' ? 'selected' : ''}>Próximos a agotarse</option>
                                <option value="AGOTADO" ${estadoSeleccionado eq 'AGOTADO' ? 'selected' : ''}>Agotados</option>
                            </select>
                        </div>
                        <div class="col-12 col-xl-auto d-flex flex-wrap gap-2">
                            <button type="submit" class="btn btn-operativo operation-button fw-bold">Consultar</button>
                            <a href="<c:url value='/ConsultaServlet'/>"
                               class="btn btn-outline-secondary fw-bold">Limpiar</a>
                        </div>
                    </form>
                </div>

                <c:choose>
                    <c:when test="${not empty errorConsulta}">
                        <div class="alert alert-danger" role="alert"><c:out value="${errorConsulta}"/></div>
                    </c:when>
                    <c:otherwise>
                        <!-- Tarjeta de Resultados -->
                        <div class="card card-custom">
                            <div class="card-body p-0">
                                <div class="table-responsive">
                                    <table class="table table-hover align-middle mb-0">
                                        <thead class="table-light">
                                            <tr>
                                                <th class="ps-4 text-center">Imagen</th>
                                                <th>Código SKU</th>
                                                <th>Artículo</th>
                                                <th class="text-end">Precio de Venta</th>
                                                <th class="text-center">Stock Físico (UND)</th>
                                                <th class="text-center">Stock Mínimo (UND)</th>
                                                <th class="text-center pe-4">Disponibilidad</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:choose>
                                                <c:when test="${empty listaResultados}">
                                                    <tr>
                                                        <td colspan="7" class="text-center p-5">
                                                            <i class="bi bi-binoculars fs-1 text-muted d-block mb-3"></i>
                                                            <h5 class="text-navy fw-bold">No se encontraron resultados</h5>
                                                            <p class="text-muted mb-0">Pruebe con otro estado o término de búsqueda.</p>
                                                        </td>
                                                    </tr>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:forEach var="p" items="${listaResultados}">
                                                        <tr>
                                                            <td class="ps-4 text-center">
                                                                <!-- Imagen del Producto -->
                                                                <img src="<c:out value="${not empty p.rutaImagen ? p.rutaImagen : 'img/productos/default.png'}"/>" 
                                                                     alt="<c:out value='${p.nombre}'/>" class="img-thumbnail-custom">
                                                            </td>
                                                            <td class="text-muted fw-bold"><c:out value="${p.codigoSku}"/></td>
                                                            <td class="fw-bold text-navy fs-5"><c:out value="${p.nombre}"/></td>
                                                            <td class="text-end text-success fw-bold fs-4">S/ ${p.precioUnitario}</td>
                                                            <td class="text-center fs-4 fw-bold">
                                                                <c:choose>
                                                                    <c:when test="${p.stockActual == 0}">
                                                                        <span class="text-danger">${p.stockActual}</span>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        ${p.stockActual}
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </td>
                                                            <td class="text-center"><c:out value="${p.stockMinimo}"/></td>
                                                            <td class="text-center pe-4">
                                                                <c:choose>
                                                                    <c:when test="${p.estadoAlerta == 'DISPONIBLE'}">
                                                                        <span class="badge bg-success bg-opacity-10 text-success border border-success rounded-pill px-3 py-2"><i class="bi bi-check-circle me-1"></i> Disponible</span>
                                                                    </c:when>
                                                                    <c:when test="${p.estadoAlerta == 'PROXIMO_A_AGOTARSE'}">
                                                                        <span class="badge bg-warning bg-opacity-10 text-dark border border-warning rounded-pill px-3 py-2"><i class="bi bi-exclamation-triangle me-1"></i> Próximo a agotarse</span>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <span class="badge bg-danger bg-opacity-10 text-danger border border-danger rounded-pill px-3 py-2"><i class="bi bi-x-circle me-1"></i> Agotado</span>
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
                    </c:otherwise>
                </c:choose>
            </main>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

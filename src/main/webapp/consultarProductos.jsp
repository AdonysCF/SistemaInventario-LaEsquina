<%@page import="modelo.Usuario"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%
    // Seguridad: Acceso permitido para AMBOS roles (Cajero y Gerente)
    Usuario usr = (Usuario) session.getAttribute("usuarioSesion");
    if (usr == null) { response.sendRedirect("index.jsp"); return; }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Consulta Operativa - La Esquina</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <style>
        :root {
            --color-navy: #10253F;
            --color-mustard: #EAA81B;
        }
        body { background-color: #f4f6f9; }
        .bg-navy { background-color: var(--color-navy) !important; }
        .text-navy { color: var(--color-navy) !important; }
        .card-custom {
            border: none;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.05);
        }
        .table-hover tbody tr:hover { background-color: rgba(234, 168, 27, 0.05); }
        .search-icon { position: absolute; left: 15px; top: 50%; transform: translateY(-50%); color: #6c757d; }
        .search-input { padding-left: 45px; border-radius: 20px; }
        .img-thumbnail-custom {
            width: 50px;
            height: 50px;
            object-fit: contain;
            border-radius: 6px;
            background-color: white;
            border: 1px solid #dee2e6;
        }
    </style>
</head>
<body>

    <!-- NAVBAR -->
    <nav class="navbar navbar-expand-lg bg-navy shadow-sm py-2">
        <div class="container-fluid px-4">
            <a class="navbar-brand" href="${sessionScope.usuarioSesion.rol == 'GERENTE' ? 'panelGerente.jsp' : 'panelCajero.jsp'}">
                <img src="img/logo.png" alt="Logo La Esquina" height="40">
            </a>
            <div class="d-flex align-items-center text-white ms-auto">
                <i class="bi bi-person-badge fs-5 me-2" style="color: var(--color-mustard)"></i>
                <span class="me-3 fw-bold"><%= usr.getNombreCompleto() %></span>
                <a href="${sessionScope.usuarioSesion.rol == 'GERENTE' ? 'panelGerente.jsp' : 'panelCajero.jsp'}" class="btn btn-outline-light btn-sm fw-bold">
                    <i class="bi bi-arrow-left me-1"></i> Volver al Panel
                </a>
            </div>
        </div>
    </nav>

    <div class="container py-5">
        
        <div class="text-center mb-5">
            <h2 class="text-navy fw-bold mb-2"><i class="bi bi-search me-2"></i>Consulta de Disponibilidad</h2>
            <p class="text-muted">Revise rápidamente el precio y el stock físico para atender al cliente.</p>
        </div>

        <!-- Tarjeta del Buscador -->
        <div class="card card-custom p-4 mb-4">
            <form action="ConsultaServlet" method="GET" class="d-flex position-relative">
                <i class="bi bi-search fs-5 search-icon"></i>
                <input type="text" name="txtBuscar" class="form-control form-control-lg search-input me-3 shadow-sm border-0 bg-light" 
                       placeholder="Escriba el nombre del artículo o código SKU..." 
                       value="${busquedaAnterior}" autocomplete="off">
                <button type="submit" class="btn btn-navy px-4 fw-bold shadow-sm" style="background-color: var(--color-navy); color: white; border-radius: 20px;">
                    Buscar
                </button>
                <a href="ConsultaServlet" class="btn btn-outline-secondary ms-2 px-4 fw-bold shadow-sm" style="border-radius: 20px;">
                    Limpiar
                </a>
            </form>
        </div>

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
                                <th class="text-center pe-4">Disponibilidad</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty listaResultados}">
                                    <tr>
                                        <td colspan="6" class="text-center p-5">
                                            <i class="bi bi-binoculars fs-1 text-muted d-block mb-3"></i>
                                            <h5 class="text-navy fw-bold">No se encontraron resultados</h5>
                                            <p class="text-muted mb-0">Intente buscar con otra palabra clave o verifique que el artículo esté activo.</p>
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="p" items="${listaResultados}">
                                        <tr>
                                            <td class="ps-4 text-center">
                                                <!-- Imagen del Producto -->
                                                <img src="${not empty p.rutaImagen ? p.rutaImagen : 'img/productos/default.png'}" 
                                                     alt="${p.nombre}" class="img-thumbnail-custom">
                                            </td>
                                            <td class="text-muted fw-bold">${p.codigoSku}</td>
                                            <td class="fw-bold text-navy fs-5">${p.nombre}</td>
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
                                            <td class="text-center pe-4">
                                                <c:choose>
                                                    <c:when test="${p.estadoAlerta == 'DISPONIBLE'}">
                                                        <span class="badge bg-success bg-opacity-10 text-success border border-success rounded-pill px-3 py-2"><i class="bi bi-check-circle me-1"></i> Disponible</span>
                                                    </c:when>
                                                    <c:when test="${p.estadoAlerta == 'PROXIMO_A_AGOTARSE'}">
                                                        <span class="badge bg-warning bg-opacity-10 text-dark border border-warning rounded-pill px-3 py-2"><i class="bi bi-exclamation-triangle me-1"></i> Poco Stock</span>
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
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
<%@page import="modelo.Usuario"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%
    // Seguridad Crítica (AC-03): Solo el Gerente puede administrar el catálogo maestro
    Usuario usr = (Usuario) session.getAttribute("usuarioSesion");
    if (usr == null || !usr.getRol().equals("GERENTE")) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso Denegado. Módulo exclusivo de Gerencia.");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Catálogo de Productos - La Esquina</title>
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
        .btn-mustard {
            background-color: var(--color-mustard);
            color: #000;
            border: none;
            transition: all 0.3s ease;
        }
        .btn-mustard:hover { background-color: #d19315; color: #000; }
        .card-custom {
            border: none;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.05);
        }
        .table-hover tbody tr:hover { background-color: rgba(234, 168, 27, 0.05); }
    </style>
</head>
<body>

    <!-- NAVBAR -->
    <nav class="navbar navbar-expand-lg bg-navy shadow-sm py-2">
        <div class="container-fluid px-4">
            <a class="navbar-brand" href="panelGerente.jsp">
                <img src="img/logo.png" alt="Logo La Esquina" height="40">
            </a>
            <div class="d-flex align-items-center text-white ms-auto">
                <i class="bi bi-person-circle fs-5 me-2" style="color: var(--color-mustard)"></i>
                <span class="me-3 fw-bold">Gerencia: <%= usr.getNombreCompleto() %></span>
                <a href="panelGerente.jsp" class="btn btn-outline-light btn-sm fw-bold">
                    <i class="bi bi-arrow-left me-1"></i> Volver al Panel
                </a>
            </div>
        </div>
    </nav>

    <div class="container-fluid px-4 py-4">
        
        <!-- Encabezado de Módulo -->
        <div class="d-flex justify-content-between align-items-end mb-4">
            <div class="d-flex align-items-center">
                <i class="bi bi-boxes fs-2 text-primary me-3"></i>
                <div>
                    <h3 class="text-navy fw-bold mb-0">Catálogo Maestro de Productos</h3>
                    <p class="text-muted mb-0">Administración de precios, códigos SKU y niveles de stock.</p>
                </div>
            </div>
            <!-- Botón para futura implementación de agregar producto -->
            <button class="btn btn-mustard fw-bold px-4 shadow-sm" onclick="alert('Función de agregar producto en desarrollo.')">
                <i class="bi bi-plus-lg me-2"></i>Nuevo Artículo
            </button>
        </div>

        <c:if test="${not empty mensajeExito}">
            <div class="alert alert-success shadow-sm"><i class="bi bi-check-circle me-2"></i>${mensajeExito}</div>
        </c:if>

        <div class="card card-custom">
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light">
                            <tr>
                                <th class="ps-4">Código SKU</th>
                                <th>Nombre del Artículo</th>
                                <th>Precio Venta</th>
                                <th class="text-center">Stock Físico</th>
                                <th class="text-center">Stock Mínimo</th>
                                <th class="text-center">Estado (Alerta)</th>
                                <th class="text-center pe-4">Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty listaProductos}">
                                    <tr>
                                        <td colspan="7" class="text-center p-5 text-muted">
                                            <i class="bi bi-inbox fs-1 d-block mb-2"></i>
                                            El catálogo de productos está vacío.
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="p" items="${listaProductos}">
                                        <tr>
                                            <td class="ps-4 fw-bold text-muted">${p.codigoSku}</td>
                                            <td class="fw-bold text-navy">${p.nombre}</td>
                                            <td class="text-success fw-bold">S/ ${p.precioUnitario}</td>
                                            <td class="text-center fs-5 fw-bold">${p.stockActual}</td>
                                            <td class="text-center text-muted">${p.stockMinimo}</td>
                                            <td class="text-center">
                                                <c:choose>
                                                    <c:when test="${p.estadoAlerta == 'DISPONIBLE'}">
                                                        <span class="badge bg-success rounded-pill px-3">Disponible</span>
                                                    </c:when>
                                                    <c:when test="${p.estadoAlerta == 'PROXIMO_A_AGOTARSE'}">
                                                        <span class="badge bg-warning text-dark rounded-pill px-3">Poco Stock</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-danger rounded-pill px-3">Agotado</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-center pe-4">
                                                <button class="btn btn-sm btn-outline-primary" title="Editar" onclick="alert('Edición en desarrollo.')">
                                                    <i class="bi bi-pencil-square"></i>
                                                </button>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="card-footer bg-white text-muted small py-3">
                Total de artículos en catálogo: ${listaProductos.size()}
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
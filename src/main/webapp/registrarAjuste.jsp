<%@page import="modelo.Usuario"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%
    Usuario usr = (Usuario) session.getAttribute("usuarioSesion");
    if (usr == null || !usr.getRol().equals("GERENTE")) { 
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso Denegado."); 
        return; 
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Ajuste de Inventario - La Esquina</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
    
    <nav class="navbar navbar-dark bg-dark mb-4">
        <div class="container-fluid">
            <a class="navbar-brand" href="panelGerente.jsp">La Esquina - Ajustes de Stock</a>
            <a href="panelGerente.jsp" class="btn btn-outline-light btn-sm">Volver al Panel</a>
        </div>
    </nav>

    <div class="container">
        <div class="card shadow-sm">
            <div class="card-header bg-warning text-dark">
                <h5 class="mb-0">Registrar Ajuste Manual de Inventario</h5>
            </div>
            <div class="card-body">
                <c:if test="${not empty mensajeExito}">
                    <div class="alert alert-success">${mensajeExito}</div>
                </c:if>
                <c:if test="${not empty mensajeError}">
                    <div class="alert alert-danger">${mensajeError}</div>
                </c:if>

                <form action="AjusteServlet" method="POST">
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Tipo de Ajuste:</label>
                            <select name="cboTipoAjuste" class="form-select" required>
                                <option value="AJUSTE_POSITIVO">Ajuste Positivo (+ Sumar stock)</option>
                                <option value="AJUSTE_NEGATIVO">Ajuste Negativo (- Restar stock por merma)</option>
                            </select>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Motivo (Obligatorio):</label>
                            <input type="text" name="txtMotivo" class="form-control" placeholder="Ej. Merma, conteo físico, producto dañado..." required>
                        </div>
                    </div>

                    <div class="row mb-4">
                        <div class="col-md-8">
                            <label class="form-label fw-bold">Producto a Ajustar:</label>
                            <select name="cboProducto" class="form-select" required>
                                <c:forEach var="p" items="${listaProductos}">
                                    <option value="${p.idProducto}">${p.codigoSku} - ${p.nombre} (Stock actual: ${p.stockActual})</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label fw-bold">Cantidad a Ajustar:</label>
                            <input type="number" name="txtCantidad" class="form-control" min="1" required>
                        </div>
                    </div>

                    <button type="submit" class="btn btn-warning w-100 fw-bold">Confirmar Ajuste y Modificar Kardex</button>
                </form>
            </div>
        </div>
    </div>
</body>
</html>
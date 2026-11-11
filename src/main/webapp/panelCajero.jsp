<%@page import="modelo.Usuario"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    Usuario usr = (Usuario) session.getAttribute("usuarioSesion");
    if (usr == null || !usr.getRol().equals("CAJERO")) {
        response.sendRedirect("index.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Panel de Caja - La Esquina</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

    <nav class="navbar navbar-expand-lg navbar-dark bg-secondary">
        <div class="container-fluid">
            <a class="navbar-brand" href="panelCajero.jsp">La Esquina - Caja</a>
            <div class="collapse navbar-collapse">
                <ul class="navbar-nav me-auto">
                    <li class="nav-item">
                        <a class="nav-link" href="VentaServlet">Registrar Venta</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="EntradaServlet">Registrar Entrada</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="ConsultaServlet">Consultar Stock</a>
                    </li>
                </ul>
                <span class="navbar-text me-3 text-white">
                    Cajero: <strong><%= usr.getNombreCompleto() %></strong>
                </span>
                <a href="login?accion=salir" class="btn btn-outline-light btn-sm">Cerrar Sesión</a>
            </div>
        </div>
    </nav>

    <div class="container mt-4">
        <div class="p-4 mb-4 bg-white rounded shadow-sm">
            <h2>Bienvenida, <%= usr.getNombreCompleto() %></h2>
            <p class="text-muted">Turno de atención en mostrador.</p>
        </div>

        <div class="row g-4">
            <div class="col-md-4">
                <div class="card h-100 shadow-sm border-0">
                    <div class="card-body text-center p-4">
                        <h4 class="card-title text-success">🛒 Caja</h4>
                        <a href="VentaServlet" class="btn btn-success btn-lg w-100 mt-3">Ir a Vender</a>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card h-100 shadow-sm border-0">
                    <div class="card-body text-center p-4">
                        <h4 class="card-title text-primary">📥 Recepción</h4>
                        <a href="EntradaServlet" class="btn btn-primary btn-lg w-100 mt-3">Ingresar Stock</a>
                    </div>
                </div>
            </div>
             <div class="col-md-4">
                <div class="card h-100 shadow-sm border-0">
                    <div class="card-body text-center p-4">
                        <h4 class="card-title text-info">🔍 Consultas</h4>
                        <a href="ConsultaServlet" class="btn btn-outline-info btn-lg w-100 mt-3">Ver Catálogo</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
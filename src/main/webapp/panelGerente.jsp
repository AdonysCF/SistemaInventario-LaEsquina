<%@page import="modelo.Usuario"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    Usuario usr = (Usuario) session.getAttribute("usuarioSesion");
    if (usr == null || !usr.getRol().equals("GERENTE")) {
        response.sendRedirect("index.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Panel de Gerencia - La Esquina</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container-fluid">
            <a class="navbar-brand" href="panelGerente.jsp">La Esquina - Gerencia</a>
            <div class="collapse navbar-collapse">
                <ul class="navbar-nav me-auto">
                    <li class="nav-item">
                        <a class="nav-link active" href="panelGerente.jsp">Inicio</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="ProductoServlet?accion=listar">Catálogo</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="EntradaServlet">Entradas</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="ConsultaServlet">Consultas</a>
                    </li>
                </ul>
                <span class="navbar-text me-3">
                    Conectado: <strong><%= usr.getNombreCompleto() %></strong> (Rol: <%= usr.getRol() %>)
                </span>
                <!-- AQUI ESTA LA CORRECCION PRINCIPAL -->
                <a href="login?accion=salir" class="btn btn-outline-danger btn-sm">Cerrar Sesión</a>
            </div>
        </div>
    </nav>

    <div class="container mt-4">
        <div class="p-4 mb-4 bg-white rounded shadow-sm">
            <h2>Bienvenido, <%= usr.getNombreCompleto() %></h2>
            <p class="text-muted">Panel de administración y control.</p>
        </div>

        <div class="row g-4">
            <div class="col-md-4">
                <div class="card h-100 shadow-sm border-0">
                    <div class="card-body">
                        <h5 class="card-title text-primary">📦 Catálogo Maestro</h5>
                        <a href="ProductoServlet?accion=listar" class="btn btn-primary w-100 mt-2">Gestionar Productos</a>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card h-100 shadow-sm border-0">
                    <div class="card-body">
                        <h5 class="card-title text-warning">⚖️ Ajustes de Stock</h5>
                        <p class="card-text text-muted small">Registrar mermas o descuadres.</p>
                        <a href="AjusteServlet" class="btn btn-warning w-100 mt-2">Realizar Ajuste</a>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card h-100 shadow-sm border-0">
                    <div class="card-body">
                        <h5 class="card-title text-info">🔍 Consultas Operativas</h5>
                        <a href="ConsultaServlet" class="btn btn-outline-info w-100 mt-2">Buscar Artículos</a>
                    </div>
                </div>
            </div>
             <div class="col-md-4">
                <div class="card h-100 shadow-sm border-0">
                    <div class="card-body">
                        <h5 class="card-title text-success">📥 Entradas</h5>
                        <a href="EntradaServlet" class="btn btn-success w-100 mt-2">Ingresar Mercadería</a>
                    </div>
                </div>
            </div>
             <div class="col-md-4">
                <div class="card h-100 shadow-sm border-0">
                    <div class="card-body">
                        <h5 class="card-title text-secondary">🛒 Ventas (Caja)</h5>
                        <a href="VentaServlet" class="btn btn-secondary w-100 mt-2">Ir a Punto de Venta</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
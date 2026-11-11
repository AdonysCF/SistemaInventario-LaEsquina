<%@page import="modelo.Usuario"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    // VALIDACIÓN DE SEGURIDAD (Intocable)
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
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel Operativo (Caja) - La Esquina</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Integración de Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    
    <style>
        :root {
            --color-navy: #10253F;
            --color-mustard: #EAA81B;
        }
        body {
            background-color: #f4f6f9;
            font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
        }
        
        /* Utilidades de color personalizadas */
        .bg-navy { background-color: var(--color-navy) !important; }
        .text-navy { color: var(--color-navy) !important; }
        .text-mustard { color: var(--color-mustard) !important; }
        
        /* Botones personalizados */
        .btn-mustard {
            background-color: var(--color-mustard);
            color: #000;
            border: none;
            transition: all 0.3s ease;
        }
        .btn-mustard:hover {
            background-color: #d19315;
            color: #000;
        }
        .btn-outline-mustard {
            color: var(--color-mustard);
            border-color: var(--color-mustard);
            transition: all 0.3s ease;
        }
        .btn-outline-mustard:hover {
            background-color: var(--color-mustard);
            color: #000;
        }

        /* Navbar estilizado */
        .navbar-brand img {
            max-height: 40px;
        }
        .nav-link {
            font-weight: 500;
            color: rgba(255,255,255,0.8) !important;
        }
        .nav-link:hover, .nav-link.active {
            color: var(--color-mustard) !important;
        }

        /* Tarjetas (Cards) del Dashboard */
        .dashboard-card {
            border: none;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.04);
            transition: transform 0.3s ease, box-shadow 0.3s ease;
            height: 100%;
        }
        .dashboard-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 12px 24px rgba(0,0,0,0.1);
        }
        .card-icon {
            font-size: 2.8rem;
            margin-bottom: 10px;
        }
    </style>
</head>
<body>

    <!-- BARRA DE NAVEGACIÓN SUPERIOR (NAVBAR) -->
    <nav class="navbar navbar-expand-lg bg-navy shadow-sm py-2">
        <div class="container-fluid px-4">
            <a class="navbar-brand d-flex align-items-center" href="panelCajero.jsp">
                <img src="img/logo.png" alt="Logo La Esquina">
            </a>
            
            <button class="navbar-toggler border-0" type="button" data-bs-toggle="collapse" data-bs-target="#navbarCajero">
                <span class="navbar-toggler-icon" style="filter: invert(1);"></span>
            </button>
            
            <div class="collapse navbar-collapse" id="navbarCajero">
                <ul class="navbar-nav me-auto mb-2 mb-lg-0 ms-3">
                    <li class="nav-item">
                        <a class="nav-link active" href="panelCajero.jsp"><i class="bi bi-house-door me-1"></i> Inicio</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="VentaServlet"><i class="bi bi-cart-check me-1"></i> Ventas</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="EntradaServlet"><i class="bi bi-box-arrow-in-down me-1"></i> Entradas</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="ConsultaServlet"><i class="bi bi-search me-1"></i> Consultas</a>
                    </li>
                </ul>
                
                <!-- Info del Usuario y Cierre de Sesión -->
                <div class="d-flex align-items-center text-white">
                    <i class="bi bi-person-badge fs-4 text-mustard me-2"></i>
                    <div class="me-4 lh-1 text-end">
                        <span class="d-block fw-bold"><%= usr.getNombreCompleto() %></span>
                        <small class="text-mustard" style="font-size: 0.75rem;">Atención en Caja</small>
                    </div>
                    <a href="login?accion=salir" class="btn btn-outline-mustard btn-sm fw-bold px-3">
                        <i class="bi bi-box-arrow-right me-1"></i> Salir
                    </a>
                </div>
            </div>
        </div>
    </nav>

    <!-- CONTENIDO PRINCIPAL (DASHBOARD OPERATIVO) -->
    <div class="container mt-5 mb-5">
        
        <!-- Saludo -->
        <div class="mb-4">
            <h3 class="text-navy fw-bold">Turno Operativo de Caja</h3>
            <p class="text-muted">Seleccione una operación para atender en mostrador.</p>
        </div>

        <!-- Cuadrícula de Tarjetas de Acción -->
        <div class="row g-4 justify-content-center">
            
            <!-- Tarjeta Principal: Punto de Venta -->
            <div class="col-md-6 col-lg-5">
                <div class="card dashboard-card text-center p-4" style="border-top: 4px solid #0d6efd;">
                    <div class="card-body d-flex flex-column align-items-center">
                        <i class="bi bi-cart4 card-icon text-primary"></i>
                        <h4 class="fw-bold text-navy">Punto de Venta</h4>
                        <p class="text-muted mb-4">Módulo principal para registrar la venta de artículos a clientes y descontar stock.</p>
                        <a href="VentaServlet" class="btn btn-primary btn-lg w-100 mt-auto fw-bold shadow-sm">
                            Iniciar Venta
                        </a>
                    </div>
                </div>
            </div>

            <!-- Tarjeta: Recepción de Mercadería -->
            <div class="col-md-6 col-lg-4">
                <div class="card dashboard-card text-center p-4" style="border-top: 4px solid #198754;">
                    <div class="card-body d-flex flex-column align-items-center">
                        <i class="bi bi-box-seam card-icon text-success"></i>
                        <h4 class="fw-bold text-navy">Recepción</h4>
                        <p class="text-muted small mb-4">Ingresar reposiciones de proveedores al inventario del local.</p>
                        <a href="EntradaServlet" class="btn btn-success w-100 mt-auto fw-bold">
                            Ingresar Stock
                        </a>
                    </div>
                </div>
            </div>

            <!-- Tarjeta: Consultas Rápidas -->
            <div class="col-md-12 col-lg-3">
                <div class="card dashboard-card text-center p-4" style="border-top: 4px solid var(--color-navy);">
                    <div class="card-body d-flex flex-column align-items-center">
                        <i class="bi bi-search card-icon text-navy"></i>
                        <h5 class="fw-bold text-navy">Consulta de Precios</h5>
                        <p class="text-muted small mb-4">Buscar precio y disponibilidad por nombre o SKU.</p>
                        <a href="ConsultaServlet" class="btn btn-outline-secondary w-100 mt-auto text-navy border-secondary fw-bold">
                            Ver Catálogo
                        </a>
                    </div>
                </div>
            </div>

        </div>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
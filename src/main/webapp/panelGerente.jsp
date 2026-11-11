<%@page import="modelo.Usuario"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    // VALIDACIÓN DE SEGURIDAD (Intocable)
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
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel de Gerencia - La Esquina</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Integración de Bootstrap Icons para una interfaz más profesional -->
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
            <a class="navbar-brand d-flex align-items-center" href="panelGerente.jsp">
                <img src="img/logo.png" alt="Logo La Esquina">
            </a>
            
            <button class="navbar-toggler border-0" type="button" data-bs-toggle="collapse" data-bs-target="#navbarGerente">
                <span class="navbar-toggler-icon" style="filter: invert(1);"></span>
            </button>
            
            <div class="collapse navbar-collapse" id="navbarGerente">
                <ul class="navbar-nav me-auto mb-2 mb-lg-0 ms-3">
                    <li class="nav-item">
                        <a class="nav-link active" href="panelGerente.jsp"><i class="bi bi-house-door me-1"></i> Inicio</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="ProductoServlet?accion=listar"><i class="bi bi-box-seam me-1"></i> Catálogo</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="ConsultaServlet"><i class="bi bi-search me-1"></i> Consultas</a>
                    </li>
                </ul>
                
                <!-- Info del Usuario y Cierre de Sesión -->
                <div class="d-flex align-items-center text-white">
                    <i class="bi bi-person-circle fs-4 text-mustard me-2"></i>
                    <div class="me-4 lh-1 text-end">
                        <span class="d-block fw-bold"><%= usr.getNombreCompleto() %></span>
                        <small class="text-mustard" style="font-size: 0.75rem;">Administrador / Gerente</small>
                    </div>
                    <a href="login?accion=salir" class="btn btn-outline-mustard btn-sm fw-bold px-3">
                        <i class="bi bi-box-arrow-right me-1"></i> Salir
                    </a>
                </div>
            </div>
        </div>
    </nav>

    <!-- CONTENIDO PRINCIPAL (DASHBOARD) -->
    <div class="container mt-5 mb-5">
        
        <!-- Saludo -->
        <div class="mb-4">
            <h3 class="text-navy fw-bold">Panel de Control Gerencial</h3>
            <p class="text-muted">Resumen de operaciones y accesos rápidos del sistema.</p>
        </div>

        <!-- Cuadrícula de Tarjetas de Acción -->
        <div class="row g-4">
            
            <!-- Tarjeta: Catálogo -->
            <div class="col-md-6 col-lg-4">
                <div class="card dashboard-card text-center p-4">
                    <div class="card-body d-flex flex-column align-items-center">
                        <i class="bi bi-boxes card-icon text-navy"></i>
                        <h5 class="fw-bold text-navy">Catálogo Maestro</h5>
                        <p class="text-muted small mb-4">Gestiona productos, precios, categorías y niveles mínimos de stock.</p>
                        <a href="ProductoServlet?accion=listar" class="btn btn-navy w-100 mt-auto" style="background-color: var(--color-navy); color: white;">
                            Gestionar Productos
                        </a>
                    </div>
                </div>
            </div>

            <!-- Tarjeta: Consultas -->
            <div class="col-md-6 col-lg-4">
                <div class="card dashboard-card text-center p-4">
                    <div class="card-body d-flex flex-column align-items-center">
                        <i class="bi bi-search card-icon text-navy"></i>
                        <h5 class="fw-bold text-navy">Consultas Operativas</h5>
                        <p class="text-muted small mb-4">Busca disponibilidad y precios de artículos en tiempo real.</p>
                        <a href="ConsultaServlet" class="btn btn-outline-secondary w-100 mt-auto text-navy border-secondary">
                            Buscar Artículos
                        </a>
                    </div>
                </div>
            </div>

            <!-- Tarjeta: Ajustes de Stock -->
            <div class="col-md-6 col-lg-4">
                <div class="card dashboard-card text-center p-4">
                    <div class="card-body d-flex flex-column align-items-center">
                        <i class="bi bi-sliders card-icon text-mustard"></i>
                        <h5 class="fw-bold text-navy">Ajustes de Stock</h5>
                        <p class="text-muted small mb-4">Registra mermas, deterioros o descuadres del conteo físico.</p>
                        <a href="AjusteServlet" class="btn btn-mustard w-100 mt-auto fw-bold">
                            Realizar Ajuste
                        </a>
                    </div>
                </div>
            </div>

            <!-- Tarjeta: Entradas -->
            <div class="col-md-6 col-lg-6">
                <div class="card dashboard-card p-4">
                    <div class="card-body d-flex align-items-center justify-content-between">
                        <div>
                            <h5 class="fw-bold text-navy mb-1">Recepción de Mercadería</h5>
                            <p class="text-muted small mb-0">Ingresar reposiciones y compras con guía.</p>
                        </div>
                        <a href="EntradaServlet" class="btn btn-success px-4 py-2 fw-bold shadow-sm">
                            <i class="bi bi-box-arrow-in-down me-1"></i> Registrar Entrada
                        </a>
                    </div>
                </div>
            </div>

            <!-- Tarjeta: Ventas -->
            <div class="col-md-6 col-lg-6">
                <div class="card dashboard-card p-4">
                    <div class="card-body d-flex align-items-center justify-content-between">
                        <div>
                            <h5 class="fw-bold text-navy mb-1">Punto de Venta (Caja)</h5>
                            <p class="text-muted small mb-0">Acceder al módulo de venta a clientes.</p>
                        </div>
                        <a href="VentaServlet" class="btn text-white px-4 py-2 fw-bold shadow-sm" style="background-color: #0d6efd;">
                            <i class="bi bi-cart-check me-1"></i> Ir a Caja
                        </a>
                    </div>
                </div>
            </div>

        </div>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
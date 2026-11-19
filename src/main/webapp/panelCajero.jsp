<%@page import="modelo.Usuario"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<%
    Usuario usr = (Usuario) session.getAttribute("usuarioSesion");
    if (usr == null || !"CAJERO".equals(usr.getRol())) {
        response.sendRedirect(request.getContextPath() + "/index.jsp");
        return;
    }
%>
<c:url var="urlInicio" value="/panelCajero.jsp"/>
<c:url var="urlVenta" value="/VentaServlet"/>
<c:url var="urlConsulta" value="/ConsultaServlet"/>
<c:url var="urlEntrada" value="/EntradaServlet"/>
<c:url var="urlSalir" value="/login">
    <c:param name="accion" value="salir"/>
</c:url>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Panel del cajero | La Esquina</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        :root {
            --navy: #10243a;
            --navy-active: #243a50;
            --gold: #efab22;
            --gold-hover: #ffbf41;
            --canvas: #f3f5f8;
            --line: #dce2ea;
            --muted: #48566a;
            --sidebar: 245px;
        }

        body {
            margin: 0;
            background: var(--canvas);
            color: var(--navy);
            font-family: system-ui, -apple-system, "Segoe UI", sans-serif;
        }

        a:focus-visible, button:focus-visible {
            outline: 3px solid #1962ad;
            outline-offset: 4px;
        }

        .cashier-sidebar {
            --bs-offcanvas-width: min(86vw, 300px);
            overflow-y: auto;
            color: #fff;
        }

        .sidebar-surface {
            min-height: 100%;
            padding: 1.35rem 1rem;
            background: var(--navy);
        }

        .sidebar-surface :focus-visible,
        .sale-card :focus-visible {
            outline-color: var(--gold);
        }

        .brand {
            display: flex;
            align-items: center;
            gap: .7rem;
            min-height: 44px;
            color: #fff;
            font-size: 1.3rem;
            font-weight: 750;
            text-decoration: none;
        }

        .brand i { color: var(--gold); font-size: 1.55rem; }
        .cashier-nav { margin-top: 1.85rem; }
        .nav-group { margin-top: 1.5rem; }

        .nav-heading {
            margin: 0 0 .4rem;
            padding-left: .85rem;
            color: #c0ccda;
            font-size: .75rem;
            font-weight: 700;
            letter-spacing: .075em;
            text-transform: uppercase;
        }

        .menu-link {
            display: flex;
            align-items: center;
            gap: .7rem;
            min-height: 46px;
            padding: .55rem .65rem .55rem .7rem;
            border-inline-start: 3px solid transparent;
            border-radius: 7px;
            color: #f5f8fc;
            font-weight: 600;
            text-decoration: none;
        }

        .menu-link i { flex: 0 0 1.15rem; font-size: 1.15rem; }
        .menu-link:hover, .menu-link[aria-current="page"] {
            background: var(--navy-active);
            color: #fff;
        }

        .menu-link[aria-current="page"] { border-inline-start-color: var(--gold); }

        .topbar {
            display: flex;
            align-items: center;
            justify-content: space-between;
            flex-wrap: wrap;
            gap: .65rem 1rem;
            min-height: 76px;
            padding: .85rem clamp(1rem, 3vw, 2.25rem);
            border-bottom: 1px solid var(--line);
            background: #fff;
        }

        .title-group, .session-actions, .user-info {
            display: flex;
            align-items: center;
            gap: .7rem;
            min-width: 0;
        }

        .topbar h1 { margin: 0; font-size: clamp(1.15rem, 2vw, 1.5rem); font-weight: 750; }
        .session-actions { margin-inline-start: auto; }
        .user-info { line-height: 1.25; }
        .user-info > i { font-size: 1.4rem; }
        .user-info strong { display: block; overflow-wrap: anywhere; }
        .user-info small { display: block; color: var(--muted); }
        .topbar .btn { min-height: 44px; font-weight: 650; white-space: nowrap; }

        .cashier-main {
            max-width: 1320px;
            margin: 0 auto;
            padding: clamp(1rem, 3vw, 2.25rem);
        }

        .cashier-main h2 {
            margin: 0 0 1.15rem;
            font-size: clamp(1.35rem, 2.6vw, 1.85rem);
            font-weight: 750;
        }

        .sale-card {
            position: relative;
            isolation: isolate;
            overflow: hidden;
            display: flex;
            align-items: center;
            gap: clamp(1rem, 3.3vw, 2.5rem);
            min-height: 235px;
            padding: clamp(1.2rem, 4vw, 2.7rem);
            border-radius: 15px;
            background: var(--navy);
            color: #fff;
        }

        .sale-icon {
            flex: 0 0 auto;
            padding-inline-end: clamp(.75rem, 3vw, 2rem);
            border-inline-end: 1px solid #62748a;
            color: var(--gold);
            font-size: clamp(2.5rem, 5vw, 4.5rem);
        }

        .sale-decoration {
            position: absolute;
            z-index: -1;
            right: -1rem;
            bottom: -4rem;
            color: #fff;
            opacity: .08;
            font-size: 17rem;
            pointer-events: none;
        }

        .sale-card h3 {
            margin: 0;
            font-size: clamp(1.65rem, 3.8vw, 3rem);
            font-weight: 780;
            line-height: 1.08;
        }

        .sale-card p { margin: .65rem 0 1.2rem; color: #e5edf6; }

        .sale-button {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: .65rem;
            min-height: 48px;
            padding: .65rem 1.15rem;
            border-radius: 9px;
            background: var(--gold);
            color: var(--navy);
            font-weight: 750;
            text-decoration: none;
        }

        .sale-button:hover { background: var(--gold-hover); color: var(--navy); }

        .quick-grid {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 1rem;
            margin-top: 1rem;
        }

        .quick-card {
            display: grid;
            grid-template-columns: auto minmax(0, 1fr) auto;
            align-items: center;
            gap: clamp(.6rem, 2vw, 1.1rem);
            min-height: 135px;
            padding: clamp(.9rem, 2vw, 1.35rem);
            border: 1px solid var(--line);
            border-radius: 13px;
            background: #fff;
            color: var(--navy);
            text-decoration: none;
        }

        .quick-card:hover { border-color: #69839e; background: #f9fbff; color: var(--navy); }

        .quick-icon {
            padding-inline-end: clamp(.6rem, 2vw, 1rem);
            border-inline-end: 1px solid var(--line);
            font-size: clamp(1.5rem, 3.5vw, 2.3rem);
        }

        .quick-card h3 { margin: 0 0 .3rem; font-size: clamp(1.05rem, 1.8vw, 1.4rem); font-weight: 750; }
        .quick-card p { margin: 0; color: var(--muted); }
        .quick-arrow { color: #895a00; font-size: 1.25rem; }

        @media (min-width: 992px) {
            .cashier-layout { display: grid; grid-template-columns: var(--sidebar) minmax(0, 1fr); }
            .cashier-sidebar { position: sticky; top: 0; align-self: start; height: 100vh; }
            .sidebar-surface { min-height: 100vh; }
        }

        @media (max-width: 767.98px) {
            .quick-grid { grid-template-columns: minmax(0, 1fr); }
            .quick-card { min-height: 104px; }
            .sale-card { min-height: 195px; }
        }

        @media (max-width: 399.98px) {
            .sale-icon, .sale-decoration { display: none; }
            .user-info > i { display: none; }
        }
    </style>
</head>
<body>
    <a class="visually-hidden-focusable position-absolute top-0 start-0 z-3 bg-white p-2"
       href="#contenidoCajero">Ir al contenido principal</a>

    <div class="cashier-layout">
        <aside class="cashier-sidebar offcanvas-lg offcanvas-start" tabindex="-1"
               id="menuCajero" aria-labelledby="marcaCajero">
            <div class="sidebar-surface">
                <div class="d-flex justify-content-between align-items-center gap-2">
                    <a id="marcaCajero" class="brand" href="${urlInicio}">
                        <i class="bi bi-shop" aria-hidden="true"></i>
                        <span>La Esquina</span>
                    </a>
                    <button type="button" class="btn-close btn-close-white d-lg-none p-2"
                            data-bs-dismiss="offcanvas" data-bs-target="#menuCajero"
                            aria-label="Cerrar menú"></button>
                </div>

                <nav class="cashier-nav" aria-label="Navegación del cajero">
                    <a class="menu-link" href="${urlInicio}" aria-current="page">
                        <i class="bi bi-house-door" aria-hidden="true"></i> Inicio
                    </a>

                    <div class="nav-group" role="group" aria-labelledby="menuVentas">
                        <p class="nav-heading" id="menuVentas">Ventas</p>
                        <a class="menu-link" href="${urlVenta}">
                            <i class="bi bi-cart" aria-hidden="true"></i> Registrar venta
                        </a>
                    </div>

                    <div class="nav-group" role="group" aria-labelledby="menuInventario">
                        <p class="nav-heading" id="menuInventario">Inventario</p>
                        <a class="menu-link" href="${urlConsulta}">
                            <i class="bi bi-search" aria-hidden="true"></i> Consultar existencias
                        </a>
                        <a class="menu-link" href="${urlEntrada}">
                            <i class="bi bi-box-arrow-in-down" aria-hidden="true"></i> Registrar entrada
                        </a>
                    </div>
                </nav>
            </div>
        </aside>

        <div>
            <header class="topbar">
                <div class="title-group">
                    <button type="button" class="btn btn-outline-secondary d-lg-none"
                            data-bs-toggle="offcanvas" data-bs-target="#menuCajero" aria-controls="menuCajero">
                        <i class="bi bi-list" aria-hidden="true"></i> Menú
                    </button>
                    <h1>Panel del cajero</h1>
                </div>
                <div class="session-actions">
                    <div class="user-info">
                        <i class="bi bi-person-circle" aria-hidden="true"></i>
                        <div>
                            <strong><c:out value="${sessionScope.usuarioSesion.nombreCompleto}"/></strong>
                            <small>Cajero</small>
                        </div>
                    </div>
                    <a class="btn btn-outline-dark" href="<c:out value='${urlSalir}'/>">
                        <i class="bi bi-box-arrow-right me-1" aria-hidden="true"></i> Cerrar sesión
                    </a>
                </div>
            </header>

            <main class="cashier-main" id="contenidoCajero" tabindex="-1">
                <h2>Accesos rápidos</h2>

                <section class="sale-card" aria-labelledby="tituloVenta">
                    <i class="bi bi-cart sale-icon" aria-hidden="true"></i>
                    <i class="bi bi-cart sale-decoration" aria-hidden="true"></i>
                    <div>
                        <h3 id="tituloVenta">Registrar venta</h3>
                        <p>Agrega productos y cobra al cliente.</p>
                        <a class="sale-button" href="${urlVenta}">
                            Abrir caja <i class="bi bi-arrow-right" aria-hidden="true"></i>
                        </a>
                    </div>
                </section>

                <div class="quick-grid">
                    <a class="quick-card" href="${urlConsulta}" aria-labelledby="tituloConsulta">
                        <i class="bi bi-search quick-icon" aria-hidden="true"></i>
                        <div>
                            <h3 id="tituloConsulta">Consultar existencias</h3>
                            <p>Revisa el stock y el precio de un producto.</p>
                        </div>
                        <i class="bi bi-arrow-right quick-arrow" aria-hidden="true"></i>
                    </a>
                    <a class="quick-card" href="${urlEntrada}" aria-labelledby="tituloEntrada">
                        <i class="bi bi-box-arrow-in-down quick-icon" aria-hidden="true"></i>
                        <div>
                            <h3 id="tituloEntrada">Registrar entrada</h3>
                            <p>Agrega mercadería al inventario.</p>
                        </div>
                        <i class="bi bi-arrow-right quick-arrow" aria-hidden="true"></i>
                    </a>
                </div>
            </main>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

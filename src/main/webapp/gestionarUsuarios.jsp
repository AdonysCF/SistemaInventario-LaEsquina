
<%@page import="modelo.Usuario"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c" %>

<%
    Usuario usr = (Usuario) session.getAttribute("usuarioSesion");

    if (usr == null || !"GERENTE".equals(usr.getRol())) {
        response.sendError(
            HttpServletResponse.SC_FORBIDDEN,
            "Acceso denegado. Modulo exclusivo de Gerencia."
        );
        return;
    }
%>

<!DOCTYPE html>
<html lang="es">

<head>

    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Gestion de Usuarios - La Esquina</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"
          rel="stylesheet">

    <link rel="stylesheet"
          href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">

    <style>

        :root {
            --color-navy: #10253F;
            --color-mustard: #EAA81B;
        }

        body {
            background-color: #f4f6f9;
        }

        .bg-navy {
            background-color: var(--color-navy) !important;
        }

        .text-navy {
            color: var(--color-navy) !important;
        }

        .btn-mustard {
            background-color: var(--color-mustard);
            color: #000;
            border: none;
        }

        .btn-mustard:hover {
            background-color: #d19315;
            color: #000;
        }

        .card-custom {
            border: none;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.05);
        }

    </style>

</head>

<body>

    <!-- NAVBAR -->
    <nav class="navbar bg-navy shadow-sm py-2">

        <div class="container-fluid px-4">

            <a class="navbar-brand" href="panelGerente.jsp">
                <img src="img/logo.png"
                     alt="Logo La Esquina"
                     height="40">
            </a>

            <div class="d-flex align-items-center text-white">

                <i class="bi bi-person-circle fs-5 me-2"></i>

                <span class="me-3 fw-bold">
                    <%= usr.getNombreCompleto() %>
                </span>

                <a href="panelGerente.jsp"
                   class="btn btn-outline-light btn-sm">

                    <i class="bi bi-arrow-left me-1"></i>
                    Volver

                </a>

            </div>

        </div>

    </nav>

    <div class="container-fluid px-4 py-4">

        <!-- ENCABEZADO -->
        <div class="d-flex justify-content-between align-items-center mb-4">

            <div>

                <h3 class="text-navy fw-bold mb-1">
                    <i class="bi bi-people me-2"></i>
                    Gestion de Usuarios
                </h3>

                <p class="text-muted mb-0">
                    Administra las cuentas y roles del personal de la tienda.
                </p>

            </div>

            <button type="button"
                    class="btn btn-mustard fw-bold px-4"
                    data-bs-toggle="modal"
                    data-bs-target="#modalUsuario">

                <i class="bi bi-person-plus me-2"></i>
                Nuevo Usuario

            </button>

        </div>

        <!-- MENSAJES -->
        <c:if test="${not empty sessionScope.mensajeExitoUsuario}">

            <div class="alert alert-success">
                <i class="bi bi-check-circle me-2"></i>

                <c:out value="${sessionScope.mensajeExitoUsuario}"/>

            </div>

            <c:remove var="mensajeExitoUsuario" scope="session"/>

        </c:if>

        <c:if test="${not empty sessionScope.mensajeErrorUsuario}">

            <div class="alert alert-danger">
                <i class="bi bi-exclamation-triangle me-2"></i>

                <c:out value="${sessionScope.mensajeErrorUsuario}"/>

            </div>

            <c:remove var="mensajeErrorUsuario" scope="session"/>

        </c:if>

        <!-- TABLA DE USUARIOS -->
        <div class="card card-custom">

            <div class="card-header bg-white py-3">

                <h5 class="text-navy fw-bold mb-0">
                    <i class="bi bi-list-ul me-2"></i>
                    Personal registrado
                </h5>

            </div>

            <div class="card-body p-0">

                <div class="table-responsive">

                    <table class="table table-hover align-middle mb-0">

                        <thead class="table-light">

                            <tr>
                                <th class="ps-4">ID</th>
                                <th>Nombre completo</th>
                                <th>Usuario</th>
                                <th>Rol</th>
                                <th>Fecha de registro</th>
                                <th class="text-center">Estado</th>
                                <th class="text-center pe-4">Acciones</th>
                            </tr>

                        </thead>

                        <tbody>

                            <c:choose>

                                <c:when test="${empty listaUsuarios}">

                                    <tr>

                                        <td colspan="7"
                                            class="text-center text-muted p-5">

                                            <i class="bi bi-people fs-1 d-block mb-2"></i>

                                            No existen usuarios registrados.

                                        </td>

                                    </tr>

                                </c:when>

                                <c:otherwise>

                                    <c:forEach var="usuario"
                                               items="${listaUsuarios}">

                                        <tr>

                                            <td class="ps-4 text-muted fw-bold">
                                                ${usuario.idUsuario}
                                            </td>

                                            <td class="fw-bold text-navy">
                                                <c:out value="${usuario.nombreCompleto}"/>
                                            </td>

                                            <td>
                                                <c:out value="${usuario.username}"/>
                                            </td>

                                            <td>

                                                <c:choose>

                                                    <c:when test="${usuario.rol == 'GERENTE'}">

                                                        <span class="badge bg-primary">
                                                            <i class="bi bi-shield-check me-1"></i>
                                                            GERENTE
                                                        </span>

                                                    </c:when>

                                                    <c:otherwise>

                                                        <span class="badge bg-secondary">
                                                            <i class="bi bi-person me-1"></i>
                                                            CAJERO
                                                        </span>

                                                    </c:otherwise>

                                                </c:choose>

                                            </td>

                                            <td>
                                                <c:out value="${usuario.fechaCreacion}"/>
                                            </td>

                                            <td class="text-center">

                                                <c:choose>

                                                    <c:when test="${usuario.activo}">

                                                        <span class="badge bg-success rounded-pill px-3">
                                                            Activo
                                                        </span>

                                                    </c:when>

                                                    <c:otherwise>

                                                        <span class="badge bg-secondary rounded-pill px-3">
                                                            Inactivo
                                                        </span>

                                                    </c:otherwise>

                                                </c:choose>

                                            </td>

                                            <td class="text-center pe-4">

                                                <form action="UsuarioServlet"
                                                      method="POST"
                                                      class="d-inline"
                                                      onsubmit="return confirmarEstado(this)">

                                                    <input type="hidden"
                                                           name="accion"
                                                           value="cambiarEstado">

                                                    <input type="hidden"
                                                           name="idUsuario"
                                                           value="${usuario.idUsuario}">

                                                    <input type="hidden"
                                                           name="activo"
                                                           value="${!usuario.activo}">

                                                    <c:choose>

                                                        <c:when test="${usuario.activo}">

                                                            <button type="submit"
                                                                    class="btn btn-sm btn-outline-danger"
                                                                    title="Desactivar cuenta">

                                                                <i class="bi bi-person-x"></i>

                                                            </button>

                                                        </c:when>

                                                        <c:otherwise>

                                                            <button type="submit"
                                                                    class="btn btn-sm btn-outline-success"
                                                                    title="Activar cuenta">

                                                                <i class="bi bi-person-check"></i>

                                                            </button>

                                                        </c:otherwise>

                                                    </c:choose>

                                                </form>

                                            </td>

                                        </tr>

                                    </c:forEach>

                                </c:otherwise>

                            </c:choose>

                        </tbody>

                    </table>

                </div>

            </div>

            <div class="card-footer bg-white text-muted small">

                Total de cuentas:
                <strong>${listaUsuarios.size()}</strong>

            </div>

        </div>

    </div>

    <!-- MODAL REGISTRAR USUARIO -->
    <div class="modal fade"
         id="modalUsuario"
         tabindex="-1"
         aria-hidden="true">

        <div class="modal-dialog modal-dialog-centered">

            <div class="modal-content border-0 shadow">

                <div class="modal-header bg-navy text-white">

                    <h5 class="modal-title fw-bold">
                        <i class="bi bi-person-plus me-2"></i>
                        Registrar nuevo usuario
                    </h5>

                    <button type="button"
                            class="btn-close btn-close-white"
                            data-bs-dismiss="modal">
                    </button>

                </div>

                <form action="UsuarioServlet" method="POST">

                    <input type="hidden"
                           name="accion"
                           value="agregar">

                    <div class="modal-body p-4">

                        <div class="mb-3">

                            <label for="nombreCompleto"
                                   class="form-label fw-bold">

                                Nombre completo

                            </label>

                            <input type="text"
                                   class="form-control"
                                   id="nombreCompleto"
                                   name="nombreCompleto"
                                   maxlength="120"
                                   required>

                        </div>

                        <div class="mb-3">

                            <label for="username"
                                   class="form-label fw-bold">

                                Nombre de usuario

                            </label>

                            <input type="text"
                                   class="form-control"
                                   id="username"
                                   name="username"
                                   minlength="4"
                                   maxlength="50"
                                   pattern="[a-zA-Z0-9._-]+"
                                   autocomplete="off"
                                   required>

                            <small class="text-muted">
                                Entre 4 y 50 caracteres.
                            </small>

                        </div>

                        <div class="mb-3">

                            <label for="password"
                                   class="form-label fw-bold">

                                Contraseña

                            </label>

                            <input type="password"
                                   class="form-control"
                                   id="password"
                                   name="password"
                                   minlength="8"
                                   maxlength="72"
                                   autocomplete="new-password"
                                   required>

                            <small class="text-muted">
                                Debe contener al menos 8 caracteres.
                            </small>

                        </div>

                        <div class="mb-3">

                            <label for="rol"
                                   class="form-label fw-bold">

                                Rol del usuario

                            </label>

                            <select class="form-select"
                                    id="rol"
                                    name="rol"
                                    required>

                                <option value="">
                                    Seleccione un rol
                                </option>

                                <option value="CAJERO">
                                    Cajero
                                </option>

                                <option value="GERENTE">
                                    Gerente
                                </option>

                            </select>

                        </div>

                    </div>

                    <div class="modal-footer bg-light">

                        <button type="button"
                                class="btn btn-outline-secondary"
                                data-bs-dismiss="modal">

                            Cancelar

                        </button>

                        <button type="submit"
                                class="btn btn-success fw-bold px-4">

                            <i class="bi bi-save me-2"></i>
                            Registrar cuenta

                        </button>

                    </div>

                </form>

            </div>

        </div>

    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js">
    </script>

    <script>

        function confirmarEstado(formulario) {

            const nuevoEstado =
                formulario.querySelector('[name="activo"]').value;

            if (nuevoEstado === 'false') {

                return confirm(
                    '¿Estas seguro de desactivar esta cuenta de usuario?'
                );

            }

            return confirm(
                '¿Deseas volver a activar esta cuenta?'
            );
        }

    </script>

</body>

</html>

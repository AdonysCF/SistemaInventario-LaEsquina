
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

    <title>Categorías | La Esquina</title>

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

        .table-hover tbody tr:hover {
            background-color: rgba(234,168,27,0.05);
        }
    </style>
    <link rel="stylesheet" href="<c:url value='/css/operaciones-caja.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/gerencia.css'/>">
</head>

<body class="gerencia">
<c:set var="tituloGerencia" value="Categorías"/>
<c:set var="pantallaGerencia" value="Categorias"/>
<%@include file="/WEB-INF/encabezadoGerencia.jspf"%>
            <main class="cashier-main" id="contenidoGerencia" tabindex="-1">


    <div class="gerencia-modulo">

        <!-- ENCABEZADO -->
        <div class="d-flex flex-wrap gap-3 justify-content-between align-items-center mb-4">

            <div class="d-flex align-items-center">

                <i class="bi bi-tags fs-2 text-primary me-3"></i>

                <div>
                    

                    <p class="text-muted mb-0">
                        Administra las categorias de los productos de la tienda.
                    </p>
                </div>

            </div>

            <button type="button"
                    class="btn btn-mustard fw-bold px-4 shadow-sm"
                    onclick="abrirModalNueva()">

                <i class="bi bi-plus-lg me-2"></i>
                Nueva Categoria

            </button>

        </div>

        <!-- MENSAJES -->
        <c:if test="${not empty sessionScope.mensajeExitoCategoria}">
            <div class="alert alert-success shadow-sm">
                <i class="bi bi-check-circle me-2"></i>
                <c:out value="${sessionScope.mensajeExitoCategoria}"/>
            </div>

            <c:remove var="mensajeExitoCategoria" scope="session"/>
        </c:if>

        <c:if test="${not empty sessionScope.mensajeErrorCategoria}">
            <div class="alert alert-danger shadow-sm">
                <i class="bi bi-exclamation-triangle me-2"></i>
                <c:out value="${sessionScope.mensajeErrorCategoria}"/>
            </div>

            <c:remove var="mensajeErrorCategoria" scope="session"/>
        </c:if>

        <!-- TABLA -->
        <div class="card card-custom">

            <div class="card-header bg-white py-3">

                <h5 class="text-navy fw-bold mb-0">
                    <i class="bi bi-list-ul me-2"></i>
                    Listado de Categorias
                </h5>

            </div>

            <div class="card-body p-0">

                <div class="table-responsive">

                    <table class="table table-hover align-middle mb-0">

                        <thead class="table-light">

                            <tr>
                                <th class="ps-4">ID</th>
                                <th>Nombre</th>
                                <th>Descripcion</th>
                                <th class="text-center">Estado</th>
                                <th class="text-center pe-4">Acciones</th>
                            </tr>

                        </thead>

                        <tbody>

                            <c:choose>

                                <c:when test="${empty listaCategorias}">

                                    <tr>
                                        <td colspan="5"
                                            class="text-center p-5 text-muted">

                                            <i class="bi bi-inbox fs-1 d-block mb-2"></i>

                                            No existen categorias registradas.

                                        </td>
                                    </tr>

                                </c:when>

                                <c:otherwise>

                                    <c:forEach var="categoria"
                                               items="${listaCategorias}">

                                        <tr>

                                            <td class="ps-4 fw-bold text-muted">
                                                ${categoria.idCategoria}
                                            </td>

                                            <td class="fw-bold text-navy">
                                                <c:out value="${categoria.nombre}"/>
                                            </td>

                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty categoria.descripcion}">
                                                        <c:out value="${categoria.descripcion}"/>
                                                    </c:when>

                                                    <c:otherwise>
                                                        <span class="text-muted">
                                                            Sin descripcion
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>

                                            <td class="text-center">

                                                <c:choose>

                                                    <c:when test="${categoria.activo}">
                                                        <span class="badge bg-success rounded-pill px-3">
                                                            Activa
                                                        </span>
                                                    </c:when>

                                                    <c:otherwise>
                                                        <span class="badge bg-secondary rounded-pill px-3">
                                                            Inactiva
                                                        </span>
                                                    </c:otherwise>

                                                </c:choose>

                                            </td>

                                            <td class="text-center pe-4">

                                                <!-- EDITAR -->
                                                <button type="button"
                                                        class="btn btn-sm btn-outline-primary me-1"
                                                        title="Editar categoria"

                                                        data-id="${categoria.idCategoria}"
                                                        data-nombre="${categoria.nombre}"
                                                        data-descripcion="${categoria.descripcion}"

                                                        onclick="abrirModalEditar(this)">

                                                    <i class="bi bi-pencil-square"></i>

                                                </button>

                                                <!-- ACTIVAR / DESACTIVAR -->
                                                <form action="CategoriaServlet"
                                                      method="POST"
                                                      class="d-inline"
                                                      onsubmit="return confirmarEstado(this)">

                                                    <input type="hidden"
                                                           name="accion"
                                                           value="cambiarEstado">

                                                    <input type="hidden"
                                                           name="idCategoria"
                                                           value="${categoria.idCategoria}">

                                                    <input type="hidden"
                                                           name="activo"
                                                           value="${!categoria.activo}">

                                                    <c:choose>

                                                        <c:when test="${categoria.activo}">

                                                            <button type="submit"
                                                                    class="btn btn-sm btn-outline-danger"
                                                                    title="Desactivar categoria">

                                                                <i class="bi bi-toggle-on"></i>

                                                            </button>

                                                        </c:when>

                                                        <c:otherwise>

                                                            <button type="submit"
                                                                    class="btn btn-sm btn-outline-success"
                                                                    title="Activar categoria">

                                                                <i class="bi bi-toggle-off"></i>

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

            <div class="card-footer bg-white text-muted small py-3">

                Total de categorias:
                <strong>${listaCategorias.size()}</strong>

            </div>

        </div>

    </div>

    <!-- MODAL REGISTRAR / EDITAR -->
    <div class="modal fade"
         id="modalCategoria"
         tabindex="-1"
         aria-hidden="true">

        <div class="modal-dialog modal-dialog-centered">

            <div class="modal-content border-0 shadow">

                <div class="modal-header bg-navy text-white">

                    <h5 class="modal-title fw-bold" id="tituloModal">
                        Nueva Categoria
                    </h5>

                    <button type="button"
                            class="btn-close btn-close-white"
                            data-bs-dismiss="modal">
                    </button>

                </div>

                <form action="CategoriaServlet" method="POST">

                    <input type="hidden"
                           name="accion"
                           id="accionFormulario"
                           value="agregar">

                    <input type="hidden"
                           name="idCategoria"
                           id="idCategoria">

                    <div class="modal-body p-4">

                        <div class="mb-3">

                            <label for="nombre" class="form-label fw-bold">
                                Nombre de la categoria
                            </label>

                            <input type="text"
                                   class="form-control"
                                   id="nombre"
                                   name="nombre"
                                   maxlength="80"
                                   required>

                        </div>

                        <div class="mb-3">

                            <label for="descripcion" class="form-label fw-bold">
                                Descripcion
                            </label>

                            <textarea class="form-control"
                                      id="descripcion"
                                      name="descripcion"
                                      rows="3"
                                      maxlength="200"
                                      placeholder="Descripcion de la categoria">
                            </textarea>

                            <small class="text-muted">
                                Maximo 200 caracteres.
                            </small>

                        </div>

                    </div>

                    <div class="modal-footer bg-light">

                        <button type="button"
                                class="btn btn-outline-secondary fw-bold"
                                data-bs-dismiss="modal">

                            Cancelar

                        </button>

                        <button type="submit"
                                class="btn btn-success fw-bold px-4">

                            <i class="bi bi-save me-2"></i>
                            Guardar

                        </button>

                    </div>

                </form>

            </div>

        </div>

    </div>
            </main>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js">
    </script>

    <script>

        const modalCategoria = new bootstrap.Modal(
            document.getElementById('modalCategoria')
        );

        function abrirModalNueva() {

            document.getElementById('tituloModal').innerText =
                'Nueva Categoria';

            document.getElementById('accionFormulario').value =
                'agregar';

            document.getElementById('idCategoria').value = '';

            document.getElementById('nombre').value = '';

            document.getElementById('descripcion').value = '';

            modalCategoria.show();
        }

        function abrirModalEditar(boton) {

            document.getElementById('tituloModal').innerText =
                'Editar Categoria';

            document.getElementById('accionFormulario').value =
                'editar';

            document.getElementById('idCategoria').value =
                boton.dataset.id;

            document.getElementById('nombre').value =
                boton.dataset.nombre;

            document.getElementById('descripcion').value =
                boton.dataset.descripcion || '';

            modalCategoria.show();
        }

        function confirmarEstado(formulario) {

            const nuevoEstado =
                formulario.querySelector('[name="activo"]').value;

            if (nuevoEstado === 'false') {

                return confirm(
                    '¿Estas seguro de desactivar esta categoria?'
                );

            }

            return confirm(
                '¿Deseas volver a activar esta categoria?'
            );
        }

    </script>
</body>
</html>

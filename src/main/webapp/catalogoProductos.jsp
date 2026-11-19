
<%@page import="modelo.Usuario"%>
<%@page import="jakarta.servlet.http.HttpServletResponse"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%@taglib uri="jakarta.tags.functions" prefix="fn" %>

<%
    // Seguridad: solo el gerente puede administrar el catálogo.
    Usuario usr = (Usuario) session.getAttribute("usuarioSesion");

    if (usr == null || usr.getRol() == null
            || !"GERENTE".equals(usr.getRol())) {

        response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Acceso Denegado. Módulo exclusivo de Gerencia."
        );
        return;
    }
%>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <title>Productos | La Esquina</title>

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

            .text-mustard {
                color: var(--color-mustard) !important;
            }

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

            .card-custom {
                border: none;
                border-radius: 12px;
                box-shadow: 0 4px 12px rgba(0,0,0,0.05);
            }

            .table-hover tbody tr:hover {
                background-color: rgba(234, 168, 27, 0.05);
            }

            .img-miniatura {
                width: 45px;
                height: 45px;
                object-fit: contain;
                border-radius: 6px;
                background-color: white;
                border: 1px solid #dee2e6;
            }

            .form-text {
                font-size: 0.78rem;
            }
        </style>
        <link rel="stylesheet" href="<c:url value='/css/operaciones-caja.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/gerencia.css'/>">
</head>

    <body class="gerencia">
<c:set var="tituloGerencia" value="Productos"/>
<c:set var="pantallaGerencia" value="Productos"/>
<%@include file="/WEB-INF/encabezadoGerencia.jspf"%>
            <main class="cashier-main" id="contenidoGerencia" tabindex="-1">



        <div class="gerencia-modulo">

            <!-- ENCABEZADO DEL MÓDULO -->
            <div class="d-flex flex-wrap gap-3 justify-content-between align-items-center mb-4">

                <div class="d-flex align-items-center">

                    <i class="bi bi-boxes fs-2 text-primary me-3"></i>

                    <div>
                        

                        <p class="text-muted mb-0">
                            Administración de precios, códigos SKU y niveles de stock.
                        </p>
                    </div>

                </div>

                <button type="button"
                        class="btn btn-mustard fw-bold px-4 shadow-sm"
                        data-bs-toggle="modal"
                        data-bs-target="#modalNuevoProducto">

                    <i class="bi bi-plus-lg me-2"></i>
                    Nuevo Artículo

                </button>

            </div>


            <!-- MENSAJES DEL SISTEMA -->
            <c:if test="${not empty sessionScope.mensajeExito}">
                <div class="alert alert-success alert-dismissible fade show shadow-sm"
                     role="alert">

                    <i class="bi bi-check-circle me-2"></i>

                    <c:out value="${sessionScope.mensajeExito}"/>

                    <button type="button"
                            class="btn-close"
                            data-bs-dismiss="alert"
                            aria-label="Cerrar">
                    </button>
                </div>
            </c:if>

            <c:if test="${not empty sessionScope.mensajeError}">
                <div class="alert alert-danger alert-dismissible fade show shadow-sm"
                     role="alert">

                    <i class="bi bi-exclamation-triangle me-2"></i>

                    <c:out value="${sessionScope.mensajeError}"/>

                    <button type="button"
                            class="btn-close"
                            data-bs-dismiss="alert"
                            aria-label="Cerrar">
                    </button>
                </div>
            </c:if>


            <!-- TABLA DE PRODUCTOS -->
            <div class="card card-custom">

                <div class="card-body p-0">

                    <div class="table-responsive">

                        <table class="table table-hover align-middle mb-0">

                            <thead class="table-light">

                                <tr>
                                    <th class="ps-4 text-center">Imagen</th>
                                    <th>Código SKU</th>
                                    <th>Nombre del Artículo</th>
                                    <th class="text-end">Precio Venta</th>
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
                                            <td colspan="8"
                                                class="text-center p-5 text-muted">

                                                <i class="bi bi-inbox fs-1 d-block mb-2"></i>

                                                El catálogo de productos está vacío.

                                            </td>
                                        </tr>

                                    </c:when>


                                    <c:otherwise>

                                        <c:forEach var="p"
                                                   items="${listaProductos}">

                                            <tr>

                                                <!-- IMAGEN -->
                                                <td class="ps-4 text-center">

                                                    <c:choose>
                                                        <c:when test="${not empty p.rutaImagen}">
                                                            <img src="<c:out value='${p.rutaImagen}'/>"
                                                                 alt="Imagen del producto"
                                                                 class="img-miniatura">
                                                        </c:when>

                                                        <c:otherwise>
                                                            <img src="img/productos/default.png"
                                                                 alt="Imagen predeterminada"
                                                                 class="img-miniatura">
                                                        </c:otherwise>
                                                    </c:choose>

                                                </td>


                                                <!-- SKU -->
                                                <td class="fw-bold text-muted">
                                                    <c:out value="${p.codigoSku}"/>
                                                </td>


                                                <!-- NOMBRE -->
                                                <td class="fw-bold text-navy">
                                                    <c:out value="${p.nombre}"/>
                                                </td>


                                                <!-- PRECIO -->
                                                <td class="text-end text-success fw-bold">
                                                    S/
                                                    <c:out value="${p.precioUnitario}"/>
                                                </td>


                                                <!-- STOCK FÍSICO -->
                                                <td class="text-center fs-5 fw-bold">

                                                    <c:choose>

                                                        <c:when test="${p.stockActual == 0}">
                                                            <span class="text-danger">
                                                                <c:out value="${p.stockActual}"/>
                                                            </span>
                                                        </c:when>

                                                        <c:when test="${p.stockActual <= p.stockMinimo}">
                                                            <span class="text-warning">
                                                                <c:out value="${p.stockActual}"/>
                                                            </span>
                                                        </c:when>

                                                        <c:otherwise>
                                                            <c:out value="${p.stockActual}"/>
                                                        </c:otherwise>

                                                    </c:choose>

                                                </td>


                                                <!-- STOCK MÍNIMO -->
                                                <td class="text-center text-muted">
                                                    <c:out value="${p.stockMinimo}"/>
                                                </td>


                                                <!-- ESTADO DE ALERTA -->
                                                <td class="text-center">

                                                    <c:choose>

                                                        <c:when test="${p.estadoAlerta == 'DISPONIBLE'}">
                                                            <span class="badge bg-success rounded-pill px-3">
                                                                Disponible
                                                            </span>
                                                        </c:when>

                                                        <c:when test="${p.estadoAlerta == 'PROXIMO_A_AGOTARSE'}">
                                                            <span class="badge bg-warning text-dark rounded-pill px-3">
                                                                Poco Stock
                                                            </span>
                                                        </c:when>

                                                        <c:otherwise>
                                                            <span class="badge bg-danger rounded-pill px-3">
                                                                Agotado
                                                            </span>
                                                        </c:otherwise>

                                                    </c:choose>

                                                </td>


                                                <!-- ACCIONES -->
                                                <td class="text-center pe-4">

                                                    <button type="button"
                                                            class="btn btn-sm btn-outline-primary shadow-sm btn-editar-producto"
                                                            title="Editar precio o SKU"

                                                            data-id="<c:out value='${p.idProducto}'/>"
                                                            data-sku="<c:out value='${p.codigoSku}'/>"
                                                            data-nombre="<c:out value='${p.nombre}'/>"
                                                            data-precio="<c:out value='${p.precioUnitario}'/>"
                                                            data-stock="<c:out value='${p.stockMinimo}'/>">

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


                <!-- PIE DE TABLA -->
                <div class="card-footer bg-white text-muted small py-3
                            d-flex justify-content-between align-items-center">

                    <span>
                        Total de artículos en catálogo:
                        <strong>
                            <c:out value="${fn:length(listaProductos)}"/>
                        </strong>
                    </span>

                    <span>
                        Última actualización de la vista:
                        <%= new java.util.Date() %>
                    </span>

                </div>

            </div>

        </div>


        <!-- MODAL REGISTRAR PRODUCTO -->
        <div class="modal fade"
             id="modalNuevoProducto"
             tabindex="-1"
             aria-labelledby="modalNuevoProductoLabel"
             aria-hidden="true">

            <div class="modal-dialog modal-dialog-centered modal-lg">

                <div class="modal-content border-0 shadow">

                    <div class="modal-header bg-navy text-white">

                        <h5 class="modal-title fw-bold"
                            id="modalNuevoProductoLabel">

                            <i class="bi bi-box-seam me-2"></i>
                            Registrar Nuevo Artículo

                        </h5>

                        <button type="button"
                                class="btn-close btn-close-white"
                                data-bs-dismiss="modal"
                                aria-label="Cerrar">
                        </button>

                    </div>


                    <form action="ProductoServlet"
                          method="POST">

                        <input type="hidden"
                               name="accion"
                               value="agregar">


                        <div class="modal-body p-4">

                            <div class="row g-3">


                                <!-- SKU -->
                                <div class="col-md-6">

                                    <div class="form-floating">

                                        <input type="text"
                                               class="form-control"
                                               id="txtSku"
                                               name="txtSku"
                                               placeholder=" "
                                               required
                                               maxlength="30"
                                               pattern="[A-Za-z0-9][A-Za-z0-9._-]{0,29}"
                                               title="Utilice letras, números, guion, punto o guion bajo. Máximo 30 caracteres.">

                                        <label for="txtSku">
                                            Código SKU (Ej. ABA-0010)
                                        </label>

                                    </div>

                                    <div class="form-text">
                                        Máximo 30 caracteres. No utilice espacios.
                                    </div>

                                </div>


                                <!-- CATEGORÍA -->
                                <div class="col-md-6">

                                    <div class="form-floating">

                                        <select class="form-select"
                                                id="selCategoria"
                                                name="selCategoria"
                                                required>

                                            <option value="">
                                                Seleccione...
                                            </option>

                                            <c:forEach var="categoria"
                                                       items="${listaCategorias}">

                                                <option value="<c:out value='${categoria.idCategoria}'/>">

                                                    <c:out value="${categoria.nombre}"/>

                                                </option>

                                            </c:forEach>

                                        </select>

                                        <label for="selCategoria">
                                            Categoría
                                        </label>

                                    </div>

                                </div>


                                <!-- NOMBRE -->
                                <div class="col-12">

                                    <div class="form-floating">

                                        <input type="text"
                                               class="form-control"
                                               id="txtNombre"
                                               name="txtNombre"
                                               placeholder=" "
                                               required
                                               maxlength="150">

                                        <label for="txtNombre">
                                            Nombre completo del producto
                                        </label>

                                    </div>

                                    <div class="form-text">
                                        El nombre no puede superar los 150 caracteres.
                                    </div>

                                </div>


                                <!-- PRECIO -->
                                <div class="col-md-4">

                                    <div class="form-floating">

                                        <input type="number"
                                               step="0.01"
                                               min="0.01"
                                               max="99999999.99"
                                               class="form-control"
                                               id="numPrecio"
                                               name="numPrecio"
                                               placeholder=" "
                                               required>

                                        <label for="numPrecio">
                                            Precio Unitario (S/)
                                        </label>

                                    </div>

                                </div>


                                <!-- STOCK MÍNIMO -->
                                <div class="col-md-4">

                                    <div class="form-floating">

                                        <input type="number"
                                               step="1"
                                               min="0"
                                               class="form-control"
                                               id="numStockMin"
                                               name="numStockMin"
                                               placeholder=" "
                                               required
                                               value="5">

                                        <label for="numStockMin">
                                            Stock Mínimo (Alerta)
                                        </label>

                                    </div>

                                </div>


                                <!-- IMAGEN -->
                                <div class="col-md-4">

                                    <div class="form-floating">

                                        <input type="text"
                                               class="form-control"
                                               id="txtImagen"
                                               name="txtImagen"
                                               placeholder=" "
                                               maxlength="255"
                                               value="img/productos/default.png">

                                        <label for="txtImagen">
                                            Ruta de Imagen
                                        </label>

                                    </div>

                                    <div class="form-text">
                                        Campo opcional. Máximo 255 caracteres.
                                    </div>

                                </div>

                            </div>


                            <!-- INFORMACIÓN DE STOCK -->
                            <div class="alert alert-info mt-3 mb-0 small">

                                <i class="bi bi-info-circle me-1"></i>

                                Al crear un nuevo artículo, su stock inicial físico será de
                                <strong>0 unidades</strong>.

                                Para incrementar el stock, deberá registrar una entrada de mercadería.

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
                                Guardar Producto

                            </button>

                        </div>

                    </form>

                </div>
            </div>
        </div>



        <!-- MODAL EDITAR PRODUCTO -->
        <div class="modal fade"
             id="modalEditarProducto"
             tabindex="-1"
             aria-labelledby="modalEditarProductoLabel"
             aria-hidden="true">

            <div class="modal-dialog modal-dialog-centered">

                <div class="modal-content border-0 shadow">

                    <div class="modal-header bg-navy text-white">

                        <h5 class="modal-title fw-bold"
                            id="modalEditarProductoLabel">

                            <i class="bi bi-pencil-square me-2"></i>
                            Editar Artículo

                        </h5>

                        <button type="button"
                                class="btn-close btn-close-white"
                                data-bs-dismiss="modal"
                                aria-label="Cerrar">
                        </button>

                    </div>


                    <form action="ProductoServlet"
                          method="POST">

                        <input type="hidden"
                               name="accion"
                               value="editar">

                        <input type="hidden"
                               name="idProducto"
                               id="editIdProducto">


                        <div class="modal-body p-4">

                            <div class="mb-3">

                                <label class="form-label text-muted fw-bold small text-uppercase">
                                    Artículo seleccionado
                                </label>

                                <div id="editNombreProducto"
                                     class="fs-5 text-navy fw-bold">
                                    Nombre del Producto
                                </div>

                            </div>


                            <div class="row g-3">


                                <!-- SKU -->
                                <div class="col-md-6">

                                    <div class="form-floating">

                                        <input type="text"
                                               class="form-control"
                                               id="editSku"
                                               name="editSku"
                                               placeholder=" "
                                               required
                                               maxlength="30"
                                               pattern="[A-Za-z0-9][A-Za-z0-9._-]{0,29}"
                                               title="Utilice letras, números, guion, punto o guion bajo. Máximo 30 caracteres.">

                                        <label for="editSku">
                                            Código SKU
                                        </label>

                                    </div>

                                </div>


                                <!-- PRECIO -->
                                <div class="col-md-6">

                                    <div class="form-floating">

                                        <input type="number"
                                               step="0.01"
                                               min="0.01"
                                               max="99999999.99"
                                               class="form-control"
                                               id="editPrecio"
                                               name="editPrecio"
                                               placeholder=" "
                                               required>

                                        <label for="editPrecio">
                                            Precio Unitario (S/)
                                        </label>

                                    </div>

                                </div>


                                <!-- STOCK MÍNIMO -->
                                <div class="col-md-12">

                                    <div class="form-floating">

                                        <input type="number"
                                               step="1"
                                               min="0"
                                               class="form-control"
                                               id="editStockMin"
                                               name="editStockMin"
                                               placeholder=" "
                                               required>

                                        <label for="editStockMin">
                                            Stock Mínimo (Alerta)
                                        </label>

                                    </div>

                                </div>

                            </div>


                            <div class="alert alert-info mt-3 mb-0 small">

                                <i class="bi bi-info-circle me-1"></i>

                                Desde esta ventana solo se modifican el SKU,
                                el precio y el stock mínimo.

                                El stock físico se controla mediante los movimientos de inventario.

                            </div>

                        </div>


                        <div class="modal-footer bg-light">

                            <button type="button"
                                    class="btn btn-outline-secondary fw-bold"
                                    data-bs-dismiss="modal">

                                Cancelar

                            </button>

                            <button type="submit"
                                    class="btn btn-primary fw-bold px-4">

                                <i class="bi bi-save me-2"></i>
                                Guardar Cambios

                            </button>

                        </div>

                    </form>

                </div>
            </div>
        </div>



        <!-- JAVASCRIPT -->
            </main>
        </div>
    </div>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

        <script>

            // Abrir modal de edición y cargar los datos del producto.
            document.querySelectorAll(".btn-editar-producto").forEach(function (boton) {

                boton.addEventListener("click", function () {

                    const id = this.dataset.id;
                    const sku = this.dataset.sku;
                    const nombre = this.dataset.nombre;
                    const precio = this.dataset.precio;
                    const stockMin = this.dataset.stock;

                    document.getElementById("editIdProducto").value = id;
                    document.getElementById("editNombreProducto").textContent = nombre;
                    document.getElementById("editSku").value = sku;
                    document.getElementById("editPrecio").value = precio;
                    document.getElementById("editStockMin").value = stockMin;

                    const modal = new bootstrap.Modal(
                        document.getElementById("modalEditarProducto")
                    );

                    modal.show();

                });

            });


            // Evitar que se envíen nombres compuestos únicamente por espacios.
            document.getElementById("txtNombre").addEventListener("input", function () {
                this.setCustomValidity("");
            });

            document.getElementById("txtNombre").addEventListener("invalid", function () {

                if (this.value.trim() === "") {
                    this.setCustomValidity("Ingrese el nombre del producto.");
                }

            });

            document.getElementById("txtNombre").addEventListener("change", function () {
                this.value = this.value.trim();
            });


            // Limpiar espacios al inicio y final del SKU.
            document.getElementById("txtSku").addEventListener("change", function () {
                this.value = this.value.trim().toUpperCase();
            });

            document.getElementById("editSku").addEventListener("change", function () {
                this.value = this.value.trim().toUpperCase();
            });

        </script>

    
</body>
</html>

<%@page import="modelo.Usuario"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%
    // Seguridad Crítica (AC-03 y RN-05): Solo el Gerente puede administrar el catálogo maestro
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
        </style>
    </head>
    <body>

        <!-- NAVBAR EXCLUSIVO GERENCIA -->
        <nav class="navbar navbar-expand-lg bg-navy shadow-sm py-2">
            <div class="container-fluid px-4">
                <a class="navbar-brand" href="panelGerente.jsp">
                    <img src="img/logo.png" alt="Logo La Esquina" height="40">
                </a>
                <div class="d-flex align-items-center text-white ms-auto">
                    <i class="bi bi-person-circle fs-5 me-2" style="color: var(--color-mustard)"></i>
                    <span class="me-3 fw-bold">Gerencia: <%= usr.getNombreCompleto()%></span>
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
                <button type="button" class="btn btn-mustard fw-bold px-4 shadow-sm" data-bs-toggle="modal" data-bs-target="#modalNuevoProducto">
                    <i class="bi bi-plus-lg me-2"></i>Nuevo Artículo
                </button>
            </div>

            <c:if test="${not empty mensajeExito}">
                <div class="alert alert-success shadow-sm"><i class="bi bi-check-circle me-2"></i>${mensajeExito}</div>
                </c:if>
                <c:if test="${not empty mensajeError}">
                <div class="alert alert-danger shadow-sm"><i class="bi bi-exclamation-triangle me-2"></i>${mensajeError}</div>
                </c:if>

            <!-- Tarjeta de Resultados (Lista Maestra) -->
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
                                            <td colspan="8" class="text-center p-5 text-muted">
                                                <i class="bi bi-inbox fs-1 d-block mb-2"></i>
                                                El catálogo de productos está vacío.
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="p" items="${listaProductos}">
                                            <tr>
                                                <td class="ps-4 text-center">
                                                    <!-- Imagen del Producto -->
                                                    <img src="${not empty p.rutaImagen ? p.rutaImagen : 'img/productos/default.png'}" 
                                                         alt="${p.nombre}" class="img-miniatura">
                                                </td>
                                                <td class="fw-bold text-muted">${p.codigoSku}</td>
                                                <td class="fw-bold text-navy">${p.nombre}</td>
                                                <td class="text-end text-success fw-bold">S/ ${p.precioUnitario}</td>

                                                <!-- Columna de Stock Físico (Validación RN-02 y RN-04) -->
                                                <td class="text-center fs-5 fw-bold">
                                                    <c:choose>
                                                        <c:when test="${p.stockActual == 0}">
                                                            <span class="text-danger">${p.stockActual}</span>
                                                        </c:when>
                                                        <c:when test="${p.stockActual <= p.stockMinimo}">
                                                            <span class="text-warning">${p.stockActual}</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            ${p.stockActual}
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>

                                                <td class="text-center text-muted">${p.stockMinimo}</td>

                                                <!-- Columna de Estado Alerta -->
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

                                                <!-- Botones de Acción (Gerente) -->
                                                <td class="text-center pe-4">
                                                    <button type="button" class="btn btn-sm btn-outline-primary shadow-sm" title="Editar Precio o SKU" 
                                                            onclick="abrirModalEditar('${p.idProducto}', '${p.codigoSku}', '${p.nombre}', '${p.precioUnitario}', '${p.stockMinimo}')">
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

                <!-- Pie de la Tarjeta (Totalizador) -->
                <div class="card-footer bg-white text-muted small py-3 d-flex justify-content-between align-items-center">
                    <span>Total de artículos en catálogo: <strong>${listaProductos.size()}</strong></span>
                    <span>Última actualización de la vista: <%= new java.util.Date()%></span>
                </div>
            </div>
        </div>

        <!-- Script de Bootstrap -->
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

        <!-- Modal Nuevo Producto -->
        <div class="modal fade" id="modalNuevoProducto" tabindex="-1" aria-labelledby="modalNuevoProductoLabel" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered modal-lg">
                <div class="modal-content border-0 shadow">
                    <div class="modal-header bg-navy text-white">
                        <h5 class="modal-title fw-bold" id="modalNuevoProductoLabel"><i class="bi bi-box-seam me-2"></i>Registrar Nuevo Artículo</h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <!-- El formulario envía los datos por POST a ProductoServlet con accion=agregar -->
                    <form action="ProductoServlet" method="POST">
                        <input type="hidden" name="accion" value="agregar">
                        <div class="modal-body p-4">
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <div class="form-floating">
                                        <input type="text" class="form-control" id="txtSku" name="txtSku" placeholder=" " required maxlength="30">
                                        <label for="txtSku">Código SKU (Ej. ABA-0010)</label>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="form-floating">
                                        <select class="form-select" id="selCategoria" name="selCategoria" required>
                                            <option value="">Seleccione...</option>
                                            <!-- En un escenario real, deberías cargar estas opciones desde CategoriaDAO -->
                                            <option value="1">Abarrotes</option>
                                            <option value="2">Lácteos y Huevos</option>
                                            <option value="3">Bebidas</option>
                                            <option value="4">Limpieza del Hogar</option>
                                        </select>
                                        <label for="selCategoria">Categoría</label>
                                    </div>
                                </div>
                                <div class="col-12">
                                    <div class="form-floating">
                                        <input type="text" class="form-control" id="txtNombre" name="txtNombre" placeholder=" " required maxlength="150">
                                        <label for="txtNombre">Nombre completo del producto</label>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="form-floating">
                                        <input type="number" step="0.01" class="form-control" id="numPrecio" name="numPrecio" placeholder=" " required min="0.10">
                                        <label for="numPrecio">Precio Unitario (S/)</label>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="form-floating">
                                        <input type="number" class="form-control" id="numStockMin" name="numStockMin" placeholder=" " required min="1" value="5">
                                        <label for="numStockMin">Stock Mínimo (Alerta)</label>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="form-floating">
                                        <!-- Aquí se ingresaría la ruta de la imagen o el nombre del archivo -->
                                        <input type="text" class="form-control" id="txtImagen" name="txtImagen" placeholder=" " value="img/productos/default.png">
                                        <label for="txtImagen">Ruta de Imagen</label>
                                    </div>
                                </div>
                            </div>
                            <div class="alert alert-info mt-3 mb-0 small">
                                <i class="bi bi-info-circle me-1"></i> Al crear un nuevo artículo, su stock inicial físico será de <strong>0 unidades</strong>. Para incrementar el stock, deberá registrar una entrada de mercadería.
                            </div>
                        </div>
                        <div class="modal-footer bg-light">
                            <button type="button" class="btn btn-outline-secondary fw-bold" data-bs-dismiss="modal">Cancelar</button>
                            <button type="submit" class="btn btn-success fw-bold px-4"><i class="bi bi-save me-2"></i>Guardar Producto</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
        
        <!-- Modal Editar Producto -->
    <div class="modal fade" id="modalEditarProducto" tabindex="-1" aria-labelledby="modalEditarProductoLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content border-0 shadow">
                <div class="modal-header bg-navy text-white">
                    <h5 class="modal-title fw-bold" id="modalEditarProductoLabel"><i class="bi bi-pencil-square me-2"></i>Editar Artículo</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <!-- El formulario envía los datos por POST a ProductoServlet con accion=editar -->
                <form action="ProductoServlet" method="POST">
                    <input type="hidden" name="accion" value="editar">
                    <input type="hidden" name="idProducto" id="editIdProducto">
                    
                    <div class="modal-body p-4">
                        <div class="mb-3">
                            <label class="form-label text-muted fw-bold small text-uppercase">Artículo seleccionado</label>
                            <div id="editNombreProducto" class="fs-5 text-navy fw-bold">Nombre del Producto</div>
                        </div>
                        
                        <div class="row g-3">
                            <div class="col-md-6">
                                <div class="form-floating">
                                    <input type="text" class="form-control" id="editSku" name="editSku" placeholder=" " required maxlength="30">
                                    <label for="editSku">Código SKU</label>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="form-floating">
                                    <input type="number" step="0.01" class="form-control" id="editPrecio" name="editPrecio" placeholder=" " required min="0.10">
                                    <label for="editPrecio">Precio Unitario (S/)</label>
                                </div>
                            </div>
                            <div class="col-md-12">
                                <div class="form-floating">
                                    <input type="number" class="form-control" id="editStockMin" name="editStockMin" placeholder=" " required min="1">
                                    <label for="editStockMin">Stock Mínimo (Alerta)</label>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer bg-light">
                        <button type="button" class="btn btn-outline-secondary fw-bold" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btn btn-primary fw-bold px-4"><i class="bi bi-save me-2"></i>Guardar Cambios</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Script para llenar los datos en el modal de edición -->
    <script>
        function abrirModalEditar(id, sku, nombre, precio, stockMin) {
            // Llenar los campos del modal
            document.getElementById('editIdProducto').value = id;
            document.getElementById('editNombreProducto').innerText = nombre;
            document.getElementById('editSku').value = sku;
            document.getElementById('editPrecio').value = precio;
            document.getElementById('editStockMin').value = stockMin;
            
            // Abrir el modal usando Bootstrap
            var myModal = new bootstrap.Modal(document.getElementById('modalEditarProducto'));
            myModal.show();
        }
    </script>
    </body>
</html>
<%@page import="modelo.Usuario"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%
    // Seguridad (AC-03)
    Usuario usr = (Usuario) session.getAttribute("usuarioSesion");
    if (usr == null) { response.sendRedirect("index.jsp"); return; }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registrar Entrada - La Esquina</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <style>
        :root {
            --color-navy: #10253F;
            --color-mustard: #EAA81B;
        }
        body { background-color: #f4f6f9; }
        .bg-navy { background-color: var(--color-navy) !important; }
        .text-navy { color: var(--color-navy) !important; }
        .btn-mustard {
            background-color: var(--color-mustard);
            color: #000;
            border: none;
            transition: all 0.3s ease;
        }
        .btn-mustard:hover { background-color: #d19315; color: #000; }
        .card-custom {
            border: none;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.05);
        }
    </style>
</head>
<body>

    <!-- NAVBAR -->
    <nav class="navbar navbar-expand-lg bg-navy shadow-sm py-2">
        <div class="container-fluid px-4">
            <a class="navbar-brand" href="${sessionScope.usuarioSesion.rol == 'GERENTE' ? 'panelGerente.jsp' : 'panelCajero.jsp'}">
                <img src="img/logo.png" alt="Logo La Esquina" height="40">
            </a>
            <div class="d-flex align-items-center text-white ms-auto">
                <i class="bi bi-person-circle fs-5 me-2" style="color: var(--color-mustard)"></i>
                <span class="me-3 fw-bold"><%= usr.getNombreCompleto() %></span>
                <a href="${sessionScope.usuarioSesion.rol == 'GERENTE' ? 'panelGerente.jsp' : 'panelCajero.jsp'}" class="btn btn-outline-light btn-sm fw-bold">
                    <i class="bi bi-arrow-left me-1"></i> Volver
                </a>
            </div>
        </div>
    </nav>

    <div class="container-fluid px-4 py-4">
        
        <div class="d-flex align-items-center mb-4">
            <i class="bi bi-box-arrow-in-down fs-2 text-success me-3"></i>
            <div>
                <h3 class="text-navy fw-bold mb-0">Recepción de Mercadería</h3>
                <p class="text-muted mb-0">Ingreso de nuevos lotes o reposiciones al inventario.</p>
            </div>
        </div>

        <c:if test="${not empty mensajeExito}">
            <div class="alert alert-success shadow-sm"><i class="bi bi-check-circle me-2"></i>${mensajeExito}</div>
        </c:if>
        <c:if test="${not empty mensajeError}">
            <div class="alert alert-danger shadow-sm"><i class="bi bi-exclamation-triangle me-2"></i>${mensajeError}</div>
        </c:if>

        <form action="EntradaServlet" method="POST" id="formEntrada">
            
            <!-- PANEL SUPERIOR: Cabecera -->
            <div class="card card-custom mb-4">
                <div class="card-header bg-white border-bottom-0 pt-4 pb-0">
                    <h5 class="fw-bold text-navy"><i class="bi bi-file-earmark-text me-2"></i>1. Datos del Documento</h5>
                </div>
                <div class="card-body">
                    <div class="row g-3">
                        <div class="col-md-4">
                            <div class="form-floating">
                                <input type="text" class="form-control" id="txtDocumento" name="txtDocumento" placeholder=" " required>
                                <label for="txtDocumento">Doc. de Referencia (Ej. Factura/Guía)</label>
                            </div>
                        </div>
                        <div class="col-md-8">
                            <div class="form-floating">
                                <input type="text" class="form-control" id="txtObservacion" name="txtObservacion" placeholder=" ">
                                <label for="txtObservacion">Observación general (Opcional)</label>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row g-4">
                <!-- PANEL IZQUIERDO: Búsqueda y Selección -->
                <div class="col-md-5">
                    <div class="card card-custom h-100">
                        <div class="card-header bg-white border-bottom-0 pt-4 pb-0">
                            <h5 class="fw-bold text-navy"><i class="bi bi-search me-2"></i>2. Agregar Artículos</h5>
                        </div>
                        <div class="card-body">
                            <div class="form-floating mb-3">
                                <select id="selProducto" class="form-select" onchange="mostrarInfoProductoEntrada()">
                                    <option value="" data-stock="0">-- Seleccione un producto --</option>
                                    <c:forEach var="p" items="${listaProductos}">
                                        <option value="${p.idProducto}" data-nombre="${p.nombre}" data-stock="${p.stockActual}" data-sku="${p.codigoSku}">
                                            ${p.codigoSku} - ${p.nombre}
                                        </option>
                                    </c:forEach>
                                </select>
                                <label for="selProducto">Buscar producto activo</label>
                            </div>
                            
                            <div class="bg-light p-3 rounded mb-3">
                                <div class="d-flex justify-content-between mb-2">
                                    <span class="text-muted">Stock Físico Actual:</span>
                                    <span id="lblStockActual" class="fw-bold fs-5 text-navy">0 und.</span>
                                </div>
                            </div>

                            <div class="row g-2 align-items-center">
                                <div class="col-8">
                                    <div class="form-floating">
                                        <input type="number" id="numCantidadIngreso" class="form-control" min="1" value="1" placeholder=" ">
                                        <label for="numCantidadIngreso">Cantidad a ingresar</label>
                                    </div>
                                </div>
                                <div class="col-4">
                                    <button type="button" class="btn btn-navy w-100 h-100 fw-bold" style="background-color: var(--color-navy); color: white;" onclick="agregarItemEntrada()">
                                        <i class="bi bi-plus-lg"></i> Añadir
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- PANEL DERECHO: Lista de Ingreso -->
                <div class="col-md-7">
                    <div class="card card-custom h-100">
                        <div class="card-header bg-success text-white py-3">
                            <h5 class="card-title mb-0 fw-bold"><i class="bi bi-list-check me-2"></i>3. Detalle de Recepción</h5>
                        </div>
                        <div class="card-body p-0">
                            <div class="table-responsive" style="max-height: 300px; overflow-y: auto;">
                                <table class="table table-hover mb-0" id="tablaEntrada">
                                    <thead class="table-light sticky-top">
                                        <tr>
                                            <th>SKU</th>
                                            <th>Producto</th>
                                            <th class="text-center">Cant. Ingreso</th>
                                            <th class="text-center">Quitar</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <!-- Filas inyectadas por JS -->
                                    </tbody>
                                </table>
                            </div>
                            
                            <div class="p-4 bg-light border-top">
                                <button type="submit" class="btn btn-success btn-lg w-100 fw-bold shadow-sm" id="btnGuardarEntrada" disabled>
                                    <i class="bi bi-save me-2"></i>Confirmar Entrada y Actualizar Kardex
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </form>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function mostrarInfoProductoEntrada() {
            let sel = document.getElementById("selProducto");
            if(sel.value === "") {
                document.getElementById("lblStockActual").innerText = "0 und.";
                return;
            }
            let opt = sel.options[sel.selectedIndex];
            document.getElementById("lblStockActual").innerText = opt.getAttribute("data-stock") + " und.";
        }

        function agregarItemEntrada() {
            let sel = document.getElementById("selProducto");
            if(sel.value === "") { alert("Seleccione un producto."); return; }

            let opt = sel.options[sel.selectedIndex];
            let idProd = sel.value;
            let sku = opt.getAttribute("data-sku");
            let nombre = opt.getAttribute("data-nombre");
            let cant = parseInt(document.getElementById("numCantidadIngreso").value);

            if (cant <= 0 || isNaN(cant)) { alert("La cantidad debe ser mayor a cero."); return; }

            let tbody = document.getElementById("tablaEntrada").getElementsByTagName("tbody")[0];
            let fila = tbody.insertRow();

            fila.innerHTML = `
                <td class="fw-bold text-muted">\${sku}</td>
                <td>
                    <input type="hidden" name="idProducto[]" value="\${idProd}">
                    \${nombre}
                </td>
                <td class="text-center fw-bold text-success fs-5">
                    <input type="hidden" name="cantidad[]" value="\${cant}">
                    +\${cant}
                </td>
                <td class="text-center">
                    <button type="button" class="btn btn-sm btn-outline-danger" onclick="quitarFilaEntrada(this)"><i class="bi bi-trash"></i></button>
                </td>
            `;

            document.getElementById("btnGuardarEntrada").disabled = false;
            document.getElementById("numCantidadIngreso").value = 1;
        }

        function quitarFilaEntrada(btn) {
            let fila = btn.parentNode.parentNode;
            fila.parentNode.removeChild(fila);
            let tbody = document.getElementById("tablaEntrada").getElementsByTagName("tbody")[0];
            if (tbody.rows.length === 0) document.getElementById("btnGuardarEntrada").disabled = true;
        }
    </script>
</body>
</html>
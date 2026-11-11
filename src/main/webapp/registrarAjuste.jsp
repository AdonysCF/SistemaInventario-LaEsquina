<%@page import="modelo.Usuario"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%
    // Seguridad (AC-03 y RN-05): Solo Gerente
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
    <title>Registrar Ajuste de Stock - La Esquina</title>
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
        .text-mustard { color: var(--color-mustard) !important; }
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
        .badge-positivo { background-color: #d1e7dd; color: #0f5132; }
        .badge-negativo { background-color: #f8d7da; color: #842029; }
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
                <span class="me-3 fw-bold">Gerencia: <%= usr.getNombreCompleto() %></span>
                <a href="panelGerente.jsp" class="btn btn-outline-light btn-sm fw-bold">
                    <i class="bi bi-arrow-left me-1"></i> Volver al Panel
                </a>
            </div>
        </div>
    </nav>

    <div class="container-fluid px-4 py-4">
        
        <div class="d-flex align-items-center mb-4">
            <i class="bi bi-sliders fs-2 text-warning me-3"></i>
            <div>
                <h3 class="text-navy fw-bold mb-0">Ajuste de Inventario (Kardex)</h3>
                <p class="text-muted mb-0">Sincerar stock por mermas, vencimientos o descuadres físicos (RN-04).</p>
            </div>
        </div>

        <c:if test="${not empty mensajeExito}">
            <div class="alert alert-success shadow-sm"><i class="bi bi-check-circle me-2"></i>${mensajeExito}</div>
        </c:if>
        <c:if test="${not empty mensajeError}">
            <div class="alert alert-danger shadow-sm"><i class="bi bi-exclamation-triangle me-2"></i>${mensajeError}</div>
        </c:if>

        <form action="AjusteServlet" method="POST" id="formAjuste">
            
            <!-- PANEL SUPERIOR: Cabecera del Motivo (RN-03) -->
            <div class="card card-custom mb-4" style="border-top: 4px solid var(--color-mustard);">
                <div class="card-header bg-white border-bottom-0 pt-4 pb-0">
                    <h5 class="fw-bold text-navy"><i class="bi bi-card-text me-2"></i>1. Sustento del Ajuste (Obligatorio)</h5>
                </div>
                <div class="card-body">
                    <div class="row g-3">
                        <div class="col-md-3">
                            <div class="form-floating">
                                <select id="selTipoAjuste" name="selTipoAjuste" class="form-select" required>
                                    <option value="">Seleccione...</option>
                                    <option value="AJUSTE_POSITIVO">Ajuste Positivo (+)</option>
                                    <option value="AJUSTE_NEGATIVO">Ajuste Negativo (-)</option>
                                </select>
                                <label for="selTipoAjuste">Naturaleza del Ajuste</label>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="form-floating">
                                <input type="text" class="form-control" id="txtDocumento" name="txtDocumento" placeholder=" " required>
                                <label for="txtDocumento">Doc. Sustento (Ej. Informe N°10, Acta)</label>
                            </div>
                        </div>
                        <div class="col-md-5">
                            <div class="form-floating">
                                <input type="text" class="form-control" id="txtObservacion" name="txtObservacion" placeholder=" " required>
                                <label for="txtObservacion">Motivo detallado (Ej. Merma, caducidad, error de conteo)</label>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row g-4">
                <!-- PANEL IZQUIERDO: Buscador de Productos -->
                <div class="col-md-5">
                    <div class="card card-custom h-100">
                        <div class="card-header bg-white border-bottom-0 pt-4 pb-0">
                            <h5 class="fw-bold text-navy"><i class="bi bi-search me-2"></i>2. Agregar Artículos a Sincerar</h5>
                        </div>
                        <div class="card-body">
                            <div class="form-floating mb-3">
                                <select id="selProducto" class="form-select" onchange="mostrarInfoProducto()">
                                    <option value="" data-stock="0">-- Seleccione un producto --</option>
                                    <c:forEach var="p" items="${listaProductos}">
                                        <option value="${p.idProducto}" data-nombre="${p.nombre}" data-stock="${p.stockActual}" data-sku="${p.codigoSku}">
                                            ${p.codigoSku} - ${p.nombre}
                                        </option>
                                    </c:forEach>
                                </select>
                                <label for="selProducto">Buscar producto por SKU o Nombre</label>
                            </div>
                            
                            <div class="bg-light p-3 rounded mb-3 border">
                                <div class="d-flex justify-content-between mb-2">
                                    <span class="text-muted fw-bold">Stock Físico Actual:</span>
                                    <span id="lblStockActual" class="fw-bold fs-5 text-navy">0 und.</span>
                                </div>
                            </div>

                            <div class="row g-2 align-items-center">
                                <div class="col-8">
                                    <div class="form-floating">
                                        <input type="number" id="numCantidadAjuste" class="form-control" min="1" value="1" placeholder=" ">
                                        <label for="numCantidadAjuste" id="lblLabelCantidad">Cantidad a Ajustar</label>
                                    </div>
                                </div>
                                <div class="col-4">
                                    <button type="button" class="btn btn-mustard w-100 h-100 fw-bold" onclick="agregarItemAjuste()">
                                        <i class="bi bi-plus-lg"></i> Añadir
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- PANEL DERECHO: Detalle del Kardex a Modificar -->
                <div class="col-md-7">
                    <div class="card card-custom h-100">
                        <div class="card-header bg-warning py-3">
                            <h5 class="card-title mb-0 fw-bold text-dark"><i class="bi bi-list-check me-2"></i>3. Detalle de Modificación en Kardex</h5>
                        </div>
                        <div class="card-body p-0">
                            <div class="table-responsive" style="max-height: 300px; overflow-y: auto;">
                                <table class="table table-hover align-middle mb-0" id="tablaAjuste">
                                    <thead class="table-light sticky-top">
                                        <tr>
                                            <th>SKU</th>
                                            <th>Producto</th>
                                            <th class="text-center">Cant. Diferencia</th>
                                            <th class="text-center">Nuevo Stock</th>
                                            <th class="text-center"><i class="bi bi-trash"></i></th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <!-- Filas inyectadas por JS -->
                                    </tbody>
                                </table>
                            </div>
                            
                            <div class="p-4 bg-light border-top">
                                <button type="submit" class="btn btn-navy w-100 fw-bold shadow-sm py-2 fs-5" id="btnGuardarAjuste" style="background-color: var(--color-navy); color: white;" disabled>
                                    <i class="bi bi-save me-2"></i>Confirmar Ajuste Múltiple
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
        // Cambia la etiqueta del input dependiendo si eliges positivo o negativo
        document.getElementById("selTipoAjuste").addEventListener("change", function() {
            let tipo = this.value;
            let lbl = document.getElementById("lblLabelCantidad");
            if (tipo === "AJUSTE_POSITIVO") {
                lbl.innerText = "Cantidad sobrante (+)";
            } else if (tipo === "AJUSTE_NEGATIVO") {
                lbl.innerText = "Cantidad faltante (-)";
            } else {
                lbl.innerText = "Cantidad a Ajustar";
            }
        });

        function mostrarInfoProducto() {
            let sel = document.getElementById("selProducto");
            if(sel.value === "") {
                document.getElementById("lblStockActual").innerText = "0 und.";
                return;
            }
            let opt = sel.options[sel.selectedIndex];
            document.getElementById("lblStockActual").innerText = opt.getAttribute("data-stock") + " und.";
        }

        function agregarItemAjuste() {
            let tipoAjuste = document.getElementById("selTipoAjuste").value;
            if (tipoAjuste === "") { alert("Primero seleccione la Naturaleza del Ajuste (Paso 1)."); return; }

            let sel = document.getElementById("selProducto");
            if(sel.value === "") { alert("Seleccione un producto."); return; }

            let opt = sel.options[sel.selectedIndex];
            let idProd = sel.value;
            let sku = opt.getAttribute("data-sku");
            let nombre = opt.getAttribute("data-nombre");
            let stockReal = parseInt(opt.getAttribute("data-stock"));
            let cant = parseInt(document.getElementById("numCantidadAjuste").value);

            if (cant <= 0 || isNaN(cant)) { alert("La cantidad debe ser mayor a cero."); return; }

            let nuevoStock = 0;
            let operacionVisual = "";
            let claseBadge = "";

            if (tipoAjuste === "AJUSTE_POSITIVO") {
                nuevoStock = stockReal + cant;
                operacionVisual = "+" + cant;
                claseBadge = "badge-positivo";
            } else {
                // RN-02 Validación estricta para ajuste negativo
                if (cant > stockReal) {
                    alert("El ajuste negativo no puede ser mayor al stock actual (" + stockReal + "). Dejaría saldo en negativo.");
                    return;
                }
                nuevoStock = stockReal - cant;
                operacionVisual = "-" + cant;
                claseBadge = "badge-negativo";
            }

            let tbody = document.getElementById("tablaAjuste").getElementsByTagName("tbody")[0];
            let fila = tbody.insertRow();

            fila.innerHTML = `
                <td class="fw-bold text-muted">\${sku}</td>
                <td>
                    <input type="hidden" name="idProducto[]" value="\${idProd}">
                    \${nombre} <br><small class="text-muted">Stock ant: \${stockReal}</small>
                </td>
                <td class="text-center fs-5">
                    <input type="hidden" name="cantidad[]" value="\${cant}">
                    <span class="badge \${claseBadge} px-3">\${operacionVisual}</span>
                </td>
                <td class="text-center fw-bold fs-5 text-navy">\${nuevoStock}</td>
                <td class="text-center">
                    <button type="button" class="btn btn-sm btn-outline-danger" onclick="quitarFilaAjuste(this)"><i class="bi bi-trash"></i></button>
                </td>
            `;

            // Bloquear el combo de "Naturaleza" para que no mezclen positivos y negativos en la misma operación
            document.getElementById("selTipoAjuste").disabled = true;
            // Se crea un input oculto para mandar el valor, ya que disabled ignora el envio del select
            let hiddenTipo = document.createElement("input");
            hiddenTipo.type = "hidden";
            hiddenTipo.name = "txtTipoAjusteFinal";
            hiddenTipo.id = "txtTipoAjusteFinal";
            hiddenTipo.value = tipoAjuste;
            document.getElementById("formAjuste").appendChild(hiddenTipo);

            document.getElementById("btnGuardarAjuste").disabled = false;
            document.getElementById("numCantidadAjuste").value = 1;
        }

        function quitarFilaAjuste(btn) {
            let fila = btn.parentNode.parentNode;
            fila.parentNode.removeChild(fila);
            
            let tbody = document.getElementById("tablaAjuste").getElementsByTagName("tbody")[0];
            if (tbody.rows.length === 0) {
                document.getElementById("btnGuardarAjuste").disabled = true;
                // Desbloquea la selección de tipo de ajuste si vacían la tabla
                document.getElementById("selTipoAjuste").disabled = false;
                let hid = document.getElementById("txtTipoAjusteFinal");
                if(hid) hid.remove();
            }
        }
    </script>
</body>
</html>
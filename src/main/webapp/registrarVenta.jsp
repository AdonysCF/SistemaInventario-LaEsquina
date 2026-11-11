<%@page import="modelo.Usuario"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%
    // Seguridad
    Usuario usr = (Usuario) session.getAttribute("usuarioSesion");
    if (usr == null) { response.sendRedirect("index.jsp"); return; }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Punto de Venta - La Esquina</title>
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
        .card-custom {
            border: none;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.05);
        }
        .total-display {
            background-color: var(--color-navy);
            color: white;
            border-radius: 8px;
            padding: 20px;
            text-align: right;
        }
        .total-display .monto {
            font-size: 3rem;
            font-weight: 800;
            color: var(--color-mustard);
            line-height: 1;
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
                <i class="bi bi-person-badge fs-5 me-2" style="color: var(--color-mustard)"></i>
                <span class="me-3 fw-bold">Caja: <%= usr.getNombreCompleto() %></span>
                <a href="${sessionScope.usuarioSesion.rol == 'GERENTE' ? 'panelGerente.jsp' : 'panelCajero.jsp'}" class="btn btn-outline-light btn-sm fw-bold">
                    <i class="bi bi-arrow-left me-1"></i> Volver
                </a>
            </div>
        </div>
    </nav>

    <div class="container-fluid px-4 py-4">
        
        <c:if test="${not empty mensajeExito}">
            <div class="alert alert-success shadow-sm"><i class="bi bi-check-circle me-2"></i>${mensajeExito}</div>
        </c:if>
        <c:if test="${not empty mensajeError}">
            <div class="alert alert-danger shadow-sm"><i class="bi bi-exclamation-triangle me-2"></i>${mensajeError}</div>
        </c:if>

        <div class="row g-4">
            
            <!-- PANEL IZQUIERDO: Buscador -->
            <div class="col-lg-5">
                <div class="card card-custom h-100">
                    <div class="card-header bg-white border-bottom-0 pt-4 pb-2">
                        <h5 class="fw-bold text-navy"><i class="bi bi-search me-2"></i>Buscar Artículo</h5>
                    </div>
                    <div class="card-body">
                        
                        <div class="form-floating mb-4">
                            <select id="selProductoVenta" class="form-select form-select-lg" onchange="mostrarInfoProductoVenta()">
                                <option value="" data-precio="0" data-stock="0">-- Seleccione un producto --</option>
                                <c:forEach var="p" items="${listaProductos}">
                                    <option value="${p.idProducto}" data-nombre="${p.nombre}" data-precio="${p.precioUnitario}" data-stock="${p.stockActual}">
                                        ${p.codigoSku} - ${p.nombre}
                                    </option>
                                </c:forEach>
                            </select>
                            <label for="selProductoVenta">Buscar producto por SKU o Nombre</label>
                        </div>
                        
                        <div class="row g-3 mb-4">
                            <div class="col-6">
                                <div class="p-3 bg-light rounded text-center border">
                                    <span class="d-block text-muted small text-uppercase fw-bold mb-1">Precio Unitario</span>
                                    <span id="lblPrecioVenta" class="fs-4 fw-bold text-navy">S/ 0.00</span>
                                </div>
                            </div>
                            <div class="col-6">
                                <div class="p-3 bg-light rounded text-center border">
                                    <span class="d-block text-muted small text-uppercase fw-bold mb-1">Stock Disponible</span>
                                    <span id="lblStockVenta" class="fs-4 fw-bold text-secondary">0 und.</span>
                                </div>
                            </div>
                        </div>

                        <div class="row g-2 align-items-center">
                            <div class="col-7">
                                <div class="form-floating">
                                    <input type="number" id="numCantidadVenta" class="form-control form-control-lg fw-bold text-center" min="1" value="1" placeholder=" ">
                                    <label for="numCantidadVenta">Cantidad a Vender</label>
                                </div>
                            </div>
                            <div class="col-5">
                                <button type="button" class="btn btn-primary btn-lg w-100 h-100 fw-bold shadow-sm" onclick="agregarAlCarrito()">
                                    <i class="bi bi-cart-plus me-1"></i> Añadir
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- PANEL DERECHO: Ticket y Cobro -->
            <div class="col-lg-7">
                <div class="card card-custom h-100 d-flex flex-column">
                    <div class="card-body p-0 d-flex flex-column flex-grow-1">
                        
                        <!-- Totalizador Gigante -->
                        <div class="total-display m-3 shadow-sm">
                            <span class="text-uppercase fw-bold opacity-75 d-block mb-1">Total a Cobrar</span>
                            <div class="monto" id="lblTotalGeneral">S/ 0.00</div>
                        </div>

                        <form action="VentaServlet" method="POST" id="formVenta" class="d-flex flex-column flex-grow-1">
                            <div class="table-responsive flex-grow-1 px-3" style="max-height: 350px; overflow-y: auto;">
                                <table class="table table-hover align-middle" id="tablaCarrito">
                                    <thead class="table-light sticky-top">
                                        <tr>
                                            <th>Producto</th>
                                            <th class="text-center">Cant.</th>
                                            <th class="text-end">P. Unit</th>
                                            <th class="text-end">Subtotal</th>
                                            <th class="text-center"><i class="bi bi-trash"></i></th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <!-- JS inyecta filas aquí -->
                                    </tbody>
                                </table>
                            </div>
                            
                            <div class="p-3 mt-auto bg-light border-top">
                                <button type="submit" class="btn btn-success btn-lg w-100 fw-bold py-3 shadow" id="btnCobrar" disabled>
                                    <i class="bi bi-cash-coin me-2 fs-5"></i>Confirmar Venta y Generar Ticket
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        let totalVenta = 0.00;

        function mostrarInfoProductoVenta() {
            let sel = document.getElementById("selProductoVenta");
            if(sel.value === "") {
                document.getElementById("lblPrecioVenta").innerText = "S/ 0.00";
                document.getElementById("lblStockVenta").innerText = "0 und.";
                document.getElementById("lblStockVenta").className = "fs-4 fw-bold text-secondary";
                return;
            }
            let opt = sel.options[sel.selectedIndex];
            document.getElementById("lblPrecioVenta").innerText = "S/ " + parseFloat(opt.getAttribute("data-precio")).toFixed(2);
            
            let stock = parseInt(opt.getAttribute("data-stock"));
            let lblStock = document.getElementById("lblStockVenta");
            lblStock.innerText = stock + " und.";
            
            // RN-02 Ayuda visual
            if(stock === 0) lblStock.className = "fs-4 fw-bold text-danger";
            else if(stock <= 5) lblStock.className = "fs-4 fw-bold text-warning";
            else lblStock.className = "fs-4 fw-bold text-success";
        }

        function agregarAlCarrito() {
            let sel = document.getElementById("selProductoVenta");
            if(sel.value === "") { alert("Seleccione un producto."); return; }

            let opt = sel.options[sel.selectedIndex];
            let idProd = sel.value;
            let nombre = opt.getAttribute("data-nombre");
            let precioU = parseFloat(opt.getAttribute("data-precio"));
            let stockReal = parseInt(opt.getAttribute("data-stock"));
            let cant = parseInt(document.getElementById("numCantidadVenta").value);

            if (cant <= 0 || isNaN(cant)) { alert("La cantidad debe ser mayor a cero."); return; }
            
            // RN-02 Validación de stock en frontend
            if (cant > stockReal) {
                alert("¡Stock insuficiente!\nSolo quedan " + stockReal + " unidades de " + nombre + ".");
                return;
            }

            let subtotal = cant * precioU;
            totalVenta += subtotal;

            let tbody = document.getElementById("tablaCarrito").getElementsByTagName("tbody")[0];
            let fila = tbody.insertRow();

            fila.innerHTML = `
                <td class="fw-bold">
                    <input type="hidden" name="idProducto[]" value="\${idProd}">
                    \${nombre}
                </td>
                <td class="text-center fs-5">
                    <input type="hidden" name="cantidad[]" value="\${cant}">
                    <span class="badge bg-primary rounded-pill">\${cant}</span>
                </td>
                <td class="text-end text-muted">
                    <input type="hidden" name="precioUnitario[]" value="\${precioU}">
                    S/ \${precioU.toFixed(2)}
                </td>
                <td class="text-end fw-bold text-navy fs-5">S/ \${subtotal.toFixed(2)}</td>
                <td class="text-center">
                    <button type="button" class="btn btn-sm btn-outline-danger" onclick="quitarFilaVenta(this, \${subtotal})"><i class="bi bi-x-lg"></i></button>
                </td>
            `;

            actualizarTotalVenta();
            document.getElementById("btnCobrar").disabled = false;
            document.getElementById("numCantidadVenta").value = 1;
        }

        function quitarFilaVenta(btn, subtotalFila) {
            let fila = btn.parentNode.parentNode;
            fila.parentNode.removeChild(fila);
            
            totalVenta -= subtotalFila;
            actualizarTotalVenta();

            let tbody = document.getElementById("tablaCarrito").getElementsByTagName("tbody")[0];
            if (tbody.rows.length === 0) document.getElementById("btnCobrar").disabled = true;
        }

        function actualizarTotalVenta() {
            if(totalVenta < 0) totalVenta = 0;
            document.getElementById("lblTotalGeneral").innerText = "S/ " + totalVenta.toFixed(2);
        }
    </script>
</body>
</html>
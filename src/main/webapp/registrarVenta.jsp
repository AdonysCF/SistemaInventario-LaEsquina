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
    <title>Punto de Venta - La Esquina</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .caja-total { font-size: 1.5rem; font-weight: bold; color: #198754; text-align: right; }
    </style>
</head>
<body class="bg-light">
    
    <nav class="navbar navbar-dark bg-dark mb-4">
        <div class="container-fluid">
            <a class="navbar-brand" href="${sessionScope.usuarioSesion.rol == 'GERENTE' ? 'panelGerente.jsp' : 'panelCajero.jsp'}">La Esquina - Caja</a>
            <span class="navbar-text text-white">Atendiendo: ${sessionScope.usuarioSesion.nombreCompleto}</span>
            <a href="${sessionScope.usuarioSesion.rol == 'GERENTE' ? 'panelGerente.jsp' : 'panelCajero.jsp'}" class="btn btn-outline-light btn-sm">Volver al Panel</a>
        </div>
    </nav>

    <div class="container-fluid px-4">
        <div class="row">
            <!-- PANEL IZQUIERDO: Búsqueda y Selección (CU-10 integrado) -->
            <div class="col-md-5">
                <div class="card shadow-sm mb-4">
                    <div class="card-header bg-primary text-white">
                        <h5 class="card-title mb-0">Buscar Producto</h5>
                    </div>
                    <div class="card-body">
                        
                        <c:if test="${not empty mensajeExito}">
                            <div class="alert alert-success">${mensajeExito}</div>
                        </c:if>
                        <c:if test="${not empty mensajeError}">
                            <div class="alert alert-danger">${mensajeError}</div>
                        </c:if>

                        <div class="mb-3">
                            <label class="form-label fw-bold">Seleccionar Artículo:</label>
                            <select id="selProducto" class="form-select" onchange="mostrarInfoProducto()">
                                <option value="" data-precio="0" data-stock="0">-- Seleccione un producto --</option>
                                <c:forEach var="p" items="${listaProductos}">
                                    <option value="${p.idProducto}" 
                                            data-nombre="${p.nombre}" 
                                            data-precio="${p.precioUnitario}" 
                                            data-stock="${p.stockActual}">
                                        ${p.codigoSku} - ${p.nombre}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        
                        <div class="row mb-3">
                            <div class="col-6">
                                <label class="text-muted">Precio Unitario:</label>
                                <h4 id="lblPrecio">S/ 0.00</h4>
                            </div>
                            <div class="col-6">
                                <label class="text-muted">Stock Disponible:</label>
                                <h4 id="lblStock">0 und.</h4>
                            </div>
                        </div>

                        <div class="row align-items-end">
                            <div class="col-8">
                                <label class="form-label fw-bold">Cantidad a vender:</label>
                                <input type="number" id="numCantidad" class="form-control form-control-lg" min="1" value="1">
                            </div>
                            <div class="col-4">
                                <button type="button" class="btn btn-primary btn-lg w-100" onclick="agregarAlCarrito()">+ Añadir</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- PANEL DERECHO: Carrito de Venta y Cobro -->
            <div class="col-md-7">
                <div class="card shadow-sm">
                    <div class="card-header bg-success text-white">
                        <h5 class="card-title mb-0">Detalle de la Venta (Carrito)</h5>
                    </div>
                    <div class="card-body p-0">
                        <form action="VentaServlet" method="POST" id="formVenta">
                            <table class="table table-hover table-striped mb-0" id="tablaCarrito">
                                <thead class="table-light">
                                    <tr>
                                        <th>Producto</th>
                                        <th class="text-center">Cant.</th>
                                        <th class="text-end">P. Unit</th>
                                        <th class="text-end">Subtotal</th>
                                        <th class="text-center">Quitar</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <!-- Las filas se inyectan con JavaScript -->
                                </tbody>
                            </table>
                            
                            <div class="p-3 bg-light border-top text-end">
                                <span class="text-muted fs-5 me-3">TOTAL A COBRAR:</span>
                                <span class="caja-total" id="lblTotalGeneral">S/ 0.00</span>
                            </div>

                            <div class="p-3">
                                <button type="submit" class="btn btn-success btn-lg w-100 fw-bold" id="btnCobrar" disabled>
                                    Confirmar Venta y Descontar Stock
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        let totalVenta = 0.00;

        // Muestra la info del producto seleccionado al instante
        function mostrarInfoProducto() {
            let sel = document.getElementById("selProducto");
            let opt = sel.options[sel.selectedIndex];
            
            document.getElementById("lblPrecio").innerText = "S/ " + parseFloat(opt.getAttribute("data-precio")).toFixed(2);
            
            let stock = parseInt(opt.getAttribute("data-stock"));
            let lblStock = document.getElementById("lblStock");
            lblStock.innerText = stock + " und.";
            
            // Colorear stock para ayuda visual
            if(stock === 0) lblStock.className = "text-danger fw-bold";
            else if(stock <= 5) lblStock.className = "text-warning fw-bold";
            else lblStock.className = "text-success fw-bold";
        }

        function agregarAlCarrito() {
            let sel = document.getElementById("selProducto");
            if(sel.value === "") { alert("Seleccione un producto."); return; }

            let opt = sel.options[sel.selectedIndex];
            let idProd = sel.value;
            let nombre = opt.getAttribute("data-nombre");
            let precioU = parseFloat(opt.getAttribute("data-precio"));
            let stockReal = parseInt(opt.getAttribute("data-stock"));
            let cant = parseInt(document.getElementById("numCantidad").value);

            if (cant <= 0) { alert("La cantidad debe ser mayor a cero."); return; }
            
            // Validación Front-end de RN-02 (Impide agregar si no hay stock)
            if (cant > stockReal) {
                alert("¡Stock insuficiente!\nSolo quedan " + stockReal + " unidades de " + nombre + ".");
                return;
            }

            let subtotal = cant * precioU;
            totalVenta += subtotal;

            let tbody = document.getElementById("tablaCarrito").getElementsByTagName("tbody")[0];
            let fila = tbody.insertRow();

            // Insertamos inputs ocultos para que el Servlet reciba los arrays
            fila.innerHTML = `
                <td>
                    <input type="hidden" name="idProducto[]" value="\${idProd}">
                    \${nombre}
                </td>
                <td class="text-center">
                    <input type="hidden" name="cantidad[]" value="\${cant}">
                    \${cant}
                </td>
                <td class="text-end">
                    <input type="hidden" name="precioUnitario[]" value="\${precioU}">
                    S/ \${precioU.toFixed(2)}
                </td>
                <td class="text-end fw-bold">S/ \${subtotal.toFixed(2)}</td>
                <td class="text-center">
                    <button type="button" class="btn btn-sm btn-danger" onclick="quitarFila(this, \${subtotal})">X</button>
                </td>
            `;

            actualizarTotal();
            document.getElementById("btnCobrar").disabled = false;
            document.getElementById("numCantidad").value = 1; // reset
        }

        function quitarFila(btn, subtotalFila) {
            let fila = btn.parentNode.parentNode;
            fila.parentNode.removeChild(fila);
            
            totalVenta -= subtotalFila;
            actualizarTotal();

            let tbody = document.getElementById("tablaCarrito").getElementsByTagName("tbody")[0];
            if (tbody.rows.length === 0) document.getElementById("btnCobrar").disabled = true;
        }

        function actualizarTotal() {
            // Evitar redondeos negativos raros en JS
            if(totalVenta < 0) totalVenta = 0;
            document.getElementById("lblTotalGeneral").innerText = "S/ " + totalVenta.toFixed(2);
        }
    </script>
</body>
</html>
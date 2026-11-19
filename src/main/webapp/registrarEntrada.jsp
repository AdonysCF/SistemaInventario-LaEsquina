<%@page import="modelo.Usuario"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%
    Usuario usr = (Usuario) session.getAttribute("usuarioSesion");
    if (usr == null) { response.sendRedirect("index.jsp"); return; }
%>
<c:set var="esGerente" value="${sessionScope.usuarioSesion.rol eq 'GERENTE'}"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registrar entrada | La Esquina</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <link rel="stylesheet" href="<c:url value='/css/operaciones-caja.css'/>">
    <c:if test="${esGerente}">
        <link rel="stylesheet" href="<c:url value='/css/gerencia.css'/>">
    </c:if>
</head>
<body class="${esGerente ? 'gerencia' : ''}">
<c:set var="tituloOperacion" value="Registrar entrada"/>
<c:set var="operacionActiva" value="entrada"/>
<c:choose>
    <c:when test="${esGerente}">
        <c:set var="tituloGerencia" value="${tituloOperacion}"/>
        <c:set var="pantallaGerencia" value="Entrada"/>
        <%@include file="/WEB-INF/encabezadoGerencia.jspf"%>
    </c:when>
    <c:otherwise>
        <%@include file="/WEB-INF/encabezadoOperativo.jspf"%>
    </c:otherwise>
</c:choose>
            <main class="cashier-main"
                  id="${esGerente ? 'contenidoGerencia' : 'contenidoCajero'}" tabindex="-1">
                <div class="operation-intro">
                    <p>Ingresa el documento de referencia y luego agrega los productos recibidos.</p>
                </div>

                <c:if test="${not empty mensajeExito}">
                    <div class="alert alert-success shadow-sm"><i class="bi bi-check-circle me-2"></i><c:out value="${mensajeExito}"/></div>
                </c:if>
                <c:if test="${not empty mensajeError}">
                    <div class="alert alert-danger shadow-sm"><i class="bi bi-exclamation-triangle me-2"></i><c:out value="${mensajeError}"/></div>
                </c:if>

                <form action="EntradaServlet" method="POST" id="formEntrada">

                    <div class="card card-custom mb-4">
                        <div class="card-header bg-white border-bottom-0 pt-4 pb-0">
                            <h2 class="fw-bold text-navy"><i class="bi bi-file-earmark-text me-2"></i>1. Datos del documento</h2>
                        </div>
                        <div class="card-body">
                            <div class="row g-3">
                                <div class="col-md-5">
                                    <div class="form-floating">
                                        <input type="text" class="form-control" id="txtDocumento" name="txtDocumento" placeholder=" " required>
                                        <label for="txtDocumento">N.º de factura o guía</label>
                                    </div>
                                </div>
                                <div class="col-md-7">
                                    <div class="form-floating">
                                        <input type="text" class="form-control" id="txtObservacion" name="txtObservacion" placeholder=" ">
                                        <label for="txtObservacion">Observación (opcional)</label>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="row g-4">
                        <div class="col-xl-5">
                            <div class="card card-custom h-100">
                                <div class="card-header bg-white border-bottom-0 pt-4 pb-0">
                                    <h2 class="fw-bold text-navy"><i class="bi bi-search me-2"></i>2. Agregar productos</h2>
                                </div>
                                <div class="card-body">
                                    <div class="form-floating mb-3">
                                        <select id="selProducto" class="form-select" onchange="mostrarInfoProductoEntrada()">
                                            <option value="" data-stock="0" data-img="">-- Seleccione un producto --</option>
                                            <c:forEach var="p" items="${listaProductos}">
                                                <option value="${p.idProducto}" 
                                                        data-nombre="${p.nombre}" 
                                                        data-stock="${p.stockActual}" 
                                                        data-sku="${p.codigoSku}"
                                                        data-img="${not empty p.rutaImagen ? p.rutaImagen : 'img/productos/default.png'}">
                                                    ${p.codigoSku} - ${p.nombre}
                                                </option>
                                            </c:forEach>
                                        </select>
                                        <label for="selProducto">Producto</label>
                                    </div>

                                    <div class="bg-light p-3 rounded mb-3 d-flex align-items-center justify-content-between border">
                                        <img id="imgPreviaEntrada" src="img/productos/default.png" alt="Previa" style="max-height: 50px; max-width: 50px; object-fit: contain;">
                                        <div class="text-end">
                                            <span class="text-muted d-block small">Stock Físico Actual:</span>
                                            <span id="lblStockActual" class="fw-bold fs-5 text-navy">0 und.</span>
                                        </div>
                                    </div>

                                    <div class="row g-2 align-items-stretch">
                                        <div class="col-12 col-sm-7">
                                            <div class="form-floating">
                                                <input type="number" id="numCantidadIngreso" class="form-control" min="1" value="1" placeholder=" ">
                                                <label for="numCantidadIngreso">Cantidad recibida</label>
                                            </div>
                                        </div>
                                        <div class="col-12 col-sm-5">
                                            <button type="button" class="btn btn-operativo operation-button w-100 h-100 fw-bold" onclick="agregarItemEntrada()">
                                                <i class="bi bi-plus-lg"></i> Añadir
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="col-xl-7">
                            <div class="card card-custom h-100">
                                <div class="card-header bg-navy text-white py-3">
                                    <h2 class="card-title mb-0 fw-bold"><i class="bi bi-list-check me-2"></i>3. Detalle de recepción</h2>
                                </div>
                                <div class="card-body p-0 d-flex flex-column h-100">
                                    <div class="table-responsive flex-grow-1" style="max-height: 300px; overflow-y: auto;">
                                        <table class="table table-hover align-middle mb-0" id="tablaEntrada">
                                            <thead class="table-light sticky-top">
                                                <tr>
                                                    <th>Imagen</th>
                                                    <th>SKU</th>
                                                    <th>Producto</th>
                                                    <th class="text-center">Ingreso</th>
                                                    <th class="text-center">Quitar</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <!-- JS inyecta filas -->
                                            </tbody>
                                        </table>
                                    </div>

                                    <div class="p-4 bg-light border-top mt-auto">
                                        <button type="submit" class="btn btn-confirmar operation-button w-100 fw-bold shadow-sm" id="btnGuardarEntrada" disabled>
                                            <i class="bi bi-save me-2"></i>Confirmar entrada
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </form>
            </main>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function mostrarInfoProductoEntrada() {
            let sel = document.getElementById("selProducto");
            if(sel.value === "") {
                document.getElementById("lblStockActual").innerText = "0 und.";
                document.getElementById("imgPreviaEntrada").src = "img/productos/default.png";
                return;
            }
            let opt = sel.options[sel.selectedIndex];
            document.getElementById("lblStockActual").innerText = opt.getAttribute("data-stock") + " und.";
            document.getElementById("imgPreviaEntrada").src = opt.getAttribute("data-img");
        }

        function agregarItemEntrada() {
            let sel = document.getElementById("selProducto");
            if(sel.value === "") { alert("Seleccione un producto."); return; }

            let opt = sel.options[sel.selectedIndex];
            let idProd = sel.value;
            let sku = opt.getAttribute("data-sku");
            let nombre = opt.getAttribute("data-nombre");
            let imgSrc = opt.getAttribute("data-img");
            let cant = parseInt(document.getElementById("numCantidadIngreso").value);

            if (cant <= 0 || isNaN(cant)) { alert("La cantidad debe ser mayor a cero."); return; }

            let tbody = document.getElementById("tablaEntrada").getElementsByTagName("tbody")[0];
            let fila = tbody.insertRow();

            fila.innerHTML = `
                <td class="text-center">
                    <img src="\${imgSrc}" alt="Prod" class="img-miniatura">
                </td>
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

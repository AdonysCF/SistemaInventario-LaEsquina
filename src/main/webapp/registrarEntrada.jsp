<%@page import="modelo.Usuario"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%
    Usuario usr = (Usuario) session.getAttribute("usuarioSesion");
    if (usr == null) {
        response.sendRedirect("index.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <title>Registrar Entrada - La Esquina</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    </head>
    <body class="bg-light">

        <nav class="navbar navbar-dark bg-dark mb-4">
            <div class="container-fluid">
                <!-- Actualizado para que apunte al panel correspondiente -->
                <a class="navbar-brand" href="${sessionScope.usuarioSesion.rol == 'GERENTE' ? 'panelGerente.jsp' : 'panelCajero.jsp'}">La Esquina - Recepción</a>

                <!-- Actualizado para leer correctamente el objeto de la sesión en JSTL -->
                <a href="${sessionScope.usuarioSesion.rol == 'GERENTE' ? 'panelGerente.jsp' : 'panelCajero.jsp'}" class="btn btn-outline-light btn-sm">Volver al Panel</a>
            </div>
        </nav>

        <div class="container">
            <h2>Registrar Entrada de Mercadería</h2>
            <hr>

            <c:if test="${not empty mensajeExito}">
                <div class="alert alert-success">${mensajeExito}</div>
            </c:if>
            <c:if test="${not empty mensajeError}">
                <div class="alert alert-danger">${mensajeError}</div>
            </c:if>

            <form action="EntradaServlet" method="POST">
                <div class="row mb-3">
                    <div class="col-md-6">
                        <label>Documento Referencia (Guía/Boleta):</label>
                        <input type="text" name="txtDocumento" class="form-control" required>
                    </div>
                    <div class="col-md-6">
                        <label>Observación:</label>
                        <input type="text" name="txtObservacion" class="form-control">
                    </div>
                </div>

                <!-- Controles para agregar ítem -->
                <div class="card p-3 mb-4 bg-white">
                    <div class="row align-items-end">
                        <div class="col-md-6">
                            <label>Seleccionar Producto:</label>
                            <select id="selProducto" class="form-select">
                                <c:forEach var="p" items="${listaProductos}">
                                    <option value="${p.idProducto}" data-nombre="${p.nombre}">${p.codigoSku} - ${p.nombre}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <label>Cantidad Recibida:</label>
                            <input type="number" id="numCantidad" class="form-control" min="1" value="1">
                        </div>
                        <div class="col-md-3">
                            <button type="button" class="btn btn-primary w-100" onclick="agregarFila()">+ Añadir a la lista</button>
                        </div>
                    </div>
                </div>

                <!-- Tabla Detalle (Carrito) -->
                <table class="table table-bordered bg-white" id="tablaDetalle">
                    <thead class="table-dark">
                        <tr>
                            <th>Producto</th>
                            <th>Cantidad</th>
                            <th>Acción</th>
                        </tr>
                    </thead>
                    <tbody>
                        <!-- Aquí se agregan las filas con JavaScript -->
                    </tbody>
                </table>

                <button type="submit" class="btn btn-success btn-lg w-100" id="btnConfirmar" disabled>Confirmar Entrada y Actualizar Stock</button>
            </form>
        </div>

        <script>
            function agregarFila() {
                var sel = document.getElementById("selProducto");
                var idProd = sel.value;
                var nombreProd = sel.options[sel.selectedIndex].getAttribute("data-nombre");
                var cant = document.getElementById("numCantidad").value;

                if (cant <= 0) {
                    alert("La cantidad debe ser mayor a 0");
                    return;
                }

                var tbody = document.getElementById("tablaDetalle").getElementsByTagName("tbody")[0];
                var fila = tbody.insertRow();

                // Insertamos los inputs ocultos (name="idProducto[]" y name="cantidad[]") que leerá el Servlet
                fila.innerHTML = `
                    <td><input type="hidden" name="idProducto[]" value="\${idProd}">\${nombreProd}</td>
                    <td><input type="hidden" name="cantidad[]" value="\${cant}">\${cant}</td>
                    <td><button type="button" class="btn btn-danger btn-sm" onclick="eliminarFila(this)">Quitar</button></td>
                `;
                document.getElementById("btnConfirmar").disabled = false;
            }

            function eliminarFila(btn) {
                var fila = btn.parentNode.parentNode;
                fila.parentNode.removeChild(fila);
                var tbody = document.getElementById("tablaDetalle").getElementsByTagName("tbody")[0];
                if (tbody.rows.length === 0)
                    document.getElementById("btnConfirmar").disabled = true;
            }
        </script>
    </body>
</html>
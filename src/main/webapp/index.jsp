<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%
    // Verificación de sesión activa
    modelo.Usuario usr = (modelo.Usuario) session.getAttribute("usuarioSesion");
    if (usr != null) {
        if (usr.getRol().equals("GERENTE")) { response.sendRedirect("panelGerente.jsp"); }
        else { response.sendRedirect("panelCajero.jsp"); }
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Iniciar Sesión - La Esquina</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <style>
        :root {
            --color-navy: #10253F;
            --color-mustard: #EAA81B;
        }
        body { background-color: #f4f6f9; }
        .text-navy { color: var(--color-navy) !important; }
        .btn-mustard {
            background-color: var(--color-mustard);
            color: #000;
            border: none;
            transition: all 0.3s;
        }
        .btn-mustard:hover { background-color: #d19315; }
        .card-login {
            border: none;
            border-radius: 12px;
            box-shadow: 0 8px 24px rgba(0,0,0,0.08);
        }
        /* Ajustes específicos para las etiquetas flotantes */
        .form-floating > label {
            color: #6c757d; /* Color gris suave cuando está inactivo */
        }
        .form-floating > .form-control:focus ~ label,
        .form-floating > .form-control:not(:placeholder-shown) ~ label {
            color: var(--color-navy); /* Color azul marino cuando sube la etiqueta */
            font-weight: 500;
        }
        .form-control:focus {
            border-color: var(--color-navy);
            box-shadow: 0 0 0 0.25rem rgba(16, 37, 63, 0.15);
        }
    </style>
</head>
<body class="d-flex justify-content-center align-items-center vh-100">

    <div class="container">
        <div class="row justify-content-center">
            <div class="col-12 col-sm-8 col-md-6 col-lg-4">
                
                <div class="card card-login p-4">
                    <div class="card-body text-center">
                        
                        <img src="img/logo.png" alt="Logo La Esquina" class="img-fluid mb-4" style="max-height: 90px;">
                        <h5 class="text-navy fw-bold mb-4">Acceso al Sistema</h5>

                        <c:if test="${not empty mensajeError}">
                            <div class="alert alert-danger py-2 text-start" role="alert">
                                <small>${mensajeError}</small>
                            </div>
                        </c:if>

                        <form action="login" method="POST">
                            
                            <!-- ETIQUETA FLOTANTE: Nombre de usuario -->
                            <div class="form-floating mb-3">
                                <!-- Nota: el placeholder=" " es vital para que funcione el efecto flotante en Bootstrap -->
                                <input type="text" class="form-control" id="txtUsuario" name="txtUsuario" placeholder=" " required autocomplete="off">
                                <label for="txtUsuario">Nombre de usuario</label>
                            </div>

                            <!-- ETIQUETA FLOTANTE: Contraseña -->
                            <div class="form-floating mb-4">
                                <!-- Nota: el placeholder=" " es vital -->
                                <input type="password" class="form-control" id="txtPassword" name="txtPassword" placeholder=" " required>
                                <label for="txtPassword">Contraseña</label>
                            </div>

                            <button type="submit" class="btn btn-mustard w-100 py-2 fw-bold fs-5 shadow-sm">
                                Iniciar Sesión
                            </button>
                        </form>
                        
                    </div>
                </div>
                
                <div class="text-center mt-4 text-muted small">
                    &copy; 2026 La Esquina - Sistema de Gestión de Inventario
                </div>

            </div>
        </div>
    </div>
    
    <!-- Script de Bootstrap necesario para algunas interacciones -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - Tienda La Esquina</title>
    <!-- Incluimos Bootstrap para un diseño rápido y profesional -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f4f7f6;
            display: flex;
            align-items: center;
            justify-content: center;
            height: 100vh;
        }
        .login-card {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
            width: 100%;
            max-width: 400px;
        }
        .brand-title {
            color: #2c3e50;
            font-weight: bold;
            text-align: center;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>

    <div class="login-card">
        <h3 class="brand-title">Tienda "La Esquina"</h3>
        <h5 class="text-center text-muted mb-4">Sistema de Inventario</h5>
        
        <!-- Bloque para mostrar mensajes de error desde el AuthController -->
        <% 
            String error = (String) request.getAttribute("errorMensaje");
            if (error != null) { 
        %>
            <div class="alert alert-danger" role="alert">
                <%= error %>
            </div>
        <% } %>

        <!-- Formulario que envía los datos al AuthController mediante POST -->
        <form action="login" method="POST">
            <div class="mb-3">
                <label for="txtUsername" class="form-label">Usuario</label>
                <!-- El atributo 'name' es clave para recuperar el dato en el Servlet -->
                <input type="text" class="form-control" id="txtUsername" name="txtUsername" required placeholder="Ingrese su usuario">
            </div>
            
            <div class="mb-4">
                <label for="txtPassword" class="form-label">Contraseña</label>
                <!-- El atributo 'name' debe coincidir con request.getParameter() -->
                <input type="password" class="form-control" id="txtPassword" name="txtPassword" required placeholder="Ingrese su contraseña">
            </div>
            
            <button type="submit" class="btn btn-primary w-100">Ingresar al Sistema</button>
        </form>
        
        <div class="text-center mt-3">
            <small class="text-muted">Desarrollado para el Proyecto T2 - Arquitectura MVC</small>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
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
<c:url var="urlLogin" value="/login"/>
<c:url var="urlLogo" value="/img/logo.png"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Iniciar sesión | La Esquina</title>
    <style>
        :root {
            --navy: #10243a;
            --navy-hover: #1c3955;
            --gold: #efab22;
            --paper: #fbfcfd;
            --brand-paper: #f0f4f8;
            --border: #8b99a8;
            --ink: #11263d;
        }

        *, *::before, *::after { box-sizing: border-box; }

        body {
            margin: 0;
            background: var(--navy);
            color: var(--ink);
            font-family: system-ui, -apple-system, "Segoe UI", sans-serif;
        }

        .login-page {
            position: relative;
            isolation: isolate;
            display: grid;
            place-items: center;
            min-height: 100vh;
            min-height: 100dvh;
            padding: clamp(1rem, 5vw, 3rem);
            overflow: hidden;
        }

        .login-page::before, .login-page::after {
            position: absolute;
            z-index: -1;
            width: min(27vw, 290px);
            aspect-ratio: 1;
            border: 1px solid #31506a;
            box-shadow: 2.25rem 2.25rem 0 -1px var(--navy), 2.25rem 2.25rem 0 #31506a;
            content: "";
            pointer-events: none;
        }

        .login-page::before { inset: -7rem -2.5rem auto auto; }
        .login-page::after { inset: auto auto -7rem -2.5rem; transform: rotate(180deg); }

        .login-card {
            width: min(100%, 530px);
            border: 1px solid #dce3eb;
            border-radius: 18px;
            background: var(--paper);
            box-shadow: 0 18px 46px rgb(0 0 0 / 24%);
        }

        .login-brand, .login-body { padding-inline: clamp(1.25rem, 4.8vw, 2rem); }

        .login-brand {
            display: flex;
            align-items: center;
            min-height: 118px;
            padding-block: 1.15rem;
            border-radius: 17px 17px 0 0;
            background: var(--brand-paper);
        }

        /* Muestra la marca sin los márgenes transparentes superior e inferior del PNG. */
        .logo-frame {
            position: relative;
            display: block;
            width: min(100%, 340px);
            aspect-ratio: 3.6;
            overflow: hidden;
        }

        .logo-frame img {
            position: absolute;
            top: 50%;
            left: 0;
            display: block;
            width: 100%;
            height: auto;
            transform: translateY(-49%);
        }

        .brand-line {
            height: 12px;
            margin-block: -6px;
            background: var(--gold);
            clip-path: polygon(0 40%, 12% 40%, 12% 70%, 16% 0, 16% 40%,
                    100% 40%, 100% 62%, 15.5% 62%, 15.5% 30%, 11.5% 100%, 11.5% 62%, 0 62%);
        }

        .login-body { padding-block: 1.65rem 2rem; }

        h1 {
            margin: 0 0 1.4rem;
            font-size: clamp(1.7rem, 4.7vw, 2.55rem);
            font-weight: 750;
            line-height: 1.12;
        }

        .field + .field { margin-top: 1rem; }

        .field label {
            display: block;
            margin-bottom: .45rem;
            font-size: 1rem;
            font-weight: 650;
        }

        .field input {
            display: block;
            width: 100%;
            min-width: 0;
            min-height: 50px;
            padding: .65rem .8rem;
            border: 1px solid var(--border);
            border-radius: 8px;
            background: #fff;
            color: var(--ink);
            font: inherit;
        }

        .password-field {
            display: flex;
            align-items: stretch;
            border: 1px solid var(--border);
            border-radius: 8px;
            background: #fff;
        }

        .password-field input {
            border: 0;
            background: transparent;
        }

        .password-toggle {
            display: inline-flex;
            align-items: center;
            gap: .5rem;
            min-width: 0;
            margin: .2rem;
            padding: .4rem .65rem;
            border: 0;
            border-radius: 6px;
            background: transparent;
            color: var(--ink);
            font: inherit;
            font-weight: 650;
            cursor: pointer;
        }

        .password-toggle[hidden] { display: none; }
        .password-toggle:hover { background: #edf2f7; }

        .eye {
            display: grid;
            place-items: center;
            width: .9rem;
            height: .9rem;
            border: 1.6px solid currentColor;
            border-radius: 70% 0;
            transform: rotate(45deg);
        }

        .eye::after {
            width: .28rem;
            height: .28rem;
            border-radius: 50%;
            background: currentColor;
            content: "";
        }

        input:focus-visible, button:focus-visible {
            outline: 2px solid #275b93;
            outline-offset: 2px;
        }

        .password-field:focus-within { border-color: #275b93; }
        .password-field input:focus-visible { outline-offset: -3px; }

        .login-submit {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: .65rem;
            width: 100%;
            min-height: 52px;
            margin-top: 1.3rem;
            padding: .7rem 1rem;
            border: 0;
            border-radius: 8px;
            background: var(--navy);
            color: #fff;
            font: inherit;
            font-size: 1.05rem;
            font-weight: 750;
            cursor: pointer;
        }

        .login-submit:hover { background: var(--navy-hover); }
        .login-submit span { color: var(--gold); font-size: 1.25em; }

        .login-error {
            margin: 0 0 1rem;
            padding: .75rem .85rem;
            border: 1px solid #efb5b5;
            border-radius: 8px;
            background: #fff1f1;
            color: #85232a;
            overflow-wrap: anywhere;
        }

        @media (max-width: 400px) {
            .login-page { padding: .85rem; }
            .login-brand { min-height: 100px; }
            .login-body { padding-block: 1.4rem; }
            .password-toggle { padding-inline: .45rem; }
        }
    </style>
</head>
<body>
    <main class="login-page">
        <section class="login-card" aria-labelledby="tituloLogin">
            <header class="login-brand">
                <div class="logo-frame">
                    <img src="<c:out value='${urlLogo}'/>"
                         alt="La Esquina — Sistema de Gestión de Inventario"
                         width="1536" height="1024">
                </div>
            </header>
            <div class="brand-line" aria-hidden="true"></div>

            <div class="login-body">
                <h1 id="tituloLogin">Iniciar sesión</h1>

                <c:if test="${not empty mensajeError}">
                    <p class="login-error" role="alert"><c:out value="${mensajeError}"/></p>
                </c:if>

                <form action="<c:out value='${urlLogin}'/>" method="POST">
                    <div class="field">
                        <label for="txtUsuario">Nombre de usuario</label>
                        <input id="txtUsuario" name="txtUsuario" type="text"
                               autocomplete="username" autocapitalize="none" spellcheck="false" required>
                    </div>

                    <div class="field">
                        <label for="txtPassword">Contraseña</label>
                        <div class="password-field">
                            <input id="txtPassword" name="txtPassword" type="password"
                                   autocomplete="current-password" required>
                            <button class="password-toggle" id="alternarPassword" type="button"
                                    aria-controls="txtPassword" aria-label="Mostrar contraseña" hidden>
                                <span class="eye" aria-hidden="true"></span>
                                <span id="textoVisibilidad">Mostrar</span>
                            </button>
                        </div>
                    </div>

                    <button class="login-submit" type="submit">
                        Ingresar <span aria-hidden="true">→</span>
                    </button>
                </form>
            </div>
        </section>
    </main>

    <script>
        const password = document.getElementById('txtPassword');
        const boton = document.getElementById('alternarPassword');
        const texto = document.getElementById('textoVisibilidad');

        boton.hidden = false;
        boton.addEventListener('click', () => {
            const mostrar = password.type === 'password';
            password.type = mostrar ? 'text' : 'password';
            texto.textContent = mostrar ? 'Ocultar' : 'Mostrar';
            boton.setAttribute('aria-label', mostrar ? 'Ocultar contraseña' : 'Mostrar contraseña');
        });
    </script>
</body>
</html>

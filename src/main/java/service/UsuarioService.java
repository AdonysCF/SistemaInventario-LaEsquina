
package service;

import dao.UsuarioDAO;
import modelo.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class UsuarioService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // REGISTRAR NUEVO USUARIO
    public boolean registrarNuevoUsuario(
            String username,
            String passwordPlano,
            String nombre,
            String rol) {

        username = validarUsername(username);
        nombre = validarNombre(nombre);
        passwordPlano = validarPassword(passwordPlano);
        rol = validarRol(rol);

        if (usuarioDAO.existeUsername(username)) {
            throw new IllegalArgumentException(
                    "El nombre de usuario ya se encuentra registrado.");
        }

        String hashGenerado =
                BCrypt.hashpw(passwordPlano, BCrypt.gensalt(12));

        Usuario nuevoUsuario = new Usuario();

        nuevoUsuario.setUsername(username);
        nuevoUsuario.setPasswordHash(hashGenerado);
        nuevoUsuario.setNombreCompleto(nombre);
        nuevoUsuario.setRol(rol);

        try {

            return usuarioDAO.registrarUsuario(nuevoUsuario);

        } catch (ExcepcionPersistencia ex) {

            if (esUsernameDuplicado(ex)) {
                throw new IllegalArgumentException(
                        "El nombre de usuario ya se encuentra registrado.");
            }

            throw ex;
        }
    }

    // LISTAR USUARIOS
    public List<Usuario> listarTodos() {
        return usuarioDAO.listarTodos();
    }

    // BUSCAR USUARIO
    public Usuario buscarPorId(int idUsuario) {

        if (idUsuario <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del usuario no es valido.");
        }

        return usuarioDAO.buscarPorId(idUsuario);
    }

    // ACTIVAR O DESACTIVAR USUARIO
    public boolean cambiarEstado(
            int idUsuario,
            boolean activar,
            int idUsuarioSesion) {

        if (idUsuario <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del usuario no es valido.");
        }

        Usuario usuario = usuarioDAO.buscarPorId(idUsuario);

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "El usuario seleccionado no existe.");
        }

        if (usuario.isActivo() == activar) {
            throw new IllegalArgumentException(
                    activar
                    ? "La cuenta ya se encuentra activa."
                    : "La cuenta ya se encuentra desactivada.");
        }

        // No permitir desactivar la propia cuenta.
        if (!activar && idUsuario == idUsuarioSesion) {
            throw new IllegalArgumentException(
                    "No puedes desactivar tu propia cuenta.");
        }

        // Evitar que el sistema se quede sin gerentes activos.
        if (!activar && "GERENTE".equals(usuario.getRol())) {

            int gerentesActivos =
                    usuarioDAO.contarGerentesActivos();

            if (gerentesActivos <= 1) {
                throw new IllegalArgumentException(
                        "No puedes desactivar al ultimo gerente activo.");
            }
        }

        return usuarioDAO.cambiarEstado(idUsuario, activar);
    }

    // VALIDAR INICIO DE SESION
    public Usuario validarLogin(
            String username,
            String passwordPlano) {

        if (username == null || passwordPlano == null) {
            return null;
        }

        Usuario usuario = usuarioDAO.buscarPorUsername(
                username.trim());

        if (usuario != null
                && BCrypt.checkpw(
                        passwordPlano,
                        usuario.getPasswordHash())) {

            return usuario;
        }

        return null;
    }

    // VALIDACIONES PRIVADAS

    private String validarUsername(String username) {

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre de usuario es obligatorio.");
        }

        username = username.trim();

        if (username.length() < 4 || username.length() > 50) {
            throw new IllegalArgumentException(
                    "El nombre de usuario debe tener entre 4 y 50 caracteres.");
        }

        if (!username.matches("[a-zA-Z0-9._-]+")) {
            throw new IllegalArgumentException(
                    "El usuario solo puede contener letras, numeros, puntos, "
                    + "guiones y guiones bajos.");
        }

        return username;
    }

    private String validarNombre(String nombre) {

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre completo es obligatorio.");
        }

        nombre = nombre.trim().replaceAll("\\s+", " ");

        if (nombre.length() > 120) {
            throw new IllegalArgumentException(
                    "El nombre completo no puede superar los 120 caracteres.");
        }

        return nombre;
    }

    private String validarPassword(String password) {

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 8 caracteres.");
        }

        if (password.length() > 72) {
            throw new IllegalArgumentException(
                    "La contraseña no puede superar los 72 caracteres.");
        }

        return password;
    }

    private String validarRol(String rol) {

        if (rol == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un rol.");
        }

        rol = rol.trim().toUpperCase();

        if (!"CAJERO".equals(rol) && !"GERENTE".equals(rol)) {
            throw new IllegalArgumentException(
                    "El rol seleccionado no es valido.");
        }

        return rol;
    }

    // DETECTAR DUPLICADOS EN SQL SERVER
    private boolean esUsernameDuplicado(Throwable ex) {

        Throwable actual = ex;

        while (actual != null) {

            if (actual instanceof java.sql.SQLException) {

                java.sql.SQLException sqlEx =
                        (java.sql.SQLException) actual;

                if (sqlEx.getErrorCode() == 2627
                        || sqlEx.getErrorCode() == 2601) {
                    return true;
                }
            }

            actual = actual.getCause();
        }

        return false;
    }
}

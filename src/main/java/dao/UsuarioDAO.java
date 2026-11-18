
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import modelo.Usuario;
import service.ExcepcionPersistencia;

public class UsuarioDAO {

    private static final Logger LOGGER =
            Logger.getLogger(UsuarioDAO.class.getName());

    // BUSCAR USUARIO PARA EL INICIO DE SESION
    public Usuario buscarPorUsername(String username) {

        String sql = "SELECT id_usuario, username, password_hash, "
                + "nombre_completo, rol, activo, fecha_creacion "
                + "FROM usuario "
                + "WHERE username = ? AND activo = 1";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapearUsuario(rs, true);
                }
            }

            return null;

        } catch (SQLException ex) {

            LOGGER.log(Level.SEVERE,
                    "Error al buscar el usuario.", ex);

            throw new ExcepcionPersistencia(
                    "No se pudo consultar el usuario.", ex);
        }
    }

    // REGISTRAR NUEVO USUARIO
    public boolean registrarUsuario(Usuario usr) {

        String sql = "INSERT INTO usuario "
                + "(username, password_hash, nombre_completo, rol, activo) "
                + "VALUES (?, ?, ?, ?, 1)";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usr.getUsername());
            ps.setString(2, usr.getPasswordHash());
            ps.setString(3, usr.getNombreCompleto());
            ps.setString(4, usr.getRol());

            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {

            LOGGER.log(Level.SEVERE,
                    "Error al registrar el usuario.", ex);

            throw new ExcepcionPersistencia(
                    "No se pudo registrar el usuario.", ex);
        }
    }

    // LISTAR TODOS LOS USUARIOS
    // Nunca se recuperan los hashes de las contraseñas para la vista.
    public List<Usuario> listarTodos() {

        List<Usuario> lista = new ArrayList<>();

        String sql = "SELECT id_usuario, username, nombre_completo, "
                + "rol, activo, fecha_creacion "
                + "FROM usuario "
                + "ORDER BY nombre_completo ASC";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearUsuario(rs, false));
            }

            return lista;

        } catch (SQLException ex) {

            LOGGER.log(Level.SEVERE,
                    "Error al listar los usuarios.", ex);

            throw new ExcepcionPersistencia(
                    "No se pudieron consultar los usuarios.", ex);
        }
    }

    // VERIFICAR SI EXISTE UN NOMBRE DE USUARIO
    public boolean existeUsername(String username) {

        String sql = "SELECT COUNT(*) "
                + "FROM usuario WHERE username = ?";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

            return false;

        } catch (SQLException ex) {

            LOGGER.log(Level.SEVERE,
                    "Error al verificar el nombre de usuario.", ex);

            throw new ExcepcionPersistencia(
                    "No se pudo verificar el nombre de usuario.", ex);
        }
    }

    // CONTAR GERENTES ACTIVOS
    public int contarGerentesActivos() {

        String sql = "SELECT COUNT(*) "
                + "FROM usuario "
                + "WHERE rol = 'GERENTE' AND activo = 1";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;

        } catch (SQLException ex) {

            LOGGER.log(Level.SEVERE,
                    "Error al contar los gerentes activos.", ex);

            throw new ExcepcionPersistencia(
                    "No se pudo verificar la cantidad de gerentes.", ex);
        }
    }

    // BUSCAR USUARIO POR ID
    public Usuario buscarPorId(int idUsuario) {

        String sql = "SELECT id_usuario, username, nombre_completo, "
                + "rol, activo, fecha_creacion "
                + "FROM usuario WHERE id_usuario = ?";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapearUsuario(rs, false);
                }
            }

            return null;

        } catch (SQLException ex) {

            LOGGER.log(Level.SEVERE,
                    "Error al buscar usuario por identificador.", ex);

            throw new ExcepcionPersistencia(
                    "No se pudo consultar el usuario.", ex);
        }
    }

    // CAMBIAR ESTADO DE UNA CUENTA
    public boolean cambiarEstado(int idUsuario, boolean activo) {

        String sql = "UPDATE usuario "
                + "SET activo = ? "
                + "WHERE id_usuario = ?";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, activo);
            ps.setInt(2, idUsuario);

            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {

            LOGGER.log(Level.SEVERE,
                    "Error al cambiar el estado del usuario.", ex);

            throw new ExcepcionPersistencia(
                    "No se pudo cambiar el estado del usuario.", ex);
        }
    }

    // CONVERTIR RESULTADO SQL EN OBJETO USUARIO
    private Usuario mapearUsuario(ResultSet rs, boolean incluirPassword)
            throws SQLException {

        Usuario usuario = new Usuario();

        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setUsername(rs.getString("username"));
        usuario.setNombreCompleto(rs.getString("nombre_completo"));
        usuario.setRol(rs.getString("rol"));
        usuario.setActivo(rs.getBoolean("activo"));
        usuario.setFechaCreacion(rs.getTimestamp("fecha_creacion"));

        if (incluirPassword) {
            usuario.setPasswordHash(rs.getString("password_hash"));
        }

        return usuario;
    }
}

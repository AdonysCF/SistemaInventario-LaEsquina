
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.Usuario;
import service.ExcepcionPersistencia;

public class UsuarioDAO {

    private static final Logger LOGGER =
            Logger.getLogger(UsuarioDAO.class.getName());

    public Usuario buscarPorUsername(String username) {

        String sql = "SELECT id_usuario, username, password_hash, "
                + "nombre_completo, rol, activo "
                + "FROM usuario WHERE username = ? AND activo = 1";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    Usuario usuario = new Usuario();

                    usuario.setIdUsuario(rs.getInt("id_usuario"));
                    usuario.setUsername(rs.getString("username"));
                    usuario.setPasswordHash(rs.getString("password_hash"));
                    usuario.setNombreCompleto(rs.getString("nombre_completo"));
                    usuario.setRol(rs.getString("rol"));
                    usuario.setActivo(rs.getBoolean("activo"));

                    return usuario;
                }
            }

            return null;

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE,
                    "Error al buscar el usuario: " + username, ex);
            throw new ExcepcionPersistencia(
                    "No se pudo consultar el usuario.", ex);
        }
    }

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
                    "Error al registrar el usuario: "
                    + usr.getUsername(), ex);
            throw new ExcepcionPersistencia(
                    "No se pudo registrar el usuario.", ex);
        }
    }
}

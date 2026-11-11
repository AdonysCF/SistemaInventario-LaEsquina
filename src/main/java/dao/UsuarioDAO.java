package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import modelo.Usuario;

public class UsuarioDAO {

    // 1. Buscar usuario para el Login
    public Usuario buscarPorUsername(String username) {
        Usuario usuario = null;
        String sql = "SELECT id_usuario, username, password_hash, nombre_completo, rol, activo " +
                     "FROM usuario WHERE username = ? AND activo = 1";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setString(1, username);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuario = new Usuario();
                    // Ajusta los setters según los nombres que usaste en tu clase Usuario
                    usuario.setIdUsuario(rs.getInt("id_usuario"));
                    usuario.setUsername(rs.getString("username"));
                    usuario.setPasswordHash(rs.getString("password_hash"));
                    usuario.setNombreCompleto(rs.getString("nombre_completo"));
                    usuario.setRol(rs.getString("rol"));
                    usuario.setActivo(rs.getBoolean("activo"));
                }
            }
        } catch (Exception e) {
            System.out.println("Error en buscarPorUsername: " + e.getMessage());
        }
        return usuario;
    }

    // 2. Registrar un nuevo usuario
    public boolean registrarUsuario(Usuario usr) {
        String sql = "INSERT INTO usuario (username, password_hash, nombre_completo, rol, activo) " +
                     "VALUES (?, ?, ?, ?, 1)";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setString(1, usr.getUsername());
            ps.setString(2, usr.getPasswordHash()); // Aquí llega el hash, NO la clave plana
            ps.setString(3, usr.getNombreCompleto());
            ps.setString(4, usr.getRol());
            
            int filas = ps.executeUpdate();
            return filas > 0;
        } catch (Exception e) {
            System.out.println("Error en registrarUsuario: " + e.getMessage());
            return false;
        }
    }
}
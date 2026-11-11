package service;

import dao.UsuarioDAO;
import modelo.Usuario;
import org.mindrot.jbcrypt.BCrypt;

public class UsuarioService {
    
    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    // LÓGICA DE REGISTRO
    public boolean registrarNuevoUsuario(String username, String passwordPlano, String nombre, String rol) {
        
        // Aquí se encripta la contraseña antes de mandarla a la BD
        String hashGenerado = BCrypt.hashpw(passwordPlano, BCrypt.gensalt(10));
        
        // Armamos el objeto Usuario con el Hash
        Usuario nuevoUsr = new Usuario();
        nuevoUsr.setUsername(username);
        nuevoUsr.setPasswordHash(hashGenerado);
        nuevoUsr.setNombreCompleto(nombre);
        nuevoUsr.setRol(rol);
        
        // Enviamos al DAO
        return usuarioDAO.registrarUsuario(nuevoUsr);
    }

    // LÓGICA DE LOGIN
    public Usuario validarLogin(String username, String passwordPlano) {
        // Buscamos al usuario en la BD
        Usuario usuario = usuarioDAO.buscarPorUsername(username);
        
        // Si existe, verificamos el hash
        if (usuario != null) {
            // BCrypt.checkpw compara la clave que el usuario escribió 
            if (BCrypt.checkpw(passwordPlano, usuario.getPasswordHash())) {
                return usuario; 
            }
        }
        return null; // Usuario no existe o clave incorrecta
    }
}
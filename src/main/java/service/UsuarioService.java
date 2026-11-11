package service;

import dao.UsuarioDAO;
import modelo.Usuario;
import org.mindrot.jbcrypt.BCrypt;

public class UsuarioService {
    
    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    // LÓGICA DE REGISTRO
    public boolean registrarNuevoUsuario(String username, String passwordPlano, String nombre, String rol) {
        
        // 1. Aquí "se encripta sola" la contraseña antes de mandarla a la BD
        String hashGenerado = BCrypt.hashpw(passwordPlano, BCrypt.gensalt(10));
        
        // 2. Armamos el objeto Usuario con el Hash, no con la clave plana
        Usuario nuevoUsr = new Usuario();
        nuevoUsr.setUsername(username);
        nuevoUsr.setPasswordHash(hashGenerado);
        nuevoUsr.setNombreCompleto(nombre);
        nuevoUsr.setRol(rol);
        
        // 3. Enviamos al DAO
        return usuarioDAO.registrarUsuario(nuevoUsr);
    }

    // LÓGICA DE LOGIN
    public Usuario validarLogin(String username, String passwordPlano) {
        // 1. Buscamos al usuario en la BD
        Usuario usuario = usuarioDAO.buscarPorUsername(username);
        
        // 2. Si existe, verificamos el hash
        if (usuario != null) {
            // BCrypt.checkpw compara la clave que el usuario escribió (ej: "123456") 
            // con el garabato guardado en la BD (ej: "$2a$10$hashSeguro...")
            if (BCrypt.checkpw(passwordPlano, usuario.getPasswordHash())) {
                return usuario; // ¡Login Exitoso!
            }
        }
        return null; // Usuario no existe o clave incorrecta
    }
}
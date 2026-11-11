package service;

import dao.UsuarioDAO;
import modelo.Usuario;
import org.mindrot.jbcrypt.BCrypt;

public class GeneradorUsuarios {
    public static void main(String[] args) {
        UsuarioDAO dao = new UsuarioDAO();

        // 1. Crear el Gerente
        Usuario gerente = new Usuario();
        gerente.setUsername("carlos.mendoza"); // Usuario para login
        // La contraseña "admin123" se encripta aquí mismo
        String hashGerente = BCrypt.hashpw("admin123", BCrypt.gensalt(10));
        gerente.setPasswordHash(hashGerente);
        gerente.setNombreCompleto("Carlos Mendoza");
        gerente.setRol("GERENTE");
        
        boolean ok1 = dao.registrarUsuario(gerente);
        System.out.println("Gerente creado: " + ok1);

        // 2. Crear el Cajero
        Usuario cajero = new Usuario();
        cajero.setUsername("lucia.paredes"); // Usuario para login
        // La contraseña "caja123" se encripta aquí mismo
        String hashCajero = BCrypt.hashpw("caja123", BCrypt.gensalt(10));
        cajero.setPasswordHash(hashCajero);
        cajero.setNombreCompleto("Lucía Paredes");
        cajero.setRol("CAJERO");
        
        boolean ok2 = dao.registrarUsuario(cajero);
        System.out.println("Cajero creado: " + ok2);
    }
}
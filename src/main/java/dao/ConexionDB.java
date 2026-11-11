package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    
    // Conectamos directamente al puerto 1433 local
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=bd_la_esquina;encrypt=true;trustServerCertificate=true;";
    
    // Credenciales (las mismas que usas en Management Studio)
    private static final String USER = "sa"; 
    private static final String PASSWORD = "Chavodelmocho123";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("¡Conexión a SQL Server exitosa!");
        } catch (ClassNotFoundException e) {
            System.out.println("Error: No se encontró el driver JDBC. " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error de conexión a la base de datos: " + e.getMessage());
        }
        return conn;
    }
}

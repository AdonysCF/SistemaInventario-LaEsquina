package dao; // O 'config' o el paquete donde lo tengas

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConexionDB {

    // Única instancia del DataSource
    private static final HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();

            // 1. URL de Conexión, Usuario y Contraseña
            // Ajusta "bd_la_esquina", "tu_usuario" y "tu_contraseña"
            config.setJdbcUrl("jdbc:sqlserver://localhost:1433;databaseName=bd_la_esquina;encrypt=true;trustServerCertificate=true;");
            config.setUsername("sa"); 
            config.setPassword("Chavodelmocho123"); 
            
            // Driver de SQL Server
            config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // 2. Configuración de Rendimiento (Cumple AC-01)
            config.setMaximumPoolSize(20);      // Límite máximo de conexiones a BD
            config.setMinimumIdle(5);           // Conexiones en espera
            config.setIdleTimeout(30000);       // Tiempo antes de cerrar conexión inactiva
            config.setConnectionTimeout(10000); // Tiempo máximo de espera (10s)
            config.setPoolName("LaEsquinaHikariPool");

            dataSource = new HikariDataSource(config);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error fatal al inicializar HikariCP", e);
        }
    }

    /**
     * Entrega una conexión activa del pool.
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    /**
     * Devuelve la conexión al pool de HikariCP.
     */
    public static void cerrar(Connection conn) {
        if (conn != null) {
            try {
                // En un pool de conexiones, .close() NO cierra la conexión física, 
                // simplemente la devuelve al pool para que otro usuario pueda usarla.
                conn.close(); 
            } catch (SQLException ex) {
                System.err.println("Error al devolver la conexión al pool: " + ex.getMessage());
            }
        }
    }
}
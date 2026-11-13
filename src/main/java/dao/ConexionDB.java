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

            // URL de Conexión, Usuario y Contraseña
            config.setJdbcUrl("jdbc:sqlserver://localhost:1433;databaseName=bd_la_esquina;encrypt=true;trustServerCertificate=true;");
            config.setUsername("sa"); 
            config.setPassword("Chavodelmocho123"); 
            
            config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            config.setMaximumPoolSize(20);     
            config.setMinimumIdle(5);          
            config.setIdleTimeout(30000);       
            config.setConnectionTimeout(10000); 
            config.setPoolName("LaEsquinaHikariPool");

            dataSource = new HikariDataSource(config);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error fatal al inicializar HikariCP", e);
        }
    }

    
     //Entrega una conexión activa del pool.
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
 
     // Devuelve la conexión al pool de HikariCP.
    public static void cerrar(Connection conn) {
        if (conn != null) {
            try {
                conn.close(); 
            } catch (SQLException ex) {
                System.err.println("Error al devolver la conexión al pool: " + ex.getMessage());
            }
        }
    }
}
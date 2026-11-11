package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import modelo.DetalleVenta;
import modelo.Venta;

public class VentaDAO {

    // (Aquí puedes mantener tus otros métodos si los tienes)

    /**
     * Inserta la cabecera de la venta usando una conexión transaccional existente.
     * Devuelve el ID generado.
     */
    public int insertarCabeceraTransaccional(Connection conn, Venta venta) throws SQLException {
        int idGenerado = 0;
        String sql = "INSERT INTO venta (numero_ticket, total_venta, id_usuario, estado) VALUES (?, ?, ?, 'CONFIRMADA')";
        
        // Usamos Statement.RETURN_GENERATED_KEYS para recuperar el id_venta creado
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, venta.getNumeroTicket());
            ps.setBigDecimal(2, venta.getTotalVenta());
            ps.setInt(3, venta.getIdUsuario());
            
            ps.executeUpdate();
            
            // Recuperar el ID generado
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    idGenerado = rs.getInt(1);
                }
            }
        }
        return idGenerado;
    }

    /**
     * Inserta un detalle de venta usando una conexión transaccional existente.
     */
    public boolean insertarDetalleTransaccional(Connection conn, DetalleVenta detalle) throws SQLException {
        String sql = "INSERT INTO detalle_venta (id_venta, id_producto, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, detalle.getIdVenta());
            ps.setInt(2, detalle.getIdProducto());
            ps.setInt(3, detalle.getCantidad());
            ps.setBigDecimal(4, detalle.getPrecioUnitario());
            ps.setBigDecimal(5, detalle.getSubtotal());
            
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        }
    }
}
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import modelo.DetalleVenta;
import modelo.Venta;

public class VentaDAO {

    public int insertarCabecera(Connection conn, Venta venta) throws Exception {
        String sql = "INSERT INTO venta (numero_ticket, total_venta, id_usuario) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, venta.getNumeroTicket());
            ps.setBigDecimal(2, venta.getTotalVenta());
            ps.setInt(3, venta.getIdUsuario());
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new Exception("Error al generar ID de Venta");
    }

    public void insertarDetalle(Connection conn, int idVenta, DetalleVenta det) throws Exception {
        String sql = "INSERT INTO detalle_venta (id_venta, id_producto, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            ps.setInt(2, det.getIdProducto());
            ps.setInt(3, det.getCantidad());
            ps.setBigDecimal(4, det.getPrecioUnitario());
            ps.setBigDecimal(5, det.getSubtotal());
            ps.executeUpdate();
        }
    }
}
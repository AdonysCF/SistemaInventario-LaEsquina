package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import modelo.DetalleMovimiento;
import modelo.MovimientoInventario;

public class MovimientoDAO {



    //Inserta la cabecera del movimiento en el Kardex y devuelve el ID generado.

    public int insertarMovimientoTransaccional(Connection conn, MovimientoInventario mov) throws SQLException {
        int idGenerado = 0;
        String sql = "INSERT INTO movimiento_inventario (codigo_movimiento, tipo_movimiento, documento_referencia, observacion, id_usuario, id_venta_origen) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, mov.getCodigoMovimiento());
            ps.setString(2, mov.getTipoMovimiento());
            ps.setString(3, mov.getDocumentoReferencia());
            ps.setString(4, mov.getObservacion());
            ps.setInt(5, mov.getIdUsuario());
            
            // id_venta_origen puede ser nulo si es una entrada de mercadería
            if (mov.getIdVentaOrigen() > 0) {
                ps.setInt(6, mov.getIdVentaOrigen());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    idGenerado = rs.getInt(1);
                }
            }
        }
        return idGenerado;
    }

    /**
     * Inserta el detalle del movimiento en el Kardex mostrando el rastro del stock.
     */
    public boolean insertarDetalleMovTransaccional(Connection conn, DetalleMovimiento det) throws SQLException {
        String sql = "INSERT INTO detalle_movimiento (id_movimiento, id_producto, cantidad, stock_anterior, stock_resultante) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, det.getIdMovimiento());
            ps.setInt(2, det.getIdProducto());
            ps.setInt(3, det.getCantidad());
            ps.setInt(4, det.getStockAnterior());
            ps.setInt(5, det.getStockResultante());
            
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        }
    }
}
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import modelo.DetalleMovimiento;
import modelo.MovimientoInventario;

public class MovimientoDAO {

    // Inserta la cabecera y devuelve el ID autogenerado
    public int insertarMovimiento(Connection conn, MovimientoInventario mov) throws Exception {
        String sql = "INSERT INTO movimiento_inventario (codigo_movimiento, tipo_movimiento, documento_referencia, observacion, id_usuario) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, mov.getCodigoMovimiento());
            ps.setString(2, mov.getTipoMovimiento());
            ps.setString(3, mov.getDocumentoReferencia());
            ps.setString(4, mov.getObservacion());
            ps.setInt(5, mov.getIdUsuario());
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // Retorna el ID del movimiento insertado
                }
            }
        }
        throw new Exception("No se pudo generar el ID del movimiento.");
    }

    // Inserta una línea de detalle del movimiento (Kardex)
    public void insertarDetalleMov(Connection conn, int idMovimiento, DetalleMovimiento det) throws Exception {
        String sql = "INSERT INTO detalle_movimiento (id_movimiento, id_producto, cantidad, stock_anterior, stock_resultante) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMovimiento);
            ps.setInt(2, det.getIdProducto());
            ps.setInt(3, det.getCantidad());
            ps.setInt(4, det.getStockAnterior());
            ps.setInt(5, det.getStockResultante());
            ps.executeUpdate();
        }
    }
}
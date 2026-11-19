package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import modelo.DetalleMovimiento;
import modelo.KardexRegistro;
import modelo.MovimientoInventario;
import service.ExcepcionPersistencia;

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

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public List<KardexRegistro> consultarKardex(LocalDate desde, LocalDate hastaExclusiva,
            String sku, String tipo, String usuario, long desplazamiento, int limite) {

        String sql = """
                SELECT m.fecha_hora, m.codigo_movimiento, m.tipo_movimiento,
                       p.codigo_sku, p.nombre AS producto, d.cantidad,
                       d.stock_anterior, d.stock_resultante, u.nombre_completo,
                       u.username, m.documento_referencia, m.observacion
                FROM detalle_movimiento d
                JOIN movimiento_inventario m ON m.id_movimiento = d.id_movimiento
                JOIN producto p ON p.id_producto = d.id_producto
                JOIN usuario u ON u.id_usuario = m.id_usuario
                WHERE 1 = 1
                """;

        List<Object> parametros = new ArrayList<>();
        if (desde != null) {
            sql += " AND m.fecha_hora >= ?";
            parametros.add(Timestamp.valueOf(desde.atStartOfDay()));
        }
        if (hastaExclusiva != null) {
            sql += " AND m.fecha_hora < ?";
            parametros.add(Timestamp.valueOf(hastaExclusiva.atStartOfDay()));
        }
        if (!sku.isEmpty()) {
            sql += " AND p.codigo_sku = ?";
            parametros.add(sku);
        }
        if ("AJUSTE".equals(tipo)) {
            sql += " AND m.tipo_movimiento IN ('AJUSTE_POSITIVO', 'AJUSTE_NEGATIVO')";
        } else if (!tipo.isEmpty()) {
            sql += " AND m.tipo_movimiento = ?";
            parametros.add(tipo);
        }
        if (!usuario.isEmpty()) {
            sql += " AND u.username = ?";
            parametros.add(usuario);
        }
        sql += " ORDER BY m.fecha_hora DESC, m.id_movimiento DESC, d.id_detalle_mov DESC"
                + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        parametros.add(desplazamiento);
        parametros.add(limite);

        List<KardexRegistro> filas = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new KardexRegistro(
                            rs.getTimestamp("fecha_hora").toLocalDateTime().format(FORMATO_FECHA),
                            rs.getString("codigo_movimiento"),
                            rs.getString("tipo_movimiento"),
                            rs.getString("codigo_sku"),
                            rs.getString("producto"),
                            rs.getInt("cantidad"),
                            rs.getInt("stock_anterior"),
                            rs.getInt("stock_resultante"),
                            rs.getString("nombre_completo"),
                            rs.getString("username"),
                            rs.getString("documento_referencia"),
                            rs.getString("observacion")));
                }
            }
        } catch (SQLException ex) {
            throw new ExcepcionPersistencia("No se pudo consultar el Kardex.", ex);
        }
        return filas;
    }
}

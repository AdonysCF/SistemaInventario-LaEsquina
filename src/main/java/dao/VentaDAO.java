package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import modelo.DetalleVenta;
import modelo.ReporteVentaFila;
import modelo.Venta;
import modelo.VentaDetalleRegistro;
import modelo.VentaResumen;
import service.ExcepcionPersistencia;

public class VentaDAO {


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

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public List<VentaResumen> listarHistorial(LocalDateTime desde, LocalDateTime hastaExclusiva,
            String ticket, String responsable, long desplazamiento, int limite) {
        String sql = """
                SELECT v.id_venta, v.numero_ticket, v.fecha_hora, v.estado,
                       v.total_venta, u.username, u.nombre_completo
                FROM venta v
                JOIN usuario u ON u.id_usuario = v.id_usuario
                WHERE 1 = 1
                """;
        List<Object> filtros = new ArrayList<>();
        if (desde != null) {
            sql += " AND v.fecha_hora >= ?";
            filtros.add(Timestamp.valueOf(desde));
        }
        if (hastaExclusiva != null) {
            sql += " AND v.fecha_hora < ?";
            filtros.add(Timestamp.valueOf(hastaExclusiva));
        }
        if (!ticket.isEmpty()) {
            sql += " AND v.numero_ticket = ?";
            filtros.add(ticket);
        }
        if (!responsable.isEmpty()) {
            sql += " AND u.username = ?";
            filtros.add(responsable);
        }
        sql += " ORDER BY v.fecha_hora DESC, v.id_venta DESC"
                + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<VentaResumen> ventas = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < filtros.size(); i++) {
                ps.setObject(i + 1, filtros.get(i));
            }
            ps.setLong(filtros.size() + 1, desplazamiento);
            ps.setInt(filtros.size() + 2, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ventas.add(leerResumen(rs));
                }
            }
        } catch (SQLException ex) {
            throw new ExcepcionPersistencia("No se pudo consultar el historial de ventas.", ex);
        }
        return ventas;
    }

    public Optional<DetalleConsultado> consultarDetalle(int idVenta) {
        String sql = """
                SELECT v.id_venta, v.numero_ticket, v.fecha_hora, v.estado,
                       v.total_venta, u.username, u.nombre_completo,
                       d.id_detalle_venta, p.codigo_sku, p.nombre AS producto,
                       d.cantidad, d.precio_unitario, d.subtotal
                FROM venta v
                JOIN usuario u ON u.id_usuario = v.id_usuario
                LEFT JOIN detalle_venta d ON d.id_venta = v.id_venta
                LEFT JOIN producto p ON p.id_producto = d.id_producto
                WHERE v.id_venta = ?
                ORDER BY d.id_detalle_venta
                """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                VentaResumen venta = leerResumen(rs);
                List<VentaDetalleRegistro> productos = new ArrayList<>();
                do {
                    if (rs.getObject("id_detalle_venta") != null) {
                        productos.add(new VentaDetalleRegistro(
                                rs.getString("codigo_sku"), rs.getString("producto"),
                                rs.getInt("cantidad"), rs.getBigDecimal("precio_unitario"),
                                rs.getBigDecimal("subtotal")));
                    }
                } while (rs.next());
                return Optional.of(new DetalleConsultado(venta, List.copyOf(productos)));
            }
        } catch (SQLException ex) {
            throw new ExcepcionPersistencia("No se pudo consultar el detalle de la venta.", ex);
        }
    }

    public Optional<VentaParaAnular> bloquearVentaParaAnular(Connection conn, int idVenta)
            throws SQLException {
        String sql = """
                SELECT numero_ticket, estado
                FROM venta WITH (UPDLOCK, ROWLOCK)
                WHERE id_venta = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new VentaParaAnular(
                            rs.getString("numero_ticket"), rs.getString("estado")));
                }
                return Optional.empty();
            }
        }
    }

    public List<CantidadVendida> listarCantidadesVendidas(Connection conn, int idVenta)
            throws SQLException {
        String sql = """
                SELECT id_producto, SUM(CAST(cantidad AS BIGINT)) AS cantidad
                FROM detalle_venta
                WHERE id_venta = ?
                GROUP BY id_producto
                ORDER BY id_producto
                """;
        List<CantidadVendida> cantidades = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cantidades.add(new CantidadVendida(rs.getInt("id_producto"), rs.getLong("cantidad")));
                }
            }
        }
        return cantidades;
    }

    public boolean marcarAnuladaTransaccional(Connection conn, int idVenta) throws SQLException {
        String sql = "UPDATE venta SET estado = 'ANULADA' WHERE id_venta = ? AND estado = 'CONFIRMADA'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            return ps.executeUpdate() == 1;
        }
    }

    public record VentaParaAnular(String ticket, String estado) {
    }

    public record CantidadVendida(int idProducto, long cantidad) {
    }

    public ReporteConsolidado consultarReporteVentas(LocalDateTime desde, LocalDateTime hastaExclusiva) {
        String sql = """
                WITH lineas AS (
                    SELECT v.id_venta, p.id_producto, p.codigo_sku AS sku,
                           p.nombre AS producto, c.id_categoria, c.nombre AS categoria,
                           CAST(d.cantidad AS BIGINT) AS unidades, d.subtotal
                    FROM venta v
                    JOIN detalle_venta d ON d.id_venta = v.id_venta
                    JOIN producto p ON p.id_producto = d.id_producto
                    JOIN categoria c ON c.id_categoria = p.id_categoria
                    WHERE v.estado = 'CONFIRMADA'
                      AND v.fecha_hora >= ?
                """;
        if (hastaExclusiva != null) {
            sql += " AND v.fecha_hora < ?\n";
        }
        sql += """
                )
                SELECT 0 AS nivel, CAST(NULL AS VARCHAR(30)) AS sku,
                       CAST(NULL AS VARCHAR(150)) AS producto,
                       CAST(NULL AS VARCHAR(150)) AS categoria,
                       COALESCE(SUM(unidades), 0) AS unidades,
                       COUNT(DISTINCT id_venta) AS tickets,
                       COALESCE(SUM(subtotal), 0) AS monto
                FROM lineas
                UNION ALL
                SELECT 1, sku, producto, categoria,
                       SUM(unidades), COUNT(DISTINCT id_venta), SUM(subtotal)
                FROM lineas
                GROUP BY id_producto, sku, producto, id_categoria, categoria
                UNION ALL
                SELECT 2, CAST(NULL AS VARCHAR(30)), CAST(NULL AS VARCHAR(150)), categoria,
                       SUM(unidades), COUNT(DISTINCT id_venta), SUM(subtotal)
                FROM lineas
                GROUP BY id_categoria, categoria
                ORDER BY nivel, monto DESC, categoria, producto, sku
                """;

        List<ReporteVentaFila> porProducto = new ArrayList<>();
        List<ReporteVentaFila> porCategoria = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde));
            if (hastaExclusiva != null) {
                ps.setTimestamp(2, Timestamp.valueOf(hastaExclusiva));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getInt("nivel") != 0) {
                    throw new SQLException("La consulta no devolvió el total del período.");
                }
                ReporteVentaFila total = leerFilaReporte(rs);
                while (rs.next()) {
                    ReporteVentaFila fila = leerFilaReporte(rs);
                    if (rs.getInt("nivel") == 1) {
                        porProducto.add(fila);
                    } else {
                        porCategoria.add(fila);
                    }
                }
                return new ReporteConsolidado(total, List.copyOf(porProducto), List.copyOf(porCategoria));
            }
        } catch (SQLException ex) {
            throw new ExcepcionPersistencia("No se pudo generar el reporte de ventas.", ex);
        }
    }

    private static ReporteVentaFila leerFilaReporte(ResultSet rs) throws SQLException {
        return new ReporteVentaFila(rs.getString("sku"), rs.getString("producto"),
                rs.getString("categoria"), rs.getLong("unidades"), rs.getInt("tickets"),
                rs.getBigDecimal("monto"));
    }

    public record ReporteConsolidado(ReporteVentaFila total, List<ReporteVentaFila> porProducto,
            List<ReporteVentaFila> porCategoria) {
    }

    private static VentaResumen leerResumen(ResultSet rs) throws SQLException {
        return new VentaResumen(rs.getInt("id_venta"), rs.getString("numero_ticket"),
                rs.getTimestamp("fecha_hora").toLocalDateTime().format(FORMATO_FECHA),
                rs.getString("nombre_completo"), rs.getString("username"),
                rs.getString("estado"), rs.getBigDecimal("total_venta"));
    }

    public record DetalleConsultado(VentaResumen venta, List<VentaDetalleRegistro> productos) {
    }
}

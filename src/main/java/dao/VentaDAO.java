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

    private static VentaResumen leerResumen(ResultSet rs) throws SQLException {
        return new VentaResumen(rs.getInt("id_venta"), rs.getString("numero_ticket"),
                rs.getTimestamp("fecha_hora").toLocalDateTime().format(FORMATO_FECHA),
                rs.getString("nombre_completo"), rs.getString("username"),
                rs.getString("estado"), rs.getBigDecimal("total_venta"));
    }

    public record DetalleConsultado(VentaResumen venta, List<VentaDetalleRegistro> productos) {
    }
}

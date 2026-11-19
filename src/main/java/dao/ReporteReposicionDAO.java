package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.ReporteReposicionFila;
import service.ExcepcionPersistencia;

public class ReporteReposicionDAO {

    public List<ReporteReposicionFila> consultarProductosCriticos(int diasRotacion) {
        String sql = """
                WITH criticos AS (
                    SELECT id_producto, codigo_sku, nombre, id_categoria,
                           stock_actual, stock_minimo
                    FROM producto
                    WHERE activo = 1 AND stock_actual <= stock_minimo
                ),
                rotacion AS (
                    SELECT dv.id_producto, SUM(CAST(dv.cantidad AS BIGINT)) AS unidades
                    FROM detalle_venta dv
                    JOIN venta v ON v.id_venta = dv.id_venta
                    JOIN criticos p ON p.id_producto = dv.id_producto
                    WHERE v.estado = 'CONFIRMADA'
                      AND v.fecha_hora >= DATEADD(day, ?, CAST(CURRENT_TIMESTAMP AS date))
                      AND v.fecha_hora < DATEADD(day, 1, CAST(CURRENT_TIMESTAMP AS date))
                    GROUP BY dv.id_producto
                )
                SELECT p.codigo_sku, p.nombre AS producto, c.nombre AS categoria,
                       p.stock_actual, p.stock_minimo,
                       COALESCE(r.unidades, 0) AS unidades_vendidas
                FROM criticos p
                JOIN categoria c ON c.id_categoria = p.id_categoria
                LEFT JOIN rotacion r ON r.id_producto = p.id_producto
                ORDER BY CASE WHEN p.stock_actual = 0 THEN 0 ELSE 1 END,
                         unidades_vendidas DESC,
                         (p.stock_minimo - p.stock_actual) DESC,
                         p.codigo_sku
                """;

        List<ReporteReposicionFila> filas = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // El período incluye hoy y los días anteriores restantes.
            ps.setInt(1, 1 - diasRotacion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new ReporteReposicionFila(
                            rs.getString("codigo_sku"), rs.getString("producto"),
                            rs.getString("categoria"), rs.getInt("stock_actual"),
                            rs.getInt("stock_minimo"), rs.getLong("unidades_vendidas")));
                }
            }
        } catch (SQLException ex) {
            throw new ExcepcionPersistencia("No se pudo consultar el reporte de reposiciones.", ex);
        }
        return List.copyOf(filas);
    }
}

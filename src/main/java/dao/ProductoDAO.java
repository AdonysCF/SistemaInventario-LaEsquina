
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.Producto;
import service.ExcepcionPersistencia;

public class ProductoDAO {

    private static final Logger LOGGER =
            Logger.getLogger(ProductoDAO.class.getName());

    public List<Producto> listarTodosActivos() {

        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto WHERE activo = 1";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Producto p = new Producto();

                p.setIdProducto(rs.getInt("id_producto"));
                p.setCodigoSku(rs.getString("codigo_sku"));
                p.setNombre(rs.getString("nombre"));
                p.setIdCategoria(rs.getInt("id_categoria"));
                p.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                p.setStockActual(rs.getInt("stock_actual"));
                p.setStockMinimo(rs.getInt("stock_minimo"));
                p.setEstadoAlerta(rs.getString("estado_alerta"));
                p.setRutaImagen(rs.getString("ruta_imagen"));

                lista.add(p);
            }

            return lista;

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE,
                    "Error al listar los productos activos.", ex);
            throw new ExcepcionPersistencia(
                    "No se pudieron consultar los productos.", ex);
        }
    }

    public List<Producto> buscarPorCriterio(String filtro) {

        List<Producto> lista = new ArrayList<>();

        String sql = "SELECT * FROM producto "
                + "WHERE activo = 1 "
                + "AND (codigo_sku LIKE ? OR nombre LIKE ?)";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String parametro = "%" + filtro + "%";

            ps.setString(1, parametro);
            ps.setString(2, parametro);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Producto p = new Producto();

                    p.setIdProducto(rs.getInt("id_producto"));
                    p.setCodigoSku(rs.getString("codigo_sku"));
                    p.setNombre(rs.getString("nombre"));
                    p.setIdCategoria(rs.getInt("id_categoria"));
                    p.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    p.setStockActual(rs.getInt("stock_actual"));
                    p.setStockMinimo(rs.getInt("stock_minimo"));
                    p.setEstadoAlerta(rs.getString("estado_alerta"));
                    p.setRutaImagen(rs.getString("ruta_imagen"));

                    lista.add(p);
                }
            }

            return lista;

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE,
                    "Error al buscar productos por criterio.", ex);
            throw new ExcepcionPersistencia(
                    "No se pudieron buscar los productos.", ex);
        }
    }

    public List<Producto> consultarExistencias(String buscar, String estado) {
        List<Producto> resultados = new ArrayList<>();

        // Se calcula el estado con los valores actuales del producto (RN-04).
        String sql = """
                WITH existencias AS (
                    SELECT id_producto, codigo_sku, nombre, id_categoria,
                           precio_unitario, stock_actual, stock_minimo, ruta_imagen,
                           CASE
                               WHEN stock_actual = 0 THEN 'AGOTADO'
                               WHEN stock_actual <= stock_minimo THEN 'PROXIMO_A_AGOTARSE'
                               ELSE 'DISPONIBLE'
                           END AS estado_calculado
                    FROM producto
                    WHERE activo = 1
                )
                SELECT id_producto, codigo_sku, nombre, id_categoria,
                       precio_unitario, stock_actual, stock_minimo, ruta_imagen,
                       estado_calculado
                FROM existencias
                WHERE 1 = 1
                """;

        if (!buscar.isEmpty()) {
            sql += " AND (codigo_sku LIKE ? OR nombre LIKE ?)";
        }
        if (!estado.isEmpty()) {
            sql += " AND estado_calculado = ?";
        }
        sql += " ORDER BY nombre, codigo_sku";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int indice = 1;
            if (!buscar.isEmpty()) {
                String patron = "%" + buscar + "%";
                ps.setString(indice++, patron);
                ps.setString(indice++, patron);
            }
            if (!estado.isEmpty()) {
                ps.setString(indice, estado);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Producto p = new Producto();
                    p.setIdProducto(rs.getInt("id_producto"));
                    p.setCodigoSku(rs.getString("codigo_sku"));
                    p.setNombre(rs.getString("nombre"));
                    p.setIdCategoria(rs.getInt("id_categoria"));
                    p.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    p.setStockActual(rs.getInt("stock_actual"));
                    p.setStockMinimo(rs.getInt("stock_minimo"));
                    p.setRutaImagen(rs.getString("ruta_imagen"));
                    p.setEstadoAlerta(rs.getString("estado_calculado"));
                    p.setActivo(true);
                    resultados.add(p);
                }
            }

            return resultados;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al consultar existencias.", ex);
            throw new ExcepcionPersistencia(
                    "No se pudieron consultar las existencias.", ex);
        }
    }

    public boolean agregarProducto(Producto p) {

        String sql = "INSERT INTO producto "
                + "(codigo_sku, nombre, id_categoria, precio_unitario, "
                + "stock_minimo, ruta_imagen, estado_alerta) "
                + "VALUES (?, ?, ?, ?, ?, ?, 'AGOTADO')";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getCodigoSku());
            ps.setString(2, p.getNombre());
            ps.setInt(3, p.getIdCategoria());
            ps.setBigDecimal(4, p.getPrecioUnitario());
            ps.setInt(5, p.getStockMinimo());
            ps.setString(6, p.getRutaImagen());

            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE,
                    "Error al registrar el producto.", ex);
            throw new ExcepcionPersistencia(
                    "No se pudo registrar el producto.", ex);
        }
    }

    public boolean actualizarProductoParcial(
            int idProducto,
            String nuevoSku,
            java.math.BigDecimal nuevoPrecio,
            int nuevoStockMinimo) {

        String sql = """
                UPDATE producto
                SET codigo_sku = ?,
                    precio_unitario = ?,
                    stock_minimo = ?,
                    estado_alerta = CASE
                        WHEN stock_actual = 0 THEN 'AGOTADO'
                        WHEN stock_actual <= ? THEN 'PROXIMO_A_AGOTARSE'
                        ELSE 'DISPONIBLE'
                    END
                WHERE id_producto = ?
                """;

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoSku);
            ps.setBigDecimal(2, nuevoPrecio);
            ps.setInt(3, nuevoStockMinimo);
            ps.setInt(4, nuevoStockMinimo);
            ps.setInt(5, idProducto);

            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE,
                    "Error al actualizar el producto con ID "
                    + idProducto + ".", ex);
            throw new ExcepcionPersistencia(
                    "No se pudo actualizar el producto.", ex);
        }
    }

    public Producto obtenerPorIdTransaccional(
            Connection conn,
            int idProducto) throws SQLException {

        Producto p = null;

        String sql = "SELECT * FROM producto "
                + "WITH (UPDLOCK, ROWLOCK) WHERE id_producto = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idProducto);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    p = new Producto();

                    p.setIdProducto(rs.getInt("id_producto"));
                    p.setCodigoSku(rs.getString("codigo_sku"));
                    p.setNombre(rs.getString("nombre"));
                    p.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    p.setStockActual(rs.getInt("stock_actual"));
                    p.setStockMinimo(rs.getInt("stock_minimo"));
                    p.setEstadoAlerta(rs.getString("estado_alerta"));
                    p.setRutaImagen(rs.getString("ruta_imagen"));
                }
            }
        }

        return p;
    }

    public boolean actualizarStockTransaccional(
            Connection conn,
            int idProducto,
            int nuevoStock,
            String estadoAlerta) throws SQLException {

        String sql = "UPDATE producto "
                + "SET stock_actual = ?, estado_alerta = ? "
                + "WHERE id_producto = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, nuevoStock);
            ps.setString(2, estadoAlerta);
            ps.setInt(3, idProducto);

            return ps.executeUpdate() > 0;
        }
    }
    
    
public boolean existeSku(String sku, int idExcluir) {

    String sql = "SELECT COUNT(*) FROM producto "
            + "WHERE codigo_sku = ? "
            + "AND id_producto <> ?";

    try (Connection con = ConexionDB.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, sku);
        ps.setInt(2, idExcluir);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }

        return false;

    } catch (SQLException ex) {
        LOGGER.log(Level.SEVERE,
                "Error al verificar el código SKU.", ex);

        throw new ExcepcionPersistencia(
                "No se pudo validar el código SKU.", ex);
    }
}

public Producto buscarPorId(int idProducto) {

    String sql = "SELECT * FROM producto WHERE id_producto = ?";

    try (Connection con = ConexionDB.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, idProducto);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                Producto p = new Producto();

                p.setIdProducto(rs.getInt("id_producto"));
                p.setCodigoSku(rs.getString("codigo_sku"));
                p.setNombre(rs.getString("nombre"));
                p.setIdCategoria(rs.getInt("id_categoria"));
                p.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                p.setStockActual(rs.getInt("stock_actual"));
                p.setStockMinimo(rs.getInt("stock_minimo"));
                p.setEstadoAlerta(rs.getString("estado_alerta"));
                p.setActivo(rs.getBoolean("activo"));
                p.setRutaImagen(rs.getString("ruta_imagen"));

                return p;
            }
        }

        return null;

    } catch (SQLException ex) {
        LOGGER.log(Level.SEVERE,
                "Error al consultar el producto.", ex);

        throw new ExcepcionPersistencia(
                "No se pudo consultar el producto.", ex);
    }
}

}

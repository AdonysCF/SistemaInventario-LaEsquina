package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Producto;

public class ProductoDAO {

    /**
     * Devuelve todos los productos que están activos en el catálogo.
     * Usado en ConsultaServlet y ProductoServlet (acción "listar").
     */
    public List<Producto> listarTodosActivos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto WHERE activo = 1";
        
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = ConexionDB.getConnection();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

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
                p.setRutaImagen(rs.getString("ruta_imagen")); // Carga la imagen
                lista.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                ConexionDB.cerrar(con);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return lista;
    }

    /**
     * Busca productos por coincidencia de SKU o de nombre.
     * Usado en ConsultaServlet para el buscador rápido de la tienda.
     */
    public List<Producto> buscarPorCriterio(String filtro) {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto WHERE activo = 1 AND (codigo_sku LIKE ? OR nombre LIKE ?)";
        
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = ConexionDB.getConnection();
            ps = con.prepareStatement(sql);
            String parametro = "%" + filtro + "%";
            ps.setString(1, parametro);
            ps.setString(2, parametro);
            
            rs = ps.executeQuery();

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
                p.setRutaImagen(rs.getString("ruta_imagen")); // Carga la imagen
                lista.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                ConexionDB.cerrar(con);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return lista;
    }

    /**
     * Inserta un nuevo producto en el catálogo (El stock inicia en 0 por RN-02).
     * Usado en ProductoServlet.
     */
    public boolean agregarProducto(Producto p) {
        // En tu tabla la imagen se guarda en 'ruta_imagen' y el estado inicial es 'AGOTADO'
        String sql = "INSERT INTO producto (codigo_sku, nombre, id_categoria, precio_unitario, stock_minimo, ruta_imagen, estado_alerta) VALUES (?, ?, ?, ?, ?, ?, 'AGOTADO')";
        
        Connection con = null;
        PreparedStatement ps = null;
        
        try {
            con = ConexionDB.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, p.getCodigoSku());
            ps.setString(2, p.getNombre());
            ps.setInt(3, p.getIdCategoria());
            ps.setBigDecimal(4, p.getPrecioUnitario());
            ps.setInt(5, p.getStockMinimo());
            ps.setString(6, p.getRutaImagen());
            
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (ps != null) ps.close();
                ConexionDB.cerrar(con);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Actualiza solo los datos editables (SKU, Precio y Stock Mínimo) de un producto.
     * Usado en ProductoServlet al editar desde el modal.
     */
    public boolean actualizarProductoParcial(int idProducto, String nuevoSku, java.math.BigDecimal nuevoPrecio, int nuevoStockMinimo) {
        String sql = "UPDATE producto SET codigo_sku = ?, precio_unitario = ?, stock_minimo = ? WHERE id_producto = ?";
        
        Connection con = null;
        PreparedStatement ps = null;
        
        try {
            con = ConexionDB.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, nuevoSku);
            ps.setBigDecimal(2, nuevoPrecio);
            ps.setInt(3, nuevoStockMinimo);
            ps.setInt(4, idProducto);
            
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (ps != null) ps.close();
                ConexionDB.cerrar(con);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Obtiene un producto bloqueando la fila (ROWLOCK, UPDLOCK) para la concurrencia.
     * Usado internamente por los servicios transaccionales.
     */
    public Producto obtenerPorIdTransaccional(Connection conn, int idProducto) throws SQLException {
        Producto p = null;
        String sql = "SELECT * FROM producto WITH (UPDLOCK, ROWLOCK) WHERE id_producto = ?";
        
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

    /**
     * Actualiza el stock y la alerta de un producto usando la conexión transaccional.
     * Usado internamente por los servicios transaccionales.
     */
    public boolean actualizarStockTransaccional(Connection conn, int idProducto, int nuevoStock, String estadoAlerta) throws SQLException {
        String sql = "UPDATE producto SET stock_actual = ?, estado_alerta = ? WHERE id_producto = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nuevoStock);
            ps.setString(2, estadoAlerta);
            ps.setInt(3, idProducto);
            
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        }
    }
}
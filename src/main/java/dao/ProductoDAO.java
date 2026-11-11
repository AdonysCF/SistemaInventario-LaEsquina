package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Producto;

public class ProductoDAO {

    // 1. LEER (Listar todos los productos activos)
    public List<Producto> listarTodosActivos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto WHERE activo = 1";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

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
                p.setActivo(rs.getBoolean("activo"));
                p.setRutaImagen(rs.getString("ruta_imagen"));
                lista.add(p);
            }
        } catch (Exception e) {
            System.out.println("Error en listarTodosActivos: " + e.getMessage());
        }
        return lista;
    }

    // 2. CREAR (Registrar nuevo producto)
    public boolean registrarProducto(Producto p) {
        String sql = "INSERT INTO producto (codigo_sku, nombre, id_categoria, precio_unitario, stock_actual, stock_minimo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getCodigoSku());
            ps.setString(2, p.getNombre());
            ps.setInt(3, p.getIdCategoria()); // Asume que el usuario selecciona una categoría válida
            ps.setBigDecimal(4, p.getPrecioUnitario());
            ps.setInt(5, p.getStockActual());
            ps.setInt(6, p.getStockMinimo());

            int filas = ps.executeUpdate();
            return filas > 0;
        } catch (Exception e) {
            System.out.println("Error en registrarProducto: " + e.getMessage());
            return false;
        }
    }

    // Añadir a ProductoDAO.java
    public Producto obtenerPorId(Connection conn, int idProducto) throws Exception {
        Producto p = null;
        String sql = "SELECT * FROM producto WHERE id_producto = ?";
        try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p = new Producto();
                    p.setIdProducto(rs.getInt("id_producto"));
                    p.setStockActual(rs.getInt("stock_actual"));
                    p.setStockMinimo(rs.getInt("stock_minimo"));
                    // Setear los demás campos que necesites
                }
            }
        }
        return p;
    }

    public void actualizarStock(Connection conn, int idProducto, int nuevoStock, String estadoAlerta) throws Exception {
        String sql = "UPDATE producto SET stock_actual = ?, estado_alerta = ? WHERE id_producto = ?";
        try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nuevoStock);
            ps.setString(2, estadoAlerta);
            ps.setInt(3, idProducto);
            ps.executeUpdate();
        }
    }

    // Método para RF-CON-01 (Búsqueda flexible)
    public List<Producto> buscarPorCriterio(String criterio) {
        List<Producto> lista = new ArrayList<>();
        // Busca coincidencias en nombre o código exacto
        String sql = "SELECT * FROM producto WHERE (nombre LIKE ? OR codigo_sku = ?) AND activo = 1";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + criterio + "%"); // Permite buscar partes del nombre
            ps.setString(2, criterio);

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
                    lista.add(p);
                }
            }
        } catch (Exception e) {
            System.out.println("Error en buscarPorCriterio: " + e.getMessage());
        }
        return lista;
    }

    public boolean agregarProducto(Producto p) {
        // El stock_actual por defecto es 0 en la BD, estado_alerta por defecto es AGOTADO porque inicia en 0
        String sql = "INSERT INTO producto (codigo_sku, nombre, id_categoria, precio_unitario, stock_minimo, ruta_imagen, estado_alerta) VALUES (?, ?, ?, ?, ?, ?, 'AGOTADO')";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = ConexionDB.getConnection(); // Ajusta según el nombre exacto de tu clase de conexión
            ps = con.prepareStatement(sql);
            ps.setString(1, p.getCodigoSku());
            ps.setString(2, p.getNombre());
            ps.setInt(3, p.getIdCategoria());
            ps.setBigDecimal(4, p.getPrecioUnitario()); // Asumiendo que usas BigDecimal para precio
            ps.setInt(5, p.getStockMinimo());
            ps.setString(6, p.getRutaImagen()); // El nuevo campo que añadiste antes

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
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
                if (con != null) con.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}


package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import modelo.Categoria;
import service.ExcepcionPersistencia;

public class CategoriaDAO {

    private static final Logger LOGGER =
            Logger.getLogger(CategoriaDAO.class.getName());

    // LISTAR TODAS LAS CATEGORIAS
    // Incluye categorias activas e inactivas.
    public List<Categoria> listarTodas() {

        List<Categoria> lista = new ArrayList<>();

        String sql = "SELECT id_categoria, nombre, descripcion, activo "
                   + "FROM categoria "
                   + "ORDER BY nombre ASC";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Categoria categoria = new Categoria();

                categoria.setIdCategoria(
                        rs.getInt("id_categoria"));

                categoria.setNombre(
                        rs.getString("nombre"));

                categoria.setDescripcion(
                        rs.getString("descripcion"));

                categoria.setActivo(
                        rs.getBoolean("activo"));

                lista.add(categoria);
            }

            return lista;

        } catch (SQLException ex) {

            LOGGER.log(Level.SEVERE,
                    "Error al listar las categorias.", ex);

            throw new ExcepcionPersistencia(
                    "No se pudieron consultar las categorias.", ex);
        }
    }

    // LISTAR SOLAMENTE CATEGORIAS ACTIVAS
    // Se utiliza en el formulario de registro de productos.
    public List<Categoria> listarActivas() {

        List<Categoria> lista = new ArrayList<>();

        String sql = "SELECT id_categoria, nombre, descripcion, activo "
                   + "FROM categoria "
                   + "WHERE activo = 1 "
                   + "ORDER BY nombre ASC";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Categoria categoria = new Categoria();

                categoria.setIdCategoria(
                        rs.getInt("id_categoria"));

                categoria.setNombre(
                        rs.getString("nombre"));

                categoria.setDescripcion(
                        rs.getString("descripcion"));

                categoria.setActivo(
                        rs.getBoolean("activo"));

                lista.add(categoria);
            }

            return lista;

        } catch (SQLException ex) {

            LOGGER.log(Level.SEVERE,
                    "Error al consultar las categorias activas.", ex);

            throw new ExcepcionPersistencia(
                    "No se pudieron consultar las categorias activas.", ex);
        }
    }

    // BUSCAR CATEGORIA POR ID
    public Categoria buscarPorId(int idCategoria) {

        String sql = "SELECT id_categoria, nombre, descripcion, activo "
                   + "FROM categoria "
                   + "WHERE id_categoria = ?";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCategoria);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    Categoria categoria = new Categoria();

                    categoria.setIdCategoria(
                            rs.getInt("id_categoria"));

                    categoria.setNombre(
                            rs.getString("nombre"));

                    categoria.setDescripcion(
                            rs.getString("descripcion"));

                    categoria.setActivo(
                            rs.getBoolean("activo"));

                    return categoria;
                }
            }

            return null;

        } catch (SQLException ex) {

            LOGGER.log(Level.SEVERE,
                    "Error al buscar la categoria.", ex);

            throw new ExcepcionPersistencia(
                    "No se pudo consultar la categoria.", ex);
        }
    }

    // VERIFICAR SI YA EXISTE UN NOMBRE
    // El segundo parametro permite excluir la categoria
    // que se esta editando.
    public boolean existeNombre(String nombre, int idExcluir) {

        String sql = "SELECT COUNT(*) "
                   + "FROM categoria "
                   + "WHERE nombre = ? "
                   + "AND id_categoria <> ?";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, idExcluir);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

            return false;

        } catch (SQLException ex) {

            LOGGER.log(Level.SEVERE,
                    "Error al verificar el nombre de la categoria.", ex);

            throw new ExcepcionPersistencia(
                    "No se pudo validar el nombre de la categoria.", ex);
        }
    }

    // CONTAR PRODUCTOS ACTIVOS ASOCIADOS
    public int contarProductosActivos(int idCategoria) {

        String sql = "SELECT COUNT(*) "
                   + "FROM producto "
                   + "WHERE id_categoria = ? "
                   + "AND activo = 1";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCategoria);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

            return 0;

        } catch (SQLException ex) {

            LOGGER.log(Level.SEVERE,
                    "Error al contar los productos asociados.", ex);

            throw new ExcepcionPersistencia(
                    "No se pudieron verificar los productos asociados.", ex);
        }
    }

    // REGISTRAR NUEVA CATEGORIA
    public boolean insertar(Categoria categoria) {

        String sql = "INSERT INTO categoria "
                   + "(nombre, descripcion, activo) "
                   + "VALUES (?, ?, 1)";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());

            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {

            LOGGER.log(Level.SEVERE,
                    "Error al registrar la categoria.", ex);

            throw new ExcepcionPersistencia(
                    "No se pudo registrar la categoria.", ex);
        }
    }

    // ACTUALIZAR NOMBRE Y DESCRIPCION
    public boolean actualizar(Categoria categoria) {

        String sql = "UPDATE categoria "
                   + "SET nombre = ?, descripcion = ? "
                   + "WHERE id_categoria = ?";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());
            ps.setInt(3, categoria.getIdCategoria());

            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {

            LOGGER.log(Level.SEVERE,
                    "Error al actualizar la categoria.", ex);

            throw new ExcepcionPersistencia(
                    "No se pudo actualizar la categoria.", ex);
        }
    }

    // CAMBIAR ESTADO: ACTIVAR O DESACTIVAR
    public boolean cambiarEstado(int idCategoria, boolean activo) {

        String sql = "UPDATE categoria "
                   + "SET activo = ? "
                   + "WHERE id_categoria = ?";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, activo);
            ps.setInt(2, idCategoria);

            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {

            LOGGER.log(Level.SEVERE,
                    "Error al cambiar el estado de la categoria.", ex);

            throw new ExcepcionPersistencia(
                    "No se pudo cambiar el estado de la categoria.", ex);
        }
    }
}

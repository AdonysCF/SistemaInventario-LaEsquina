
package service;

import dao.CategoriaDAO;
import java.sql.SQLException;
import java.util.List;
import modelo.Categoria;

public class CategoriaService {

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    public List<Categoria> listarTodas() {
        return categoriaDAO.listarTodas();
    }

    public List<Categoria> listarActivas() {
        return categoriaDAO.listarActivas();
    }

    public Categoria buscarPorId(int idCategoria) {

        if (idCategoria <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de la categoria no es valido.");
        }

        return categoriaDAO.buscarPorId(idCategoria);
    }

    // REGISTRAR CATEGORIA
    public boolean registrar(String nombre, String descripcion) {

        Categoria categoria = construirCategoria(nombre, descripcion);

        validarNombreDuplicado(categoria.getNombre(), 0);

        try {

            return categoriaDAO.insertar(categoria);

        } catch (ExcepcionPersistencia ex) {

            if (esNombreDuplicado(ex)) {
                throw new IllegalArgumentException(
                        "Ya existe una categoria con ese nombre.");
            }

            throw ex;
        }
    }

    // EDITAR CATEGORIA
    public boolean actualizar(
            int idCategoria,
            String nombre,
            String descripcion) {

        if (idCategoria <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de la categoria no es valido.");
        }

        Categoria existente = categoriaDAO.buscarPorId(idCategoria);

        if (existente == null) {
            throw new IllegalArgumentException(
                    "La categoria que intenta editar no existe.");
        }

        Categoria categoria = construirCategoria(nombre, descripcion);

        categoria.setIdCategoria(idCategoria);

        validarNombreDuplicado(
                categoria.getNombre(), idCategoria);

        try {

            return categoriaDAO.actualizar(categoria);

        } catch (ExcepcionPersistencia ex) {

            if (esNombreDuplicado(ex)) {
                throw new IllegalArgumentException(
                        "Ya existe otra categoria con ese nombre.");
            }

            throw ex;
        }
    }

    // ACTIVAR O DESACTIVAR CATEGORIA
    public boolean cambiarEstado(int idCategoria, boolean activar) {

        if (idCategoria <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de la categoria no es valido.");
        }

        Categoria categoria = categoriaDAO.buscarPorId(idCategoria);

        if (categoria == null) {
            throw new IllegalArgumentException(
                    "La categoria seleccionada no existe.");
        }

        if (categoria.isActivo() == activar) {
            throw new IllegalArgumentException(
                    activar
                    ? "La categoria ya se encuentra activa."
                    : "La categoria ya se encuentra desactivada.");
        }

        // No permitir desactivar categorias que tengan
        // productos activos asociados.
        if (!activar) {

            int productosAsociados =
                    categoriaDAO.contarProductosActivos(idCategoria);

            if (productosAsociados > 0) {
                throw new IllegalArgumentException(
                        "No se puede desactivar esta categoria porque tiene "
                        + productosAsociados
                        + " producto(s) activo(s) asociado(s). "
                        + "Reasigne o desactive esos productos primero.");
            }
        }

        return categoriaDAO.cambiarEstado(idCategoria, activar);
    }

    // CONSTRUIR Y VALIDAR DATOS
    private Categoria construirCategoria(
            String nombre,
            String descripcion) {

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre de la categoria es obligatorio.");
        }

        nombre = nombre.trim();

        if (nombre.length() > 80) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar los 80 caracteres.");
        }

        if (descripcion != null) {

            descripcion = descripcion.trim();

            if (descripcion.isEmpty()) {
                descripcion = null;
            }

            if (descripcion != null && descripcion.length() > 200) {
                throw new IllegalArgumentException(
                        "La descripcion no puede superar los 200 caracteres.");
            }
        }

        Categoria categoria = new Categoria();

        categoria.setNombre(nombre);
        categoria.setDescripcion(descripcion);
        categoria.setActivo(true);

        return categoria;
    }

    // VALIDAR NOMBRE DUPLICADO
    private void validarNombreDuplicado(
            String nombre,
            int idExcluir) {

        if (categoriaDAO.existeNombre(nombre, idExcluir)) {
            throw new IllegalArgumentException(
                    "Ya existe una categoria con ese nombre.");
        }
    }

    // IDENTIFICAR DUPLICADOS SQL SERVER
    private boolean esNombreDuplicado(Throwable ex) {

        Throwable actual = ex;

        while (actual != null) {

            if (actual instanceof SQLException) {

                SQLException sqlEx = (SQLException) actual;

                if (sqlEx.getErrorCode() == 2627
                        || sqlEx.getErrorCode() == 2601) {
                    return true;
                }
            }

            actual = actual.getCause();
        }

        return false;
    }
}

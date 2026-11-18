
package service;

import dao.CategoriaDAO;
import dao.ProductoDAO;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.regex.Pattern;
import modelo.Categoria;
import modelo.Producto;

public class ProductoService {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    private static final Pattern PATRON_SKU =
            Pattern.compile("^[A-Z0-9][A-Z0-9._-]{0,29}$");

    public boolean registrar(Producto producto) {
        validarProducto(producto);

        if (productoDAO.existeSku(producto.getCodigoSku(), 0)) {
            throw new IllegalArgumentException(
                    "Ya existe un producto registrado con ese código SKU.");
        }

        try {
            return productoDAO.agregarProducto(producto);
        } catch (ExcepcionPersistencia ex) {
            if (esDuplicado(ex)) {
                throw new IllegalArgumentException(
                        "El código SKU ya está registrado.");
            }
            throw ex;
        }
    }

    public boolean actualizar(
            int idProducto,
            String sku,
            BigDecimal precio,
            int stockMinimo) {

        if (idProducto <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del producto no es válido.");
        }

        Producto existente = productoDAO.buscarPorId(idProducto);

        if (existente == null) {
            throw new IllegalArgumentException(
                    "El producto que intenta editar no existe.");
        }

        String skuValidado = validarSku(sku);
        BigDecimal precioValidado = validarPrecio(precio);
        validarStockMinimo(stockMinimo);

        if (productoDAO.existeSku(skuValidado, idProducto)) {
            throw new IllegalArgumentException(
                    "Ese código SKU pertenece a otro producto.");
        }

        try {
            return productoDAO.actualizarProductoParcial(
                    idProducto,
                    skuValidado,
                    precioValidado,
                    stockMinimo);
        } catch (ExcepcionPersistencia ex) {
            if (esDuplicado(ex)) {
                throw new IllegalArgumentException(
                        "El código SKU ya está registrado.");
            }
            throw ex;
        }
    }

    private void validarProducto(Producto p) {
        if (p == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del producto.");
        }

        p.setCodigoSku(validarSku(p.getCodigoSku()));
        p.setNombre(validarNombre(p.getNombre()));
        p.setPrecioUnitario(validarPrecio(p.getPrecioUnitario()));
        validarStockMinimo(p.getStockMinimo());
        validarCategoria(p.getIdCategoria());
        p.setRutaImagen(validarRutaImagen(p.getRutaImagen()));
    }

    private String validarSku(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El código SKU es obligatorio.");
        }

        sku = sku.trim().toUpperCase();

        if (!PATRON_SKU.matcher(sku).matches()) {
            throw new IllegalArgumentException(
                    "El SKU debe tener máximo 30 caracteres y utilizar "
                    + "letras, números, guion, punto o guion bajo.");
        }

        return sku;
    }

    private String validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre del producto es obligatorio.");
        }

        nombre = nombre.trim();

        if (nombre.length() > 150) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar los 150 caracteres.");
        }

        if (nombre.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(
                    "El nombre contiene caracteres no permitidos.");
        }

        return nombre;
    }

    private BigDecimal validarPrecio(BigDecimal precio) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El precio debe ser mayor que cero.");
        }

        if (precio.scale() > 2) {
            throw new IllegalArgumentException(
                    "El precio solo puede tener hasta dos decimales.");
        }

        if (precio.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new IllegalArgumentException(
                    "El precio supera el máximo permitido.");
        }

        return precio;
    }

    private void validarStockMinimo(int stockMinimo) {
        if (stockMinimo < 0) {
            throw new IllegalArgumentException(
                    "El stock mínimo no puede ser negativo.");
        }
    }

    private void validarCategoria(int idCategoria) {
        if (idCategoria <= 0) {
            throw new IllegalArgumentException(
                    "Debe seleccionar una categoría.");
        }

        Categoria categoria = categoriaDAO.buscarPorId(idCategoria);

        if (categoria == null || !categoria.isActivo()) {
            throw new IllegalArgumentException(
                    "La categoría seleccionada no existe o está inactiva.");
        }
    }

    private String validarRutaImagen(String ruta) {
        if (ruta == null || ruta.trim().isEmpty()) {
            return null;
        }

        ruta = ruta.trim();

        if (ruta.length() > 255) {
            throw new IllegalArgumentException(
                    "La ruta de la imagen no puede superar los 255 caracteres.");
        }

        if (ruta.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(
                    "La ruta de la imagen contiene caracteres no permitidos.");
        }

        return ruta;
    }

    private boolean esDuplicado(Throwable ex) {
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

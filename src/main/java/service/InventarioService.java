
package service;

import dao.ConexionDB;
import dao.MovimientoDAO;
import dao.ProductoDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.DetalleMovimiento;
import modelo.MovimientoInventario;
import modelo.Producto;

public class InventarioService {

    private static final Logger LOGGER =
            Logger.getLogger(InventarioService.class.getName());

    private final MovimientoDAO movimientoDAO = new MovimientoDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    /**
     * Registra una entrada de inventario de manera transaccional.
     * Todos los productos se registran o ninguno se registra.
     */
    public boolean registrarEntradaTransaccional(
            MovimientoInventario mov,
            List<DetalleMovimiento> detalles) {

        if (mov == null || detalles == null || detalles.isEmpty()) {
            return false;
        }

        // Validar los detalles antes de iniciar la transacción.
        for (DetalleMovimiento det : detalles) {
            if (det == null
                    || det.getIdProducto() <= 0
                    || det.getCantidad() <= 0) {
                return false;
            }
        }

        try (Connection conn = ConexionDB.getConnection()) {

            conn.setAutoCommit(false);

            try {
                // 1. Registrar la cabecera del movimiento.
                int idMovimiento =
                        movimientoDAO.insertarMovimientoTransaccional(conn, mov);

                if (idMovimiento <= 0) {
                    conn.rollback();
                    return false;
                }

                // 2. Procesar cada producto ingresado.
                for (DetalleMovimiento det : detalles) {

                    det.setIdMovimiento(idMovimiento);

                    Producto producto =
                            productoDAO.obtenerPorIdTransaccional(
                                    conn, det.getIdProducto());

                    if (producto == null) {
                        conn.rollback();
                        return false;
                    }

                    int stockAnterior = producto.getStockActual();
                    int nuevoStock;

                    try {
                        nuevoStock = Math.addExact(
                                stockAnterior, det.getCantidad());
                    } catch (ArithmeticException ex) {
                        conn.rollback();
                        return false;
                    }

                    det.setStockAnterior(stockAnterior);
                    det.setStockResultante(nuevoStock);

                    // 3. Registrar el detalle en el Kardex.
                    boolean detalleRegistrado =
                            movimientoDAO.insertarDetalleMovTransaccional(
                                    conn, det);

                    if (!detalleRegistrado) {
                        conn.rollback();
                        return false;
                    }

                    // 4. Actualizar el stock y su estado de alerta.
                    String nuevaAlerta = evaluarEstadoAlerta(
                            nuevoStock, producto.getStockMinimo());

                    boolean stockActualizado =
                            productoDAO.actualizarStockTransaccional(
                                    conn,
                                    producto.getIdProducto(),
                                    nuevoStock,
                                    nuevaAlerta);

                    if (!stockActualizado) {
                        conn.rollback();
                        return false;
                    }
                }

                // 5. Confirmar todos los cambios.
                conn.commit();
                return true;

            } catch (Exception ex) {
                conn.rollback();

                LOGGER.log(
                        Level.SEVERE,
                        "Error al registrar la entrada de inventario.",
                        ex);

                return false;
            }

        } catch (SQLException ex) {
            LOGGER.log(
                    Level.SEVERE,
                    "Error de conexión al registrar la entrada.",
                    ex);

            return false;
        }
    }

    /**
     * Registra un ajuste positivo o negativo de inventario.
     *
     * AJUSTE_POSITIVO: aumenta el stock.
     * AJUSTE_NEGATIVO: disminuye el stock.
     *
     * El proceso es transaccional: si falla un producto,
     * se revierten todos los cambios del ajuste.
     */
    public String registrarAjuste(MovimientoInventario mov) {

        // 1. Validaciones generales.
        if (mov == null) {
            return "ERROR: No se recibió información del ajuste.";
        }

        List<DetalleMovimiento> detalles = mov.getDetalles();

        if (detalles == null || detalles.isEmpty()) {
            return "ERROR: Debe agregar al menos un producto al ajuste.";
        }

        String tipoAjuste = mov.getTipoMovimiento();

        boolean esPositivo = "AJUSTE_POSITIVO".equals(tipoAjuste);
        boolean esNegativo = "AJUSTE_NEGATIVO".equals(tipoAjuste);

        if (!esPositivo && !esNegativo) {
            return "ERROR: El tipo de ajuste no es válido.";
        }

        // 2. Validar los productos y cantidades.
        for (DetalleMovimiento det : detalles) {

            if (det == null) {
                return "ERROR: Existe un detalle de producto inválido.";
            }

            if (det.getIdProducto() <= 0) {
                return "ERROR: Seleccione un producto válido.";
            }

            if (det.getCantidad() <= 0) {
                return "ERROR: Todas las cantidades deben ser mayores a cero.";
            }
        }

        // 3. Ejecutar la operación dentro de una transacción.
        try (Connection conn = ConexionDB.getConnection()) {

            conn.setAutoCommit(false);

            try {
                // Registrar la cabecera del ajuste.
                int idMovimiento =
                        movimientoDAO.insertarMovimientoTransaccional(
                                conn, mov);

                if (idMovimiento <= 0) {
                    conn.rollback();
                    return "ERROR: No se pudo registrar la cabecera del ajuste.";
                }

                // Procesar cada producto del ajuste.
                for (DetalleMovimiento det : detalles) {

                    det.setIdMovimiento(idMovimiento);

                    // Obtener el producto bloqueando su fila
                    // para evitar modificaciones simultáneas.
                    Producto producto =
                            productoDAO.obtenerPorIdTransaccional(
                                    conn, det.getIdProducto());

                    if (producto == null) {
                        conn.rollback();
                        return "ERROR: Uno de los productos no existe.";
                    }

                    int stockAnterior = producto.getStockActual();
                    int cantidadAjuste = det.getCantidad();

                    // CORRECCIÓN CRÍTICA:
                    // El tipo de ajuste determina si se suma o se resta.
                    int variacion = esPositivo
                            ? cantidadAjuste
                            : -cantidadAjuste;

                    int nuevoStock;

                    try {
                        nuevoStock = Math.addExact(
                                stockAnterior, variacion);
                    } catch (ArithmeticException ex) {
                        conn.rollback();
                        return "ERROR: La cantidad del ajuste supera el límite permitido.";
                    }

                    // Impedir que el inventario termine con stock negativo.
                    if (nuevoStock < 0) {
                        conn.rollback();

                        return "ERROR: El ajuste dejaría el stock en negativo "
                                + "para el producto: " + producto.getNombre()
                                + ". Stock actual: " + stockAnterior
                                + ", cantidad a descontar: " + cantidadAjuste;
                    }

                    // 4. Preparar los datos del Kardex.
                    det.setStockAnterior(stockAnterior);
                    det.setStockResultante(nuevoStock);

                    // La cantidad se guarda como valor positivo.
                    // La naturaleza del movimiento indica si fue entrada o salida.
                    det.setCantidad(cantidadAjuste);

                    // 5. Registrar el detalle del movimiento.
                    boolean detalleRegistrado =
                            movimientoDAO.insertarDetalleMovTransaccional(
                                    conn, det);

                    if (!detalleRegistrado) {
                        conn.rollback();
                        return "ERROR: No se pudo registrar el detalle en el historial.";
                    }

                    // 6. Actualizar el stock del producto.
                    String nuevaAlerta = evaluarEstadoAlerta(
                            nuevoStock, producto.getStockMinimo());

                    boolean stockActualizado =
                            productoDAO.actualizarStockTransaccional(
                                    conn,
                                    producto.getIdProducto(),
                                    nuevoStock,
                                    nuevaAlerta);

                    if (!stockActualizado) {
                        conn.rollback();
                        return "ERROR: No se pudo actualizar el stock del producto.";
                    }
                }

                // 7. Confirmar todos los cambios.
                conn.commit();

                return "EXITO: Ajuste registrado correctamente. "
                        + "El inventario ha sido actualizado.";

            } catch (Exception ex) {

                // Si ocurre cualquier error, revertir toda la operación.
                conn.rollback();

                LOGGER.log(
                        Level.SEVERE,
                        "Error al registrar el ajuste de inventario.",
                        ex);

                return "ERROR: No se pudo completar el ajuste de inventario.";
            }

        } catch (SQLException ex) {

            LOGGER.log(
                    Level.SEVERE,
                    "Error de conexión al registrar el ajuste.",
                    ex);

            return "ERROR: Ocurrió un problema al conectar con la base de datos.";
        }
    }

    /**
     * Determina el estado de alerta según el stock actual
     * y el stock mínimo configurado para el producto.
     */
    private String evaluarEstadoAlerta(int stockActual, int stockMinimo) {

        if (stockActual == 0) {
            return "AGOTADO";
        }

        if (stockActual <= stockMinimo) {
            return "PROXIMO_A_AGOTARSE";
        }

        return "DISPONIBLE";
    }
}

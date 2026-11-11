
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
     */
    public boolean registrarEntradaTransaccional(
            MovimientoInventario mov,
            List<DetalleMovimiento> detalles) throws ExcepcionNegocio {

        if (mov == null || detalles == null || detalles.isEmpty()) {
            throw new ExcepcionNegocio(
                    ExcepcionNegocio.Tipo.DATOS_INVALIDOS,
                    "Debe proporcionar los datos del movimiento y al menos un producto.");
        }

        for (DetalleMovimiento det : detalles) {
            if (det == null
                    || det.getIdProducto() <= 0
                    || det.getCantidad() <= 0) {
                throw new ExcepcionNegocio(
                        ExcepcionNegocio.Tipo.DATOS_INVALIDOS,
                        "Los productos y las cantidades de la entrada deben ser válidos.");
            }
        }

        try (Connection conn = ConexionDB.getConnection()) {

            conn.setAutoCommit(false);

            try {
                int idMovimiento =
                        movimientoDAO.insertarMovimientoTransaccional(
                                conn, mov);

                if (idMovimiento <= 0) {
                    throw new ExcepcionPersistencia(
                            "No se pudo registrar la cabecera de la entrada.");
                }

                for (DetalleMovimiento det : detalles) {

                    det.setIdMovimiento(idMovimiento);

                    Producto producto =
                            productoDAO.obtenerPorIdTransaccional(
                                    conn, det.getIdProducto());

                    if (producto == null) {
                        throw new ExcepcionNegocio(
                                ExcepcionNegocio.Tipo.PRODUCTO_NO_ENCONTRADO,
                                "El producto con ID " + det.getIdProducto()
                                + " no existe o no está disponible.");
                    }

                    int stockAnterior = producto.getStockActual();
                    int nuevoStock;

                    try {
                        nuevoStock = Math.addExact(
                                stockAnterior, det.getCantidad());
                    } catch (ArithmeticException ex) {
                        throw new ExcepcionNegocio(
                                ExcepcionNegocio.Tipo.DATOS_INVALIDOS,
                                "La cantidad ingresada supera el límite permitido.",
                                ex);
                    }

                    det.setStockAnterior(stockAnterior);
                    det.setStockResultante(nuevoStock);

                    if (!movimientoDAO.insertarDetalleMovTransaccional(
                            conn, det)) {
                        throw new ExcepcionPersistencia(
                                "No se pudo registrar el detalle de la entrada.");
                    }

                    String nuevaAlerta = evaluarEstadoAlerta(
                            nuevoStock, producto.getStockMinimo());

                    if (!productoDAO.actualizarStockTransaccional(
                            conn,
                            producto.getIdProducto(),
                            nuevoStock,
                            nuevaAlerta)) {
                        throw new ExcepcionPersistencia(
                                "No se pudo actualizar el stock del producto "
                                + producto.getNombre() + ".");
                    }
                }

                conn.commit();
                return true;

            } catch (ExcepcionNegocio ex) {
                rollbackSeguro(conn, ex);
                throw ex;

            } catch (ExcepcionPersistencia ex) {
                rollbackSeguro(conn, ex);
                throw ex;

            } catch (SQLException ex) {
                rollbackSeguro(conn, ex);
                throw new ExcepcionPersistencia(
                        "No se pudo registrar la entrada por un error en la base de datos.",
                        ex);

            } catch (RuntimeException ex) {
                rollbackSeguro(conn, ex);
                throw ex;
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE,
                    "Error al obtener la conexión para registrar la entrada.",
                    ex);

            throw new ExcepcionPersistencia(
                    "No se pudo establecer conexión con la base de datos.",
                    ex);
        }
    }

    /**
     * Registra un ajuste positivo o negativo de inventario.
     */
    public String registrarAjuste(
            MovimientoInventario mov) throws ExcepcionNegocio {

        if (mov == null) {
            throw new ExcepcionNegocio(
                    ExcepcionNegocio.Tipo.DATOS_INVALIDOS,
                    "No se recibió información del ajuste.");
        }

        List<DetalleMovimiento> detalles = mov.getDetalles();

        if (detalles == null || detalles.isEmpty()) {
            throw new ExcepcionNegocio(
                    ExcepcionNegocio.Tipo.DATOS_INVALIDOS,
                    "Debe agregar al menos un producto al ajuste.");
        }

        String tipoAjuste = mov.getTipoMovimiento();

        boolean esPositivo = "AJUSTE_POSITIVO".equals(tipoAjuste);
        boolean esNegativo = "AJUSTE_NEGATIVO".equals(tipoAjuste);

        if (!esPositivo && !esNegativo) {
            throw new ExcepcionNegocio(
                    ExcepcionNegocio.Tipo.DATOS_INVALIDOS,
                    "El tipo de ajuste no es válido.");
        }

        for (DetalleMovimiento det : detalles) {

            if (det == null) {
                throw new ExcepcionNegocio(
                        ExcepcionNegocio.Tipo.DATOS_INVALIDOS,
                        "Existe un detalle de producto inválido.");
            }

            if (det.getIdProducto() <= 0) {
                throw new ExcepcionNegocio(
                        ExcepcionNegocio.Tipo.DATOS_INVALIDOS,
                        "Seleccione un producto válido.");
            }

            if (det.getCantidad() <= 0) {
                throw new ExcepcionNegocio(
                        ExcepcionNegocio.Tipo.DATOS_INVALIDOS,
                        "Todas las cantidades deben ser mayores a cero.");
            }
        }

        try (Connection conn = ConexionDB.getConnection()) {

            conn.setAutoCommit(false);

            try {
                int idMovimiento =
                        movimientoDAO.insertarMovimientoTransaccional(
                                conn, mov);

                if (idMovimiento <= 0) {
                    throw new ExcepcionPersistencia(
                            "No se pudo registrar la cabecera del ajuste.");
                }

                for (DetalleMovimiento det : detalles) {

                    det.setIdMovimiento(idMovimiento);

                    Producto producto =
                            productoDAO.obtenerPorIdTransaccional(
                                    conn, det.getIdProducto());

                    if (producto == null) {
                        throw new ExcepcionNegocio(
                                ExcepcionNegocio.Tipo.PRODUCTO_NO_ENCONTRADO,
                                "El producto con ID " + det.getIdProducto()
                                + " no existe o no está disponible.");
                    }

                    int stockAnterior = producto.getStockActual();
                    int cantidadAjuste = det.getCantidad();

                    int variacion = esPositivo
                            ? cantidadAjuste
                            : -cantidadAjuste;

                    int nuevoStock;

                    try {
                        nuevoStock = Math.addExact(
                                stockAnterior, variacion);
                    } catch (ArithmeticException ex) {
                        throw new ExcepcionNegocio(
                                ExcepcionNegocio.Tipo.DATOS_INVALIDOS,
                                "La cantidad del ajuste supera el límite permitido.",
                                ex);
                    }

                    if (nuevoStock < 0) {
                        throw new ExcepcionNegocio(
                                ExcepcionNegocio.Tipo.STOCK_INSUFICIENTE,
                                "El ajuste dejaría el stock en negativo para el producto: "
                                + producto.getNombre()
                                + ". Stock actual: " + stockAnterior
                                + ", cantidad a descontar: "
                                + cantidadAjuste + ".");
                    }

                    det.setStockAnterior(stockAnterior);
                    det.setStockResultante(nuevoStock);
                    det.setCantidad(cantidadAjuste);

                    if (!movimientoDAO.insertarDetalleMovTransaccional(
                            conn, det)) {
                        throw new ExcepcionPersistencia(
                                "No se pudo registrar el detalle del ajuste en el historial.");
                    }

                    String nuevaAlerta = evaluarEstadoAlerta(
                            nuevoStock, producto.getStockMinimo());

                    if (!productoDAO.actualizarStockTransaccional(
                            conn,
                            producto.getIdProducto(),
                            nuevoStock,
                            nuevaAlerta)) {
                        throw new ExcepcionPersistencia(
                                "No se pudo actualizar el stock del producto "
                                + producto.getNombre() + ".");
                    }
                }

                conn.commit();

                return "EXITO: Ajuste registrado correctamente. "
                        + "El inventario ha sido actualizado.";

            } catch (ExcepcionNegocio ex) {
                rollbackSeguro(conn, ex);
                throw ex;

            } catch (ExcepcionPersistencia ex) {
                rollbackSeguro(conn, ex);
                throw ex;

            } catch (SQLException ex) {
                rollbackSeguro(conn, ex);
                throw new ExcepcionPersistencia(
                        "No se pudo completar el ajuste por un error en la base de datos.",
                        ex);

            } catch (RuntimeException ex) {
                rollbackSeguro(conn, ex);
                throw ex;
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE,
                    "Error al obtener la conexión para registrar el ajuste.",
                    ex);

            throw new ExcepcionPersistencia(
                    "No se pudo establecer conexión con la base de datos.",
                    ex);
        }
    }

    /**
     * Regla centralizada para determinar el estado de alerta.
     */
    public String evaluarEstadoAlerta(int stockActual, int stockMinimo) {

        if (stockActual == 0) {
            return "AGOTADO";
        }

        if (stockActual <= stockMinimo) {
            return "PROXIMO_A_AGOTARSE";
        }

        return "DISPONIBLE";
    }

    /**
     * Revierte la transacción sin ocultar la excepción original.
     */
    private void rollbackSeguro(Connection conn, Throwable causaOriginal) {

        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE,
                        "No se pudo revertir la transacción de inventario.",
                        ex);
                causaOriginal.addSuppressed(ex);
            }
        }
    }
}

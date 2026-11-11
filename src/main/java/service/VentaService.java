
package service;

import dao.ConexionDB;
import dao.MovimientoDAO;
import dao.ProductoDAO;
import dao.VentaDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.DetalleMovimiento;
import modelo.DetalleVenta;
import modelo.MovimientoInventario;
import modelo.Producto;
import modelo.Venta;

public class VentaService {

    private static final Logger LOGGER =
            Logger.getLogger(VentaService.class.getName());

    private final VentaDAO ventaDAO = new VentaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MovimientoDAO movimientoDAO = new MovimientoDAO();
    private final InventarioService inventarioService =
            new InventarioService();

    public boolean registrarVentaTransaccional(
            Venta venta,
            List<DetalleVenta> detalles,
            int idUsuario) throws ExcepcionNegocio {

        if (venta == null) {
            throw new ExcepcionNegocio(
                    ExcepcionNegocio.Tipo.DATOS_INVALIDOS,
                    "No se recibió la información de la venta.");
        }

        if (detalles == null || detalles.isEmpty()) {
            throw new ExcepcionNegocio(
                    ExcepcionNegocio.Tipo.DATOS_INVALIDOS,
                    "La venta debe contener al menos un producto.");
        }

        for (DetalleVenta det : detalles) {
            if (det == null
                    || det.getIdProducto() <= 0
                    || det.getCantidad() <= 0) {
                throw new ExcepcionNegocio(
                        ExcepcionNegocio.Tipo.DATOS_INVALIDOS,
                        "Los productos y las cantidades de la venta deben ser válidos.");
            }
        }

        Connection conn = null;

        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false);

            int idVenta = ventaDAO.insertarCabeceraTransaccional(
                    conn, venta);

            if (idVenta <= 0) {
                throw new ExcepcionPersistencia(
                        "No se pudo generar la cabecera de la venta.");
            }

            MovimientoInventario mov = new MovimientoInventario();
            mov.setCodigoMovimiento("S-" + System.currentTimeMillis());
            mov.setTipoMovimiento("SALIDA_VENTA");
            mov.setDocumentoReferencia(venta.getNumeroTicket());
            mov.setObservacion("Salida automática por venta en caja");
            mov.setIdUsuario(idUsuario);
            mov.setIdVentaOrigen(idVenta);

            int idMovimiento =
                    movimientoDAO.insertarMovimientoTransaccional(
                            conn, mov);

            if (idMovimiento <= 0) {
                throw new ExcepcionPersistencia(
                        "No se pudo registrar el movimiento de salida.");
            }

            for (DetalleVenta det : detalles) {

                det.setIdVenta(idVenta);

                if (!ventaDAO.insertarDetalleTransaccional(conn, det)) {
                    throw new ExcepcionPersistencia(
                            "No se pudo registrar el detalle de la venta.");
                }

                Producto producto = productoDAO.obtenerPorIdTransaccional(
                        conn, det.getIdProducto());

                if (producto == null) {
                    throw new ExcepcionNegocio(
                            ExcepcionNegocio.Tipo.PRODUCTO_NO_ENCONTRADO,
                            "El producto con ID " + det.getIdProducto()
                            + " no existe o no está disponible.");
                }

                if (producto.getStockActual() < det.getCantidad()) {
                    throw new ExcepcionNegocio(
                            ExcepcionNegocio.Tipo.STOCK_INSUFICIENTE,
                            "Stock insuficiente para el producto: "
                            + producto.getNombre()
                            + ". Stock disponible: "
                            + producto.getStockActual()
                            + ", cantidad solicitada: "
                            + det.getCantidad() + ".");
                }

                int stockAnterior = producto.getStockActual();
                int nuevoStock = stockAnterior - det.getCantidad();

                String nuevaAlerta =
                        inventarioService.evaluarEstadoAlerta(
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

                DetalleMovimiento detMov = new DetalleMovimiento();
                detMov.setIdMovimiento(idMovimiento);
                detMov.setIdProducto(producto.getIdProducto());
                detMov.setCantidad(det.getCantidad());
                detMov.setStockAnterior(stockAnterior);
                detMov.setStockResultante(nuevoStock);

                if (!movimientoDAO.insertarDetalleMovTransaccional(
                        conn, detMov)) {

                    throw new ExcepcionPersistencia(
                            "No se pudo registrar el detalle del movimiento en Kardex.");
                }
            }

            conn.commit();
            return true;

        } catch (ExcepcionNegocio ex) {

            rollbackSeguro(conn, ex);
            throw ex;

        } catch (ExcepcionPersistencia ex) {

            rollbackSeguro(conn, ex);
            LOGGER.log(Level.SEVERE,
                    "Error de persistencia al registrar la venta.", ex);
            throw ex;

        } catch (SQLException ex) {

            rollbackSeguro(conn, ex);
            LOGGER.log(Level.SEVERE,
                    "Error SQL al registrar la venta.", ex);

            throw new ExcepcionPersistencia(
                    "No se pudo completar la venta por un error en la base de datos.",
                    ex);

        } catch (RuntimeException ex) {

            rollbackSeguro(conn, ex);
            LOGGER.log(Level.SEVERE,
                    "Error inesperado al registrar la venta.", ex);

            throw ex;

        } finally {

            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    LOGGER.log(Level.WARNING,
                            "No se pudo restaurar el autocommit de la conexión.",
                            ex);
                }

                ConexionDB.cerrar(conn);
            }
        }
    }

    private void rollbackSeguro(Connection conn, Throwable causaOriginal) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE,
                        "No se pudo revertir la transacción de venta.", ex);
                causaOriginal.addSuppressed(ex);
            }
        }
    }
}

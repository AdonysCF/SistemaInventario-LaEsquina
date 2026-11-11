
package service;

import dao.ConexionDB;
import dao.MovimientoDAO;
import dao.ProductoDAO;
import dao.VentaDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.DetalleMovimiento;
import modelo.DetalleVenta;
import modelo.MovimientoInventario;
import modelo.Producto;
import modelo.Venta;

public class VentaService {

    private final VentaDAO ventaDAO = new VentaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MovimientoDAO movimientoDAO = new MovimientoDAO();
    private final InventarioService inventarioService = new InventarioService();

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

            // 1. Registrar la cabecera de venta.
            int idVenta = ventaDAO.insertarCabeceraTransaccional(conn, venta);

            if (idVenta <= 0) {
                conn.rollback();
                return false;
            }

            // 2. Registrar la cabecera del movimiento en Kardex.
            MovimientoInventario mov = new MovimientoInventario();
            mov.setCodigoMovimiento("S-" + System.currentTimeMillis());
            mov.setTipoMovimiento("SALIDA_VENTA");
            mov.setDocumentoReferencia(venta.getNumeroTicket());
            mov.setObservacion("Salida automática por venta en caja");
            mov.setIdUsuario(idUsuario);
            mov.setIdVentaOrigen(idVenta);

            int idMovimiento =
                    movimientoDAO.insertarMovimientoTransaccional(conn, mov);

            if (idMovimiento <= 0) {
                conn.rollback();
                return false;
            }

            // 3. Procesar los productos vendidos.
            for (DetalleVenta det : detalles) {

                det.setIdVenta(idVenta);

                if (!ventaDAO.insertarDetalleTransaccional(conn, det)) {
                    conn.rollback();
                    return false;
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

                // Utilizar el método centralizado de InventarioService.
                String nuevaAlerta = inventarioService.evaluarEstadoAlerta(
                        nuevoStock, producto.getStockMinimo());

                if (!productoDAO.actualizarStockTransaccional(
                        conn,
                        producto.getIdProducto(),
                        nuevoStock,
                        nuevaAlerta)) {

                    conn.rollback();
                    return false;
                }

                // 4. Registrar el detalle en Kardex.
                DetalleMovimiento detMov = new DetalleMovimiento();
                detMov.setIdMovimiento(idMovimiento);
                detMov.setIdProducto(producto.getIdProducto());
                detMov.setCantidad(det.getCantidad());
                detMov.setStockAnterior(stockAnterior);
                detMov.setStockResultante(nuevoStock);

                if (!movimientoDAO.insertarDetalleMovTransaccional(
                        conn, detMov)) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;

        } catch (ExcepcionNegocio ex) {

            rollbackSeguro(conn);
            throw ex;

        } catch (Exception ex) {

            rollbackSeguro(conn);
            ex.printStackTrace();
            return false;

        } finally {

            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    ConexionDB.cerrar(conn);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void rollbackSeguro(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}

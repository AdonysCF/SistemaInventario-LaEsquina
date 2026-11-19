package service;

import dao.ConexionDB;
import dao.MovimientoDAO;
import dao.ProductoDAO;
import dao.VentaDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.DetalleMovimiento;
import modelo.MovimientoInventario;
import modelo.Producto;
import service.ExcepcionNegocio.Tipo;

public class AnulacionVentaService {

    private final VentaDAO ventaDAO = new VentaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MovimientoDAO movimientoDAO = new MovimientoDAO();
    private final InventarioService inventarioService = new InventarioService();

    public void anularVenta(int idVenta, int idGerente, String motivo) throws ExcepcionNegocio {
        String motivoLimpio = motivo == null ? "" : motivo.strip();
        if (idVenta <= 0 || idGerente <= 0) {
            throw new ExcepcionNegocio(Tipo.DATOS_INVALIDOS, "La venta o el responsable no son válidos.");
        }
        if (motivoLimpio.isBlank() || motivoLimpio.length() > 250) {
            throw new ExcepcionNegocio(Tipo.DATOS_INVALIDOS,
                    "Ingresa un motivo de anulación de 1 a 250 caracteres.");
        }

        try (Connection conn = ConexionDB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                anularEnTransaccion(conn, idVenta, idGerente, motivoLimpio);
                conn.commit();
            } catch (SQLException | ExcepcionNegocio | RuntimeException ex) {
                try {
                    conn.rollback();
                } catch (SQLException errorRollback) {
                    ex.addSuppressed(errorRollback);
                }
                throw ex;
            }
        } catch (SQLException ex) {
            throw new ExcepcionPersistencia("No se pudo completar la anulación de la venta.", ex);
        }
    }

    private void anularEnTransaccion(Connection conn, int idVenta, int idGerente, String motivo)
            throws SQLException, ExcepcionNegocio {
        var venta = ventaDAO.bloquearVentaParaAnular(conn, idVenta).orElseThrow(
                () -> new ExcepcionNegocio(Tipo.OPERACION_NO_PERMITIDA, "No se encontró la venta."));
        if (!"CONFIRMADA".equals(venta.estado())) {
            throw new ExcepcionNegocio(Tipo.OPERACION_NO_PERMITIDA,
                    "Solo se pueden anular ventas confirmadas.");
        }

        List<VentaDAO.CantidadVendida> cantidades = ventaDAO.listarCantidadesVendidas(conn, idVenta);
        if (cantidades.isEmpty()) {
            throw new ExcepcionNegocio(Tipo.OPERACION_NO_PERMITIDA,
                    "La venta no tiene productos registrados para devolver al stock.");
        }

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setCodigoMovimiento("ANUL-V-" + idVenta);
        movimiento.setTipoMovimiento("ANULACION");
        movimiento.setDocumentoReferencia(venta.ticket());
        movimiento.setObservacion(motivo);
        movimiento.setIdUsuario(idGerente);
        movimiento.setIdVentaOrigen(idVenta);
        int idMovimiento = movimientoDAO.insertarMovimientoTransaccional(conn, movimiento);
        if (idMovimiento <= 0) {
            throw new SQLException("No se obtuvo el identificador del movimiento de anulación.");
        }

        for (VentaDAO.CantidadVendida linea : cantidades) {
            devolverProducto(conn, idMovimiento, linea);
        }
        if (!ventaDAO.marcarAnuladaTransaccional(conn, idVenta)) {
            throw new SQLException("No se pudo cambiar el estado de la venta confirmada.");
        }
    }

    private void devolverProducto(Connection conn, int idMovimiento, VentaDAO.CantidadVendida linea)
            throws SQLException, ExcepcionNegocio {
        Producto producto = productoDAO.obtenerPorIdTransaccional(conn, linea.idProducto());
        if (producto == null) {
            throw new ExcepcionNegocio(Tipo.PRODUCTO_NO_ENCONTRADO,
                    "No se encontró uno de los productos de la venta.");
        }

        int stockAnterior = producto.getStockActual();
        if (linea.cantidad() <= 0 || linea.cantidad() > Integer.MAX_VALUE - (long) stockAnterior) {
            throw new ExcepcionNegocio(Tipo.OPERACION_NO_PERMITIDA,
                    "La cantidad de la venta supera el límite de stock permitido para el producto "
                            + producto.getCodigoSku() + ".");
        }
        int stockResultante = stockAnterior + (int) linea.cantidad();
        String alerta = inventarioService.evaluarEstadoAlerta(stockResultante, producto.getStockMinimo());
        if (!productoDAO.actualizarStockTransaccional(conn, linea.idProducto(), stockResultante, alerta)) {
            throw new SQLException("No se pudo devolver el stock del producto " + linea.idProducto() + ".");
        }

        DetalleMovimiento detalle = new DetalleMovimiento();
        detalle.setIdMovimiento(idMovimiento);
        detalle.setIdProducto(linea.idProducto());
        detalle.setCantidad((int) linea.cantidad());
        detalle.setStockAnterior(stockAnterior);
        detalle.setStockResultante(stockResultante);
        if (!movimientoDAO.insertarDetalleMovTransaccional(conn, detalle)) {
            throw new SQLException("No se pudo registrar el detalle de la anulación.");
        }
    }
}

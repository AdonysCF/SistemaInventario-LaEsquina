package service;

import dao.ConexionDB;
import dao.MovimientoDAO;
import dao.ProductoDAO;
import dao.VentaDAO;
import java.sql.Connection;
import modelo.DetalleMovimiento;
import modelo.DetalleVenta;
import modelo.MovimientoInventario;
import modelo.Producto;
import modelo.Venta;

public class VentaService {
    
    private VentaDAO ventaDAO = new VentaDAO();
    private ProductoDAO productoDAO = new ProductoDAO();
    private MovimientoDAO movimientoDAO = new MovimientoDAO();

    public String registrarVenta(Venta venta) {
        Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false); // INICIA TRANSACCIÓN

            // 1. Registrar Cabecera de Venta
            int idVenta = ventaDAO.insertarCabecera(conn, venta);

            // 2. Preparar cabecera para el Kardex
            MovimientoInventario mov = new MovimientoInventario();
            mov.setCodigoMovimiento("SAL-" + System.currentTimeMillis());
            mov.setTipoMovimiento("SALIDA_VENTA");
            mov.setDocumentoReferencia(venta.getNumeroTicket());
            mov.setObservacion("Venta en mostrador");
            mov.setIdUsuario(venta.getIdUsuario());
            int idMov = movimientoDAO.insertarMovimiento(conn, mov);

            // 3. Procesar cada detalle
            for (DetalleVenta det : venta.getDetalles()) {
                Producto prod = productoDAO.obtenerPorId(conn, det.getIdProducto());
                
                int stockActual = prod.getStockActual();
                
                // VALIDACIÓN CRUCIAL RN-02: BLOQUEO POR STOCK INSUFICIENTE
                if (stockActual < det.getCantidad()) {
                    conn.rollback();
                    return "ERROR: Stock insuficiente para el producto ID " + prod.getIdProducto() + ". Quedan " + stockActual + " unidades.";
                }

                int nuevoStock = stockActual - det.getCantidad();

                // Insertar detalle de venta
                ventaDAO.insertarDetalle(conn, idVenta, det);

                // Insertar en Kardex
                DetalleMovimiento detMov = new DetalleMovimiento();
                detMov.setIdProducto(det.getIdProducto());
                detMov.setCantidad(det.getCantidad());
                detMov.setStockAnterior(stockActual);
                detMov.setStockResultante(nuevoStock);
                movimientoDAO.insertarDetalleMov(conn, idMov, detMov);

                // Evaluar RN-04 y actualizar tabla producto
                String alerta = "DISPONIBLE";
                if (nuevoStock == 0) alerta = "AGOTADO";
                else if (nuevoStock <= prod.getStockMinimo()) alerta = "PROXIMO_A_AGOTARSE";
                
                productoDAO.actualizarStock(conn, det.getIdProducto(), nuevoStock, alerta);
            }

            conn.commit(); // CONFIRMA TODO
            return "EXITO:" + venta.getNumeroTicket();

        } catch (Exception e) {
            if (conn != null) { try { conn.rollback(); } catch (Exception ex) {} }
            return "ERROR: Fallo transaccional de base de datos - " + e.getMessage();
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (Exception e) {} }
        }
    }
}
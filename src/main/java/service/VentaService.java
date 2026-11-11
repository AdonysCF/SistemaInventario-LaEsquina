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

    private VentaDAO ventaDAO = new VentaDAO();
    private ProductoDAO productoDAO = new ProductoDAO();
    private MovimientoDAO movimientoDAO = new MovimientoDAO();

    public boolean registrarVentaTransaccional(Venta venta, List<DetalleVenta> detalles, int idUsuario) {
        Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            // 1. INICIAR TRANSACCIÓN ATÓMICA (AC-02)
            conn.setAutoCommit(false); 

            // 2. Insertar Cabecera de Venta
            int idVenta = ventaDAO.insertarCabeceraTransaccional(conn, venta);
            if (idVenta <= 0) {
                conn.rollback();
                return false;
            }

            // 3. Crear cabecera del Kardex (Movimiento)
            MovimientoInventario mov = new MovimientoInventario();
            mov.setCodigoMovimiento("S-" + System.currentTimeMillis()); // Código único
            mov.setTipoMovimiento("SALIDA_VENTA");
            mov.setDocumentoReferencia(venta.getNumeroTicket());
            mov.setObservacion("Salida automática por venta en caja");
            mov.setIdUsuario(idUsuario);
            mov.setIdVentaOrigen(idVenta);
            
            int idMovimiento = movimientoDAO.insertarMovimientoTransaccional(conn, mov);
            if (idMovimiento <= 0) {
                conn.rollback();
                return false;
            }

            // 4. Procesar cada detalle (ítem vendido)
            for (DetalleVenta det : detalles) {
                det.setIdVenta(idVenta);
                
                // A. Guardar detalle de venta
                if (!ventaDAO.insertarDetalleTransaccional(conn, det)) {
                    conn.rollback();
                    return false;
                }

                // B. Verificar Stock Real (RN-02)
                Producto p = productoDAO.obtenerPorIdTransaccional(conn, det.getIdProducto());
                if (p == null || p.getStockActual() < det.getCantidad()) {
                    conn.rollback(); // Si no hay stock, deshacer TODO
                    return false; 
                }

                int stockAnterior = p.getStockActual();
                int nuevoStock = stockAnterior - det.getCantidad();

                // C. Actualizar Stock y evaluar alerta (RN-04)
                String nuevaAlerta = evaluarEstadoAlerta(nuevoStock, p.getStockMinimo());
                if (!productoDAO.actualizarStockTransaccional(conn, p.getIdProducto(), nuevoStock, nuevaAlerta)) {
                    conn.rollback();
                    return false;
                }

                // D. Guardar detalle en el Kardex
                DetalleMovimiento detMov = new DetalleMovimiento();
                detMov.setIdMovimiento(idMovimiento);
                detMov.setIdProducto(p.getIdProducto());
                detMov.setCantidad(det.getCantidad());
                detMov.setStockAnterior(stockAnterior);
                detMov.setStockResultante(nuevoStock);
                
                if (!movimientoDAO.insertarDetalleMovTransaccional(conn, detMov)) {
                    conn.rollback();
                    return false;
                }
            }

            // 5. CONFIRMAR TRANSACCIÓN (Si llegó aquí, todo salió perfecto)
            conn.commit();
            return true;

        } catch (Exception e) {
            try {
                if (conn != null) {
                    // DESHACER TODO SI OCURRE UN ERROR IMPREVISTO
                    conn.rollback(); 
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            // 6. Restablecer el autocommit y devolver la conexión al Pool
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    ConexionDB.cerrar(conn);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private String evaluarEstadoAlerta(int stockActual, int stockMinimo) {
        if (stockActual == 0) return "AGOTADO";
        if (stockActual <= stockMinimo) return "PROXIMO_A_AGOTARSE";
        return "DISPONIBLE";
    }
}
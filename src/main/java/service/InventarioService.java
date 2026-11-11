package service;

import dao.ConexionDB;
import dao.MovimientoDAO;
import dao.ProductoDAO;
import java.sql.Connection;
import modelo.DetalleMovimiento;
import modelo.MovimientoInventario;
import modelo.Producto;

public class InventarioService {
    
    private MovimientoDAO movimientoDAO = new MovimientoDAO();
    private ProductoDAO productoDAO = new ProductoDAO();

    public boolean registrarEntrada(MovimientoInventario mov) {
        Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false); // ¡INICIA TRANSACCIÓN ACID!

            // 1. Insertar cabecera del movimiento
            int idMovimiento = movimientoDAO.insertarMovimiento(conn, mov);

            // 2. Procesar cada detalle recibido
            for (DetalleMovimiento det : mov.getDetalles()) {
                // Leer stock actual bloqueando la fila (si tuvieras FOR UPDATE)
                Producto prod = productoDAO.obtenerPorId(conn, det.getIdProducto());
                
                int stockAnterior = prod.getStockActual();
                int nuevoStock = stockAnterior + det.getCantidad();
                
                // Actualizar el detalle para el Kardex
                det.setStockAnterior(stockAnterior);
                det.setStockResultante(nuevoStock);
                
                // Insertar detalle en Kardex
                movimientoDAO.insertarDetalleMov(conn, idMovimiento, det);
                
                // Evaluar regla RN-04 (Alerta de stock)
                String alerta = "DISPONIBLE";
                if (nuevoStock == 0) alerta = "AGOTADO";
                else if (nuevoStock <= prod.getStockMinimo()) alerta = "PROXIMO_A_AGOTARSE";
                
                // Actualizar stock real en la tabla producto
                productoDAO.actualizarStock(conn, det.getIdProducto(), nuevoStock, alerta);
            }

            conn.commit(); // CONFIRMA TRANSACCIÓN
            return true;

        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) {} // DESHACE TODO SI HAY ERROR
            }
            System.out.println("Error transaccional en entrada: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (Exception e) {}
            }
        }
    }
    
    // Método para RF-NEG-04: Ajuste de Inventario
    public String registrarAjuste(MovimientoInventario mov) {
        Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false); // Inicia Transacción ACID

            // 1. Insertar cabecera del ajuste (AJUSTE_POSITIVO o AJUSTE_NEGATIVO)
            int idMovimiento = movimientoDAO.insertarMovimiento(conn, mov);

            // 2. Procesar el detalle del ajuste
            for (DetalleMovimiento det : mov.getDetalles()) {
                Producto prod = productoDAO.obtenerPorId(conn, det.getIdProducto());
                int stockActual = prod.getStockActual();
                int nuevoStock;

                if (mov.getTipoMovimiento().equals("AJUSTE_NEGATIVO")) {
                    if (stockActual < det.getCantidad()) {
                        conn.rollback();
                        return "ERROR: No puede ajustar negativamente más stock del que existe. Quedan " + stockActual + " unidades.";
                    }
                    nuevoStock = stockActual - det.getCantidad();
                } else {
                    nuevoStock = stockActual + det.getCantidad();
                }

                // Configurar saldos para el Kardex
                det.setStockAnterior(stockActual);
                det.setStockResultante(nuevoStock);

                // Insertar detalle en Kardex
                movimientoDAO.insertarDetalleMov(conn, idMovimiento, det);

                // Evaluar RN-04 (Alerta de stock)
                String alerta = "DISPONIBLE";
                if (nuevoStock == 0) alerta = "AGOTADO";
                else if (nuevoStock <= prod.getStockMinimo()) alerta = "PROXIMO_A_AGOTARSE";

                // Actualizar stock en la tabla producto
                productoDAO.actualizarStock(conn, det.getIdProducto(), nuevoStock, alerta);
            }

            conn.commit(); // CONFIRMA TRANSACCIÓN
            return "EXITO:" + mov.getCodigoMovimiento();

        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) {}
            }
            return "ERROR: Fallo transaccional - " + e.getMessage();
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (Exception e) {}
            }
        }
    }
}
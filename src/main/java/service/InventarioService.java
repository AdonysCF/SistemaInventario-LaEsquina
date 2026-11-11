package service;

import dao.ConexionDB;
import dao.MovimientoDAO;
import dao.ProductoDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.DetalleMovimiento;
import modelo.MovimientoInventario;
import modelo.Producto;

public class InventarioService {

    private MovimientoDAO movimientoDAO = new MovimientoDAO();
    private ProductoDAO productoDAO = new ProductoDAO();

    public boolean registrarEntradaTransaccional(MovimientoInventario mov, List<DetalleMovimiento> detalles) {
        Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            // 1. INICIAR TRANSACCIÓN ATÓMICA
            conn.setAutoCommit(false);

            // 2. Insertar Cabecera del Movimiento en Kardex
            int idMovimiento = movimientoDAO.insertarMovimientoTransaccional(conn, mov);
            if (idMovimiento <= 0) {
                conn.rollback();
                return false;
            }

            // 3. Procesar cada producto ingresado
            for (DetalleMovimiento det : detalles) {
                det.setIdMovimiento(idMovimiento);
                
                // Obtener stock actual para calcular el resultante
                Producto p = productoDAO.obtenerPorIdTransaccional(conn, det.getIdProducto());
                if (p == null) {
                    conn.rollback();
                    return false;
                }

                int stockAnterior = p.getStockActual();
                int nuevoStock = stockAnterior + det.getCantidad();
                
                det.setStockAnterior(stockAnterior);
                det.setStockResultante(nuevoStock);

                // A. Insertar detalle en Kardex
                if (!movimientoDAO.insertarDetalleMovTransaccional(conn, det)) {
                    conn.rollback();
                    return false;
                }

                // B. Actualizar Stock y levantar alertas (RN-04)
                String nuevaAlerta = evaluarEstadoAlerta(nuevoStock, p.getStockMinimo());
                if (!productoDAO.actualizarStockTransaccional(conn, p.getIdProducto(), nuevoStock, nuevaAlerta)) {
                    conn.rollback();
                    return false;
                }
            }

            // 4. CONFIRMAR TRANSACCIÓN
            conn.commit();
            return true;

        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
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
    
    public String registrarAjuste(MovimientoInventario mov) {
        Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false); // Iniciar transacción ACID[cite: 117.2, 117.5]

            // 1. Registrar cabecera del ajuste
            int idMov = movimientoDAO.insertarMovimientoTransaccional(conn, mov);
            if (idMov <= 0) {
                conn.rollback();
                return "Error al registrar la cabecera del ajuste.";
            }

            // 2. Procesar cada detalle (producto afectado por el ajuste)
            for (DetalleMovimiento det : mov.getDetalles()) {
                det.setIdMovimiento(idMov);
                
                // Obtener producto bloqueando la fila
                Producto p = productoDAO.obtenerPorIdTransaccional(conn, det.getIdProducto());
                if (p == null) {
                    conn.rollback();
                    return "Error: Uno de los productos no existe.";
                }

                int stockAnterior = p.getStockActual();
                int cantidadAjuste = det.getCantidad(); // En ajuste, la cantidad puede ser positiva o negativa
                int nuevoStock = stockAnterior + cantidadAjuste;

                // Validación de stock negativo (RN-02)
                if (nuevoStock < 0) {
                    conn.rollback();
                    return "El ajuste dejaría el stock en negativo para el producto: " + p.getNombre();
                }

                // Llenar datos faltantes del detalle
                det.setStockAnterior(stockAnterior);
                det.setStockResultante(nuevoStock);
                // Si la cantidad es negativa (salida), para el kardex guardamos el valor absoluto
                det.setCantidad(Math.abs(cantidadAjuste)); 

                // A. Insertar detalle en Kardex
                if (!movimientoDAO.insertarDetalleMovTransaccional(conn, det)) {
                    conn.rollback();
                    return "Error al registrar el detalle en el historial.";
                }

                // B. Actualizar stock del producto (RN-04)
                String nuevaAlerta = evaluarEstadoAlerta(nuevoStock, p.getStockMinimo());
                if (!productoDAO.actualizarStockTransaccional(conn, p.getIdProducto(), nuevoStock, nuevaAlerta)) {
                    conn.rollback();
                    return "Error al actualizar el stock del producto.";
                }
            }

            // 3. Confirmar todo[cite: 117.2, 117.5]
            conn.commit();
            return "OK";

        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback(); //[cite: 117.2, 117.5]
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return "Error inesperado: " + e.getMessage();
        } finally {
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
    
}
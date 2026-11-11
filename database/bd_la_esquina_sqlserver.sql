SET NOCOUNT ON;
GO

IF DB_ID('bd_la_esquina') IS NULL
BEGIN
    CREATE DATABASE bd_la_esquina;
END
GO

USE bd_la_esquina;
GO

IF OBJECT_ID('dbo.detalle_movimiento', 'U') IS NOT NULL DROP TABLE dbo.detalle_movimiento;
IF OBJECT_ID('dbo.movimiento_inventario', 'U') IS NOT NULL DROP TABLE dbo.movimiento_inventario;
IF OBJECT_ID('dbo.detalle_venta', 'U') IS NOT NULL DROP TABLE dbo.detalle_venta;
IF OBJECT_ID('dbo.venta', 'U') IS NOT NULL DROP TABLE dbo.venta;
IF OBJECT_ID('dbo.producto', 'U') IS NOT NULL DROP TABLE dbo.producto;
IF OBJECT_ID('dbo.categoria', 'U') IS NOT NULL DROP TABLE dbo.categoria;
IF OBJECT_ID('dbo.usuario', 'U') IS NOT NULL DROP TABLE dbo.usuario;
GO

-- TABLA 1: usuario 

CREATE TABLE dbo.usuario (
    id_usuario INT IDENTITY(1,1) NOT NULL,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(120) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    activo BIT NOT NULL CONSTRAINT df_usuario_activo DEFAULT (1),
    fecha_creacion DATETIME NOT NULL CONSTRAINT df_usuario_fecha DEFAULT (GETDATE()),
    CONSTRAINT pk_usuario PRIMARY KEY CLUSTERED (id_usuario),
    CONSTRAINT uq_usuario_username UNIQUE (username),
    CONSTRAINT chk_usuario_rol CHECK (rol IN ('CAJERO', 'GERENTE'))
);
GO

-- TABLA 2: categoria 

CREATE TABLE dbo.categoria (
    id_categoria INT IDENTITY(1,1) NOT NULL,
    nombre VARCHAR(80) NOT NULL,
    descripcion VARCHAR(200) NULL,
    activo BIT NOT NULL CONSTRAINT df_categoria_activo DEFAULT (1),
    CONSTRAINT pk_categoria PRIMARY KEY CLUSTERED (id_categoria),
    CONSTRAINT uq_categoria_nombre UNIQUE (nombre)
);
GO


-- TABLA 3: producto 

CREATE TABLE dbo.producto (
    id_producto INT IDENTITY(1,1) NOT NULL,
    codigo_sku VARCHAR(30) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    id_categoria INT NOT NULL,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    stock_actual INT NOT NULL CONSTRAINT df_producto_stock DEFAULT (0),
    stock_minimo INT NOT NULL CONSTRAINT df_producto_minimo DEFAULT (5),
    estado_alerta VARCHAR(30) NOT NULL CONSTRAINT df_producto_alerta DEFAULT ('DISPONIBLE'),
    activo BIT NOT NULL CONSTRAINT df_producto_activo DEFAULT (1),
    fecha_actualizacion DATETIME NOT NULL CONSTRAINT df_producto_fecha DEFAULT (GETDATE()),
    CONSTRAINT pk_producto PRIMARY KEY CLUSTERED (id_producto),
    CONSTRAINT uq_producto_sku UNIQUE (codigo_sku),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria)
        REFERENCES dbo.categoria (id_categoria) ON UPDATE CASCADE ON DELETE NO ACTION,
    CONSTRAINT chk_producto_precio CHECK (precio_unitario > 0),
    CONSTRAINT chk_producto_stock_no_negativo CHECK (stock_actual >= 0),
    CONSTRAINT chk_producto_stock_minimo CHECK (stock_minimo >= 0),
    CONSTRAINT chk_producto_estado_alerta CHECK (estado_alerta IN ('DISPONIBLE', 'PROXIMO_A_AGOTARSE', 'AGOTADO'))
);
GO

CREATE NONCLUSTERED INDEX idx_producto_nombre ON dbo.producto (nombre);
CREATE NONCLUSTERED INDEX idx_producto_categoria ON dbo.producto (id_categoria);
CREATE NONCLUSTERED INDEX idx_producto_alerta ON dbo.producto (estado_alerta);
GO


-- TABLA 4: venta 

CREATE TABLE dbo.venta (
    id_venta INT IDENTITY(1,1) NOT NULL,
    numero_ticket VARCHAR(25) NOT NULL,
    fecha_hora DATETIME NOT NULL CONSTRAINT df_venta_fecha DEFAULT (GETDATE()),
    total_venta DECIMAL(10, 2) NOT NULL,
    estado VARCHAR(20) NOT NULL CONSTRAINT df_venta_estado DEFAULT ('CONFIRMADA'),
    id_usuario INT NOT NULL,
    CONSTRAINT pk_venta PRIMARY KEY CLUSTERED (id_venta),
    CONSTRAINT uq_venta_ticket UNIQUE (numero_ticket),
    CONSTRAINT fk_venta_usuario FOREIGN KEY (id_usuario)
        REFERENCES dbo.usuario (id_usuario) ON UPDATE CASCADE ON DELETE NO ACTION,
    CONSTRAINT chk_venta_total CHECK (total_venta >= 0),
    CONSTRAINT chk_venta_estado CHECK (estado IN ('CONFIRMADA', 'ANULADA'))
);
GO

CREATE NONCLUSTERED INDEX idx_venta_fecha ON dbo.venta (fecha_hora);
GO

-- TABLA 5: detalle_venta

CREATE TABLE dbo.detalle_venta (
    id_detalle_venta INT IDENTITY(1,1) NOT NULL,
    id_venta INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    CONSTRAINT pk_detalle_venta PRIMARY KEY CLUSTERED (id_detalle_venta),
    CONSTRAINT fk_detalle_venta_cabecera FOREIGN KEY (id_venta)
        REFERENCES dbo.venta (id_venta) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_detalle_venta_producto FOREIGN KEY (id_producto)
        REFERENCES dbo.producto (id_producto) ON UPDATE CASCADE ON DELETE NO ACTION,
    CONSTRAINT chk_detalle_venta_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_venta_subtotal CHECK (subtotal >= 0)
);
GO

-- TABLA 6: movimiento_inventario

CREATE TABLE dbo.movimiento_inventario (
    id_movimiento INT IDENTITY(1,1) NOT NULL,
    codigo_movimiento VARCHAR(25) NOT NULL,
    tipo_movimiento VARCHAR(30) NOT NULL,
    documento_referencia VARCHAR(60) NOT NULL,
    fecha_hora DATETIME NOT NULL CONSTRAINT df_movimiento_fecha DEFAULT (GETDATE()),
    observacion VARCHAR(250) NULL,
    id_usuario INT NOT NULL,
    id_venta_origen INT NULL,
    CONSTRAINT pk_movimiento_inventario PRIMARY KEY CLUSTERED (id_movimiento),
    CONSTRAINT uq_movimiento_codigo UNIQUE (codigo_movimiento),
    CONSTRAINT fk_movimiento_usuario FOREIGN KEY (id_usuario)
        REFERENCES dbo.usuario (id_usuario) ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT fk_movimiento_venta FOREIGN KEY (id_venta_origen)
        REFERENCES dbo.venta (id_venta) ON UPDATE NO ACTION ON DELETE SET NULL,
    CONSTRAINT chk_movimiento_tipo CHECK (tipo_movimiento IN ('ENTRADA_REPOSICION', 'SALIDA_VENTA', 'AJUSTE_POSITIVO', 'AJUSTE_NEGATIVO', 'ANULACION'))
);
GO

CREATE NONCLUSTERED INDEX idx_movimiento_fecha ON dbo.movimiento_inventario (fecha_hora);
CREATE NONCLUSTERED INDEX idx_movimiento_tipo ON dbo.movimiento_inventario (tipo_movimiento);
GO


-- TABLA 7: detalle_movimiento 

CREATE TABLE dbo.detalle_movimiento (
    id_detalle_mov INT IDENTITY(1,1) NOT NULL,
    id_movimiento INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    stock_anterior INT NOT NULL,
    stock_resultante INT NOT NULL,
    CONSTRAINT pk_detalle_movimiento PRIMARY KEY CLUSTERED (id_detalle_mov),
    CONSTRAINT fk_det_mov_cabecera FOREIGN KEY (id_movimiento)
        REFERENCES dbo.movimiento_inventario (id_movimiento) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_det_mov_producto FOREIGN KEY (id_producto)
        REFERENCES dbo.producto (id_producto) ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT chk_det_mov_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_det_mov_stock_resultante CHECK (stock_resultante >= 0)
);
GO


-- INSERCIÓN DE DATOS DE PRUEBA 


-- 1. Datos de prueba para TABLA: usuario (2 registros)
INSERT INTO dbo.usuario (username, password_hash, nombre_completo, rol) VALUES
('gerente.esquina', '$2a$10$hashGerenteBCryptEjemplo123456', 'Carlos Mendoza (Gerente)', 'GERENTE'),
('cajero.turno1',   '$2a$10$hashCajero1BCryptEjemplo123456', 'María Torres (Cajera)',    'CAJERO');

-- 2. Datos de prueba para TABLA: categoria (3 registros)
INSERT INTO dbo.categoria (nombre, descripcion) VALUES
('Abarrotes', 'Arroz, azúcar, aceites y productos básicos de despensa'),
('Lácteos',   'Leche evaporada, yogures y derivados lácteos'),
('Bebidas',   'Agua mineral, gaseosas y jugos embotellados');

-- 3. Datos de prueba para TABLA: producto (3 registros en los 3 estados del semáforo RN-04)
INSERT INTO dbo.producto (codigo_sku, nombre, id_categoria, precio_unitario, stock_actual, stock_minimo, estado_alerta) VALUES
('ABA-0001', 'Arroz Extra Costeño Bolsa 750g',       1, 4.20, 45, 10, 'DISPONIBLE'),
('LAC-0001', 'Leche Evaporada Gloria Azul Lata 390g', 2, 4.50,  8, 10, 'PROXIMO_A_AGOTARSE'),
('BEB-0001', 'Agua Mineral San Mateo Sin Gas 600ml',  3, 2.00,  0, 12, 'AGOTADO');

-- 4. Datos de prueba para TABLA: venta (1 registro)
INSERT INTO dbo.venta (numero_ticket, total_venta, estado, id_usuario) VALUES
('V-00001', 12.60, 'CONFIRMADA', 2);

-- 5. Datos de prueba para TABLA: detalle_venta (1 registro)
INSERT INTO dbo.detalle_venta (id_venta, id_producto, cantidad, precio_unitario, subtotal) VALUES
(1, 1, 3, 4.20, 12.60);

-- 6. Datos de prueba para TABLA: movimiento_inventario (2 registros: 1 entrada y 1 salida)
INSERT INTO dbo.movimiento_inventario (codigo_movimiento, tipo_movimiento, documento_referencia, observacion, id_usuario, id_venta_origen) VALUES
('E-00001', 'ENTRADA_REPOSICION', 'Guía N.° 001-004582', 'Abastecimiento inicial de arroz', 1, NULL),
('S-00001', 'SALIDA_VENTA',       'Ticket V-00001',      'Salida automática por venta en caja', 2, 1);

-- 7. Datos de prueba para TABLA: detalle_movimiento (2 registros)
INSERT INTO dbo.detalle_movimiento (id_movimiento, id_producto, cantidad, stock_anterior, stock_resultante) VALUES
(1, 1, 48,  0, 48),
(2, 1,  3, 48, 45);
GO


-- CONSULTAS DE CADA TABLA

SELECT '1. TABLA: usuario' AS [Indicador_Tabla], id_usuario, username, nombre_completo, rol, activo FROM dbo.usuario;
SELECT '2. TABLA: categoria' AS [Indicador_Tabla], id_categoria, nombre, descripcion, activo FROM dbo.categoria;
SELECT '3. TABLA: producto' AS [Indicador_Tabla], id_producto, codigo_sku, nombre, precio_unitario, stock_actual, stock_minimo, estado_alerta FROM dbo.producto;
SELECT '4. TABLA: venta' AS [Indicador_Tabla], id_venta, numero_ticket, fecha_hora, total_venta, estado, id_usuario FROM dbo.venta;
SELECT '5. TABLA: detalle_venta' AS [Indicador_Tabla], id_detalle_venta, id_venta, id_producto, cantidad, precio_unitario, subtotal FROM dbo.detalle_venta;
SELECT '6. TABLA: movimiento_inventario' AS [Indicador_Tabla], id_movimiento, codigo_movimiento, tipo_movimiento, documento_referencia, id_usuario, id_venta_origen FROM dbo.movimiento_inventario;
SELECT '7. TABLA: detalle_movimiento' AS [Indicador_Tabla], id_detalle_mov, id_movimiento, id_producto, cantidad, stock_anterior, stock_resultante FROM dbo.detalle_movimiento;
GO

DELETE FROM detalle_movimiento;
DELETE FROM movimiento_inventario;
DELETE FROM detalle_venta;
DELETE FROM venta;
DELETE FROM usuario;
GO
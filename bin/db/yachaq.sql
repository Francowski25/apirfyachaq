-- =====================================================================
-- BASE DE DATOS: YACHAQ MINERO
-- Motor: MySQL 8.0+
-- =====================================================================

CREATE DATABASE IF NOT EXISTS yachaq_minero_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE yachaq_minero_db;

-- =====================================================================
-- 1. TABLA DE USUARIOS
-- =====================================================================
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    clave_hash VARCHAR(255) NOT NULL, -- NUNCA guardar texto plano
    rol ENUM('ADMIN', 'MINERO') DEFAULT 'MINERO',
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE
);

-- =====================================================================
-- 2. TABLA DE SUSCRIPCIONES (Google Play Billing)
-- =====================================================================
CREATE TABLE suscripciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    google_purchase_token VARCHAR(500) NOT NULL, -- Token que devuelve Google Play
    google_product_id VARCHAR(100) NOT NULL,     -- Ej: 'premium_mensual'
    estado ENUM('ACTIVA', 'CANCELADA', 'EXPIRADA', 'EN_GRACIA') DEFAULT 'ACTIVA',
    fecha_inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion TIMESTAMP NULL,
    renovacion_automatica BOOLEAN DEFAULT TRUE,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- =====================================================================
-- 3. TABLA DE HISTORIAL DE CÁLCULOS (Liquidaciones)
-- =====================================================================
CREATE TABLE historial_calculos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    material VARCHAR(50) NOT NULL, -- 'Cobre', 'Oro', 'Plata'
    tonelaje DECIMAL(10, 2) NOT NULL,
    ley DECIMAL(10, 4) NOT NULL,
    precio DECIMAL(10, 4) NOT NULL,
    alquiler_planta DECIMAL(10, 2) NOT NULL,
    
    -- Resultados principales
    tm_humedas DECIMAL(10, 2) NOT NULL,
    tmns DECIMAL(10, 2) NOT NULL,
    pago_total DECIMAL(12, 2) NOT NULL, -- El 'PAGO TOTAL' del Excel
    total_a_pagar DECIMAL(12, 2) NOT NULL, -- El 'TOTAL A PAGAR' final
    
    -- Guardamos el JSON completo con el desglose por si acaso
    desglose_json JSON, 
    
    fecha_calculo TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sincronizado BOOLEAN DEFAULT TRUE, -- Para saber si vino offline
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- =====================================================================
-- 4. TABLA DE LOTES DE MINERAL (Trazabilidad)
-- =====================================================================
CREATE TABLE lotes_mineral (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    codigo_lote VARCHAR(50) NOT NULL UNIQUE, -- Ej: 'LOTE-2023-001'
    planta_id INT NOT NULL, -- ID de la planta (Chalhuanca, Antabamba, etc.)
    tonelaje_bruto DECIMAL(10, 2) NOT NULL,
    ley_estimada DECIMAL(10, 4),
    
    fecha_ingreso TIMESTAMP NOT NULL,
    fecha_salida TIMESTAMP NULL,
    
    estado ENUM('EN_TRANSITO', 'EN_PLANTA', 'PROCESADO', 'ENTREGADO') DEFAULT 'EN_TRANSITO',
    
    -- Campo calculado (o actualizado por el backend) para el tiempo barado
    dias_barado INT DEFAULT 0, 
    
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- =====================================================================
-- 5. TABLA DE EVENTOS DE TRAZABILIDAD (Historial del lote)
-- =====================================================================
CREATE TABLE eventos_lote (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id BIGINT NOT NULL,
    descripcion VARCHAR(255) NOT NULL, -- Ej: 'Ingreso a balanza', 'Inicio de molienda'
    fecha_evento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lote_id) REFERENCES lotes_mineral(id) ON DELETE CASCADE
);

-- =====================================================================
-- ÍNDICES PARA OPTIMIZAR BÚSQUEDAS
-- =====================================================================
CREATE INDEX idx_usuarios_correo ON usuarios(correo);
CREATE INDEX idx_suscripciones_usuario ON suscripciones(usuario_id);
CREATE INDEX idx_historial_usuario ON historial_calculos(usuario_id);
CREATE INDEX idx_lotes_usuario ON lotes_mineral(usuario_id);
CREATE INDEX idx_lotes_estado ON lotes_mineral(estado);
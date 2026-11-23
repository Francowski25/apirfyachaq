
-- APPIRFYACHAQ - Base de datos PostgreSQL

-- 1. Eliminar tablas anteriores
DROP TABLE IF EXISTS tuserTerms CASCADE;
DROP TABLE IF EXISTS tcalculationHistory CASCADE;
DROP TABLE IF EXISTS tmarketPrice CASCADE;
DROP TABLE IF EXISTS tuserFavorite CASCADE;
DROP TABLE IF EXISTS tuserPlant CASCADE;
DROP TABLE IF EXISTS tplant CASCADE;
DROP TABLE IF EXISTS tterms CASCADE;
DROP TABLE IF EXISTS tsubscription CASCADE;
DROP TABLE IF EXISTS tuser CASCADE;

DROP TYPE IF EXISTS user_role CASCADE;
DROP TYPE IF EXISTS sub_status CASCADE;
DROP TYPE IF EXISTS plant_status CASCADE;

-- 2. Usuarios
CREATE TABLE tuser (
    idUser VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(10) NOT NULL DEFAULT 'minero'
        CHECK (role IN ('admin', 'minero')),
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status BOOLEAN DEFAULT TRUE,
    resetCodeHash VARCHAR(100),
    resetCodeExpiresAt TIMESTAMP,
    resetAttempts INT DEFAULT 0
);

-- 3. Suscripciones
CREATE TABLE tsubscription (
    idSubscription VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    idUser VARCHAR(36) NOT NULL,
    googlePurchaseToken VARCHAR(500) NOT NULL,
    googleProductId VARCHAR(100) NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVA'
        CHECK (status IN ('ACTIVA', 'CANCELADA', 'EXPIRADA', 'EN_GRACIA')),
    startDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expirationDate TIMESTAMP NULL,
    autoRenew BOOLEAN DEFAULT TRUE,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subscription_user
        FOREIGN KEY (idUser) REFERENCES tuser(idUser) ON DELETE CASCADE
);

-- 4. Términos generales
CREATE TABLE tterms (
    idTerm VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    factorConcentrado FLOAT NOT NULL DEFAULT 3.3,
    humedad FLOAT NOT NULL DEFAULT 10.1,
    merma FLOAT NOT NULL DEFAULT 1.0,
    leyConcentradoCu FLOAT NOT NULL DEFAULT 25.23,
    leyConcentradoAg FLOAT NOT NULL DEFAULT 0.0,
    leyConcentradoAu FLOAT NOT NULL DEFAULT 0.0,
    pagableCu FLOAT NOT NULL DEFAULT 100.0,
    deduccionCu FLOAT NOT NULL DEFAULT 1.35,
    gastosRefCu FLOAT NOT NULL DEFAULT 0.19,
    proteccionCu FLOAT NOT NULL DEFAULT 0.1,
    pagableAg FLOAT NOT NULL DEFAULT 90.0,
    deduccionAg FLOAT NOT NULL DEFAULT 2.0,
    gastosRefAg FLOAT NOT NULL DEFAULT 1.2,
    pagableAu FLOAT NOT NULL DEFAULT 90.0,
    deduccionAu FLOAT NOT NULL DEFAULT 60.0,
    gastosRefAu FLOAT NOT NULL DEFAULT 12.0,
    maquila FLOAT NOT NULL DEFAULT 175.0,
    analisis FLOAT NOT NULL DEFAULT 100.0,
    trasladoInterno FLOAT NOT NULL DEFAULT 55.0,
    igvPorcentaje FLOAT NOT NULL DEFAULT 0.18,
    detraccionPorcentaje FLOAT NOT NULL DEFAULT 0.10,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Plantas oficiales (solo admin)
CREATE TABLE tplant (
    idPlant VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    idTerm VARCHAR(36) NOT NULL,
    name VARCHAR(150) NOT NULL,
    location VARCHAR(200) NOT NULL,
    capacityTMAD FLOAT NOT NULL,
    rentalCostPerTM FLOAT NOT NULL,
    status VARCHAR(15) NOT NULL DEFAULT 'Operativa'
        CHECK (status IN ('Operativa', 'Mantenimiento')),
    CONSTRAINT fk_plant_terms
        FOREIGN KEY (idTerm) REFERENCES tterms(idTerm) ON DELETE CASCADE
);

-- 6. Plantas personales (cada minero)
CREATE TABLE tuserPlant (
    idUserPlant VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    idUser VARCHAR(36) NOT NULL,
    idTerm VARCHAR(36) NOT NULL,
    name VARCHAR(150) NOT NULL,
    location VARCHAR(200) NOT NULL,
    capacityTMAD FLOAT NOT NULL,
    rentalCostPerTM FLOAT NOT NULL,
    status VARCHAR(15) NOT NULL DEFAULT 'Operativa'
        CHECK (status IN ('Operativa', 'Mantenimiento')),
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_userplant_user
        FOREIGN KEY (idUser) REFERENCES tuser(idUser) ON DELETE CASCADE,
    CONSTRAINT fk_userplant_terms
        FOREIGN KEY (idTerm) REFERENCES tterms(idTerm) ON DELETE RESTRICT,
    -- Permite validar que la planta pertenece al usuario
    CONSTRAINT uq_userplant_owner
        UNIQUE (idUserPlant, idUser)
);

-- 7. Términos personalizados del usuario
-- Se relaciona con una planta oficial o una personal, nunca ambas
CREATE TABLE tuserTerms (
    idUserTerm VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    idUser VARCHAR(36) NOT NULL,
    idPlant VARCHAR(36) NULL,
    idUserPlant VARCHAR(36) NULL,
    factorConcentrado FLOAT NOT NULL,
    humedad FLOAT NOT NULL,
    merma FLOAT NOT NULL,
    leyConcentradoCu FLOAT NOT NULL,
    leyConcentradoAg FLOAT NOT NULL,
    leyConcentradoAu FLOAT NOT NULL,
    pagableCu FLOAT NOT NULL,
    deduccionCu FLOAT NOT NULL,
    gastosRefCu FLOAT NOT NULL,
    proteccionCu FLOAT NOT NULL,
    pagableAg FLOAT NOT NULL,
    deduccionAg FLOAT NOT NULL,
    gastosRefAg FLOAT NOT NULL,
    pagableAu FLOAT NOT NULL,
    deduccionAu FLOAT NOT NULL,
    gastosRefAu FLOAT NOT NULL,
    maquila FLOAT NOT NULL,
    analisis FLOAT NOT NULL,
    trasladoInterno FLOAT NOT NULL,
    igvPorcentaje FLOAT NOT NULL,
    detraccionPorcentaje FLOAT NOT NULL,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_userterms_user
        FOREIGN KEY (idUser) REFERENCES tuser(idUser) ON DELETE CASCADE,
    CONSTRAINT fk_userterms_plant
        FOREIGN KEY (idPlant) REFERENCES tplant(idPlant) ON DELETE CASCADE,
    -- La planta personal debe pertenecer al mismo usuario
    CONSTRAINT fk_userterms_userplant_owner
        FOREIGN KEY (idUserPlant, idUser)
        REFERENCES tuserPlant(idUserPlant, idUser) ON DELETE CASCADE,
    -- Solo un tipo de planta relacionada
    CONSTRAINT ck_userterms_plant_type
        CHECK (
            (idPlant IS NOT NULL AND idUserPlant IS NULL)
            OR
            (idPlant IS NULL AND idUserPlant IS NOT NULL)
        )
);

-- 8. Índices únicos de términos personalizados
CREATE UNIQUE INDEX uq_userterms_official_plant
    ON tuserTerms (idUser, idPlant)
    WHERE idPlant IS NOT NULL;

CREATE UNIQUE INDEX uq_userterms_personal_plant
    ON tuserTerms (idUser, idUserPlant)
    WHERE idUserPlant IS NOT NULL;

-- 9. Historial de cálculos
CREATE TABLE tcalculationHistory (
    idCalculation VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    idUser VARCHAR(36) NOT NULL,

    idPlant VARCHAR(36) NULL,
    idUserPlant VARCHAR(36) NULL,
    plantType VARCHAR(10) NOT NULL CHECK (plantType IN ('OFICIAL', 'PERSONAL')),
    plantName VARCHAR(150) NOT NULL,
    plantLocation VARCHAR(200) NOT NULL,
    plantRentalCostPerTM DOUBLE PRECISION NOT NULL,
    termsSource VARCHAR(15) NOT NULL CHECK (termsSource IN ('GENERAL', 'PERSONALIZADO')),

    factorConcentrado DOUBLE PRECISION NOT NULL,
    humedad DOUBLE PRECISION NOT NULL,
    merma DOUBLE PRECISION NOT NULL,
    leyConcentradoCu DOUBLE PRECISION NOT NULL,
    leyConcentradoAg DOUBLE PRECISION NOT NULL,
    leyConcentradoAu DOUBLE PRECISION NOT NULL,
    pagableCu DOUBLE PRECISION NOT NULL,
    deduccionCu DOUBLE PRECISION NOT NULL,
    gastosRefCu DOUBLE PRECISION NOT NULL,
    proteccionCu DOUBLE PRECISION NOT NULL,
    pagableAg DOUBLE PRECISION NOT NULL,
    deduccionAg DOUBLE PRECISION NOT NULL,
    gastosRefAg DOUBLE PRECISION NOT NULL,
    pagableAu DOUBLE PRECISION NOT NULL,
    deduccionAu DOUBLE PRECISION NOT NULL,
    gastosRefAu DOUBLE PRECISION NOT NULL,
    maquila DOUBLE PRECISION NOT NULL,
    analisis DOUBLE PRECISION NOT NULL,
    trasladoInterno DOUBLE PRECISION NOT NULL,
    igvPorcentaje DOUBLE PRECISION NOT NULL,
    detraccionPorcentaje DOUBLE PRECISION NOT NULL,

    material VARCHAR(50) NOT NULL,
    tonelaje DOUBLE PRECISION NOT NULL,
    leyCu DOUBLE PRECISION NOT NULL,
    leyAg DOUBLE PRECISION NOT NULL,
    leyAu DOUBLE PRECISION NOT NULL,
    adelanto DOUBLE PRECISION NOT NULL DEFAULT 0,

    precioCu DOUBLE PRECISION NOT NULL,
    precioAg DOUBLE PRECISION NOT NULL,
    precioAu DOUBLE PRECISION NOT NULL,
    preciosUpdatedAt TIMESTAMP NULL,

    tmHumedas DOUBLE PRECISION NOT NULL,
    tmns DOUBLE PRECISION NOT NULL,

    contenidoPagableCu DOUBLE PRECISION NOT NULL DEFAULT 0,
    precioAjustadoCu DOUBLE PRECISION NOT NULL DEFAULT 0,
    pagoCu DOUBLE PRECISION NOT NULL DEFAULT 0,
    contenidoPagableAg DOUBLE PRECISION NOT NULL DEFAULT 0,
    precioAjustadoAg DOUBLE PRECISION NOT NULL DEFAULT 0,
    pagoAg DOUBLE PRECISION NOT NULL DEFAULT 0,
    contenidoPagableAu DOUBLE PRECISION NOT NULL DEFAULT 0,
    precioAjustadoAu DOUBLE PRECISION NOT NULL DEFAULT 0,
    pagoAu DOUBLE PRECISION NOT NULL DEFAULT 0,

    pagoTotal DOUBLE PRECISION NOT NULL,
    reduccionRefinacion DOUBLE PRECISION NOT NULL DEFAULT 0,
    analisisPorTmns DOUBLE PRECISION NOT NULL DEFAULT 0,
    trasladoPorTmns DOUBLE PRECISION NOT NULL DEFAULT 0,
    pagoPorTm DOUBLE PRECISION NOT NULL DEFAULT 0,
    valorNeto DOUBLE PRECISION NOT NULL DEFAULT 0,
    igv DOUBLE PRECISION NOT NULL DEFAULT 0,
    valorTotal DOUBLE PRECISION NOT NULL DEFAULT 0,
    detraccion DOUBLE PRECISION NOT NULL DEFAULT 0,
    alquilerPlanta DOUBLE PRECISION NOT NULL,
    alquilerTotal DOUBLE PRECISION NOT NULL DEFAULT 0,
    totalAPagar DOUBLE PRECISION NOT NULL,

    calculatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    isSynced BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_calculation_user
        FOREIGN KEY (idUser) REFERENCES tuser(idUser) ON DELETE CASCADE,
    CONSTRAINT fk_calculation_plant
        FOREIGN KEY (idPlant) REFERENCES tplant(idPlant) ON DELETE SET NULL,
    CONSTRAINT fk_calculation_userplant
        FOREIGN KEY (idUserPlant) REFERENCES tuserPlant(idUserPlant) ON DELETE SET NULL,
    CONSTRAINT ck_calculation_plant_ref CHECK (
        (plantType = 'OFICIAL' AND idUserPlant IS NULL)
        OR (plantType = 'PERSONAL' AND idPlant IS NULL)
    )
);

CREATE INDEX idx_calculation_user_date ON tcalculationHistory(idUser, calculatedAt DESC);
CREATE INDEX idx_calculation_plant ON tcalculationHistory(idPlant);
CREATE INDEX idx_calculation_userplant ON tcalculationHistory(idUserPlant);

-- 10. Favoritos (plantas oficiales)
CREATE TABLE tuserFavorite (
    idUserFavorite VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    idUser VARCHAR(36) NOT NULL,
    idPlant VARCHAR(36) NOT NULL,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_userfavorite_user
        FOREIGN KEY (idUser) REFERENCES tuser(idUser) ON DELETE CASCADE,
    CONSTRAINT fk_userfavorite_plant
        FOREIGN KEY (idPlant) REFERENCES tplant(idPlant) ON DELETE CASCADE,
    CONSTRAINT uq_userfavorite_user_plant
        UNIQUE (idUser, idPlant)
);

-- 11. Precios de mercado
CREATE TABLE tmarketPrice (
    idMarketPrice VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    metal VARCHAR(10) NOT NULL,
    price NUMERIC(18,6) NOT NULL,
    updatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_market_price_metal UNIQUE (metal),
    CONSTRAINT ck_market_price_price CHECK (price > 0)
);

-- 12. Índices adicionales
CREATE INDEX idx_subscription_user ON tsubscription(idUser);
CREATE INDEX idx_plant_term ON tplant(idTerm);
CREATE INDEX idx_userplant_user ON tuserPlant(idUser);
CREATE INDEX idx_userplant_term ON tuserPlant(idTerm);
CREATE INDEX idx_userterms_user ON tuserTerms(idUser);
CREATE INDEX idx_userterms_plant ON tuserTerms(idPlant);
CREATE INDEX idx_userterms_userplant ON tuserTerms(idUserPlant);
CREATE INDEX idx_userfavorite_user ON tuserFavorite(idUser);

-- 13. Usuarios de prueba
INSERT INTO tuser (name, email, password, role, createdAt, status)
VALUES
('Carlos Admin', 'admin@yachaq.com', '$2a$12$ESpe3J8spdwd8cvbAWO.qOy3TS4xX/.Mfg/PXrUNgi74/f4ynO0ha', 'admin', CURRENT_TIMESTAMP, TRUE),
('Juan Minero', 'minero@yachaq.com', '$2a$12$KYSLKVbAF3oNLbSRvM.JrO3gnpsDq/PDzCUzyD23OZlO2liInylIi', 'minero', CURRENT_TIMESTAMP, TRUE),
('Pedro Quispe', 'pedro@gmail.com', '$2a$12$jUNrAOlOkXJv./lsQgqOReI7GSYQlM8fJPNF1uKqzgpTsaTOk1QTW', 'minero', CURRENT_TIMESTAMP, TRUE),
('Maria Condori', 'maria@gmail.com', '$2a$12$DOgLk2180jYcBqy6uuXQ4eMwOXIk21IQ3e.U4DpC6oiP/x9laB1me', 'minero', CURRENT_TIMESTAMP, TRUE),
('Luis Suarez', 'luis@gmail.com', '$2a$12$IZIpUHAGAptNe506WpfttO6oHuQl44smVjQ5ShVNZTu2GaqlC4XNS', 'minero', CURRENT_TIMESTAMP, FALSE);

-- 14. Términos generales
INSERT INTO tterms (
    factorConcentrado, humedad, merma,
    leyConcentradoCu, leyConcentradoAg, leyConcentradoAu,
    pagableCu, deduccionCu, gastosRefCu, proteccionCu,
    pagableAg, deduccionAg, gastosRefAg,
    pagableAu, deduccionAu, gastosRefAu,
    maquila, analisis, trasladoInterno,
    igvPorcentaje, detraccionPorcentaje
)
VALUES (
    3.3, 10.1, 1.0,
    25.23, 0.0, 0.0,
    100.0, 1.35, 0.19, 0.1,
    90.0, 2.0, 1.2,
    90.0, 60.0, 12.0,
    175.0, 100.0, 55.0,
    0.18, 0.10
);

-- 15. Plantas oficiales
INSERT INTO tplant (idTerm, name, location, capacityTMAD, rentalCostPerTM, status)
VALUES
((SELECT idTerm FROM tterms LIMIT 1), 'Planta Minera Yachaq', 'Abancay, Apurímac', 100.0, 45.00, 'Operativa'),
((SELECT idTerm FROM tterms LIMIT 1), 'Planta Concentradora San José', 'Andahuaylas, Apurímac', 150.0, 50.00, 'Operativa');

-- 16. Precios iniciales
INSERT INTO tmarketPrice (metal, price, updatedAt)
VALUES
('CU', 4.250000, CURRENT_TIMESTAMP),
('AG', 31.500000, CURRENT_TIMESTAMP),
('AU', 2350.000000, CURRENT_TIMESTAMP);

select * from tuser;
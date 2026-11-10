/*
DROP TABLE IF EXISTS tcalculationHistory CASCADE;
DROP TABLE IF EXISTS tmarketPrice CASCADE;
DROP TABLE IF EXISTS tplant CASCADE;
DROP TABLE IF EXISTS tterms CASCADE;
DROP TABLE IF EXISTS tsubscription CASCADE;
DROP TABLE IF EXISTS tuser CASCADE;
DROP TYPE IF EXISTS user_role;
DROP TYPE IF EXISTS sub_status;
DROP TYPE IF EXISTS plant_status;
*/

-- =====================================================================
--      TABLAS
-- =====================================================================
CREATE TABLE tuser (
    idUser VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(10) NOT NULL DEFAULT 'minero' CHECK (role IN ('admin', 'minero')),
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status BOOLEAN DEFAULT TRUE
);

CREATE TABLE tsubscription (
    idSubscription VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    idUser VARCHAR(36) NOT NULL,
    googlePurchaseToken VARCHAR(500) NOT NULL,
    googleProductId VARCHAR(100) NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVA' CHECK (status IN ('ACTIVA', 'CANCELADA', 'EXPIRADA', 'EN_GRACIA')),
    startDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expirationDate TIMESTAMP NULL,
    autoRenew BOOLEAN DEFAULT TRUE,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (idUser) REFERENCES tuser(idUser) ON DELETE CASCADE
);

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

CREATE TABLE tplant (
    idPlant VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    idTerm VARCHAR(36) NOT NULL,
    name VARCHAR(150) NOT NULL,
    location VARCHAR(200) NOT NULL,
    capacityTMAD FLOAT NOT NULL,
    rentalCostPerTM FLOAT NOT NULL,
    status VARCHAR(15) NOT NULL DEFAULT 'Operativa' CHECK (status IN ('Operativa', 'Mantenimiento')),

    CONSTRAINT fk_plant_terms FOREIGN KEY (idTerm) REFERENCES tterms(idTerm) ON DELETE CASCADE
);

CREATE TABLE tcalculationHistory (
    idCalculation VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    idUser VARCHAR(36) NOT NULL,
    material VARCHAR(50) NOT NULL,
    tonelaje DOUBLE PRECISION NOT NULL,
    leyCu DOUBLE PRECISION NOT NULL,
    leyAg DOUBLE PRECISION NOT NULL,
    leyAu DOUBLE PRECISION NOT NULL,
    precioAg DOUBLE PRECISION NOT NULL,
    precioAu DOUBLE PRECISION NOT NULL,
    adelanto DOUBLE PRECISION NOT NULL,
    precioCu DOUBLE PRECISION NOT NULL,
    alquilerPlanta DOUBLE PRECISION NOT NULL,

    tmHumedas DOUBLE PRECISION NOT NULL,
    tmns DOUBLE PRECISION NOT NULL,
    pagoTotal DOUBLE PRECISION NOT NULL,
    totalAPagar DOUBLE PRECISION NOT NULL,

    calculatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    isSynced BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (idUser) REFERENCES tuser(idUser) ON DELETE CASCADE
);

CREATE TABLE tmarketPrice (
    idMarketPrice VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    metal VARCHAR(10) NOT NULL,
    price NUMERIC(18,6) NOT NULL,
    updatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_market_price_metal UNIQUE (metal),
    CONSTRAINT ck_market_price_price CHECK (price > 0)
);

-- =====================================================================
--      INSERTS
-- =====================================================================
INSERT INTO tuser (name, email, password, role, createdAt, status) VALUES
('Carlos Admin', 'admin@yachaq.com', '$2a$12$v0YFGBhHaduV1f4xuuthy.rsDcdr5RmdS8fyt.0/5y4MAeiwe7sjy', 'admin', CURRENT_TIMESTAMP, true),
('Juan Minero', 'minero@yachaq.com', '$2a$12$KYSLKVbAF3oNLbSRvM.JrO3gnpsDq/PDzCUzyD23OZlO2liInylIi', 'minero', CURRENT_TIMESTAMP, true),
('Pedro Quispe', 'pedro@gmail.com', '$2a$12$jUNrAOlOkXJv./lsQgqOReI7GSYQlM8fJPNF1uKqzgpTsaTOk1QTW', 'minero', CURRENT_TIMESTAMP, true),
('Maria Condori', 'maria@gmail.com', '$2a$12$DOgLk2180jYcBqy6uuXQ4eMwOXIk21IQ3e.U4DpC6oiP/x9laB1me', 'minero', CURRENT_TIMESTAMP, true),
('Luis Suarez', 'luis@gmail.com', '$2a$12$IZIpUHAGAptNe506WpfttO6oHuQl44smVjQ5ShVNZTu2GaqlC4XNS', 'minero', CURRENT_TIMESTAMP, false);

INSERT INTO tterms (factorConcentrado, humedad, merma, leyConcentradoCu, leyConcentradoAg, leyConcentradoAu, pagableCu, deduccionCu, gastosRefCu, proteccionCu, pagableAg, deduccionAg, gastosRefAg, pagableAu, deduccionAu, gastosRefAu, maquila, analisis, trasladoInterno, igvPorcentaje, detraccionPorcentaje) VALUES
(3.3, 10.1, 1.0, 25.23, 0.0, 0.0, 100.0, 1.35, 0.19, 0.1, 90.0, 2.0, 1.2, 90.0, 60.0, 12.0, 175.0, 100.0, 55.0, 0.18, 0.10);

INSERT INTO tplant (idTerm, name, location, capacityTMAD, rentalCostPerTM, status) VALUES
((SELECT idTerm FROM tterms LIMIT 1), 'Planta Minera Yachaq', 'Abancay, Apurímac', 100.0, 45.00, 'Operativa'),
((SELECT idTerm FROM tterms LIMIT 1), 'Planta Concentradora San José', 'Andahuaylas, Apurímac', 150.0, 50.00, 'Operativa');

INSERT INTO tmarketPrice (metal, price, updatedAt) VALUES
('CU', 4.250000, CURRENT_TIMESTAMP),
('AG', 31.500000, CURRENT_TIMESTAMP),
('AU', 2350.000000, CURRENT_TIMESTAMP);

SELECT * FROM tuser;
SELECT * FROM tcalculationHistory;
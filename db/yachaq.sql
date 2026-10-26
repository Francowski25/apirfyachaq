create database yachaq;
use yachaq;

CREATE TABLE tuser (
    idUser char(36) PRIMARY KEY,
    name VARCHAR(100) not null,
    email VARCHAR(150) not null unique,
    password VARCHAR(255) not null,
    role ENUM('admin', 'minero') DEFAULT 'minero',
    createdAt datetime not null,
    status boolean default true
);

CREATE TABLE tsubscription (
    idSubscription char(36) PRIMARY KEY,
    idUser char(36) NOT NULL, 
    googlePurchaseToken VARCHAR(500) NOT NULL, 
    googleProductId VARCHAR(100) NOT NULL,     
    status ENUM('ACTIVA', 'CANCELADA', 'EXPIRADA', 'EN_GRACIA') DEFAULT 'ACTIVA',
    startDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expirationDate TIMESTAMP NULL,
    autoRenew BOOLEAN DEFAULT TRUE,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (idUser) REFERENCES tuser(idUser) ON DELETE CASCADE
);

CREATE TABLE tterms (
    idTerm char(36) PRIMARY KEY, -- 🔥 FALTABA ESTO EN TU SCRIPT
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
    
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE tplant (
    idPlant char(36) PRIMARY KEY,
    idTerm char(36) NOT NULL,
    name VARCHAR(150) NOT NULL,
    location VARCHAR(200) NOT NULL,
    capacityTMAD FLOAT NOT NULL,
    rentalCostPerTM FLOAT NOT NULL,
    status ENUM('Operativa', 'Mantenimiento') DEFAULT 'Operativa',
    
    -- Llave foránea: Si borras una planta, se borran sus términos
    CONSTRAINT fk_plant_terms FOREIGN KEY (idTerm) REFERENCES tterms(idTerm) ON DELETE CASCADE
);

CREATE TABLE tcalculationHistory (
    idCalculation char(36) PRIMARY KEY,
    idUser char(36) NOT NULL,
    material VARCHAR(50) NOT NULL, 
    tonelaje FLOAT NOT NULL,
    ley FLOAT NOT NULL,
    precio FLOAT NOT NULL,
    alquilerPlanta FLOAT NOT NULL,
    
    tmHumedas FLOAT NOT NULL,
    tmns FLOAT NOT NULL,
    pagoTotal FLOAT NOT NULL, 
    totalAPagar FLOAT NOT NULL, 
    
    calculatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    isSynced BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (idUser) REFERENCES tuser(idUser) ON DELETE CASCADE
);

CREATE TABLE ttracking (
    idTracking char(36) PRIMARY KEY,
    idUser char(36) NOT NULL,
    idPlant char(36) NOT NULL,
    mineral VARCHAR(50) NOT NULL,
    grossTonnage FLOAT NOT NULL,
    entryDate DATE NOT NULL,
    status VARCHAR(50) DEFAULT 'EN PLANTA',
    daysInPlant INT DEFAULT 0,
    
    FOREIGN KEY (idUser) REFERENCES tuser(idUser) ON DELETE CASCADE,
    FOREIGN KEY (idPlant) REFERENCES tplant(idPlant)
);

-- =====================================================================
-- 		INSERTS 
-- =====================================================================
INSERT INTO tuser VALUES
(UUID(), 'Carlos Admin', 'admin@yachaq.com', '$2a$12$v0YFGBhHaduV1f4xuuthy.rsDcdr5RmdS8fyt.0/5y4MAeiwe7sjy', 'admin', NOW(), true),
(UUID(), 'Juan Minero', 'minero@yachaq.com', '$2a$12$KYSLKVbAF3oNLbSRvM.JrO3gnpsDq/PDzCUzyD23OZlO2liInylIi', 'minero', NOW(), true),
(UUID(), 'Pedro Quispe', 'pedro@gmail.com', '$2a$12$jUNrAOlOkXJv./lsQgqOReI7GSYQlM8fJPNF1uKqzgpTsaTOk1QTW', 'minero', NOW(), true),
(UUID(), 'Maria Condori', 'maria@gmail.com', '$2a$12$DOgLk2180jYcBqy6uuXQ4eMwOXIk21IQ3e.U4DpC6oiP/x9laB1me', 'minero', NOW(), true),
(UUID(), 'Luis Suarez', 'luis@gmail.com', '$2a$12$IZIpUHAGAptNe506WpfttO6oHuQl44smVjQ5ShVNZTu2GaqlC4XNS', 'minero', NOW(), false);

INSERT INTO tterms VALUES
(UUID(), 3.3, 10.1, 1.0, 25.23, 0.0, 0.0, 100.0, 1.35, 0.19, 0.1, 90.0, 2.0, 1.2, 90.0, 60.0, 12.0, 175.0, 100.0, 55.0, 0.18, 0.10, DEFAULT);

INSERT INTO tplant VALUES
(UUID(), (SELECT idTerm FROM tterms LIMIT 1), 'Planta Minera Yachaq', 'Abancay, Apurímac', 100.0, 45.00, 'Operativa'),
(UUID(), (SELECT idTerm FROM tterms LIMIT 1), 'Planta Concentradora San José', 'Andahuaylas, Apurímac', 150.0, 50.00, 'Operativa');

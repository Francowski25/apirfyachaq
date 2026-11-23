package com.minero.appirfyachaq.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tcalculationHistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityCalculationHistory {

    @Id
    @Column(name = "idCalculation", length = 36)
    private String idCalculation;

    @Column(name = "idUser", length = 36, nullable = false)
    private String idUser;

    @Column(name = "idPlant", length = 36)
    private String idPlant;

    @Column(name = "idUserPlant", length = 36)
    private String idUserPlant;

    @Column(name = "plantType", length = 10, nullable = false)
    private String plantType; // OFICIAL | PERSONAL

    @Column(name = "plantName", length = 150, nullable = false)
    private String plantName;

    @Column(name = "plantLocation", length = 200, nullable = false)
    private String plantLocation;

    @Column(name = "plantRentalCostPerTM", nullable = false)
    private Double plantRentalCostPerTM;

    @Column(name = "termsSource", length = 15, nullable = false)
    private String termsSource; // GENERAL | PERSONALIZADO

    @Column(name = "factorConcentrado", nullable = false)
    private Double factorConcentrado;

    @Column(name = "humedad", nullable = false)
    private Double humedad;

    @Column(name = "merma", nullable = false)
    private Double merma;

    @Column(name = "leyConcentradoCu", nullable = false)
    private Double leyConcentradoCu;

    @Column(name = "leyConcentradoAg", nullable = false)
    private Double leyConcentradoAg;

    @Column(name = "leyConcentradoAu", nullable = false)
    private Double leyConcentradoAu;

    @Column(name = "pagableCu", nullable = false)
    private Double pagableCu;

    @Column(name = "deduccionCu", nullable = false)
    private Double deduccionCu;

    @Column(name = "gastosRefCu", nullable = false)
    private Double gastosRefCu;

    @Column(name = "proteccionCu", nullable = false)
    private Double proteccionCu;

    @Column(name = "pagableAg", nullable = false)
    private Double pagableAg;

    @Column(name = "deduccionAg", nullable = false)
    private Double deduccionAg;

    @Column(name = "gastosRefAg", nullable = false)
    private Double gastosRefAg;

    @Column(name = "pagableAu", nullable = false)
    private Double pagableAu;

    @Column(name = "deduccionAu", nullable = false)
    private Double deduccionAu;

    @Column(name = "gastosRefAu", nullable = false)
    private Double gastosRefAu;

    @Column(name = "maquila", nullable = false)
    private Double maquila;

    @Column(name = "analisis", nullable = false)
    private Double analisis;

    @Column(name = "trasladoInterno", nullable = false)
    private Double trasladoInterno;

    @Column(name = "igvPorcentaje", nullable = false)
    private Double igvPorcentaje;

    @Column(name = "detraccionPorcentaje", nullable = false)
    private Double detraccionPorcentaje;

    @Column(name = "material", length = 50, nullable = false)
    private String material;

    @Column(name = "tonelaje", nullable = false)
    private Double tonelaje;

    @Column(name = "leyCu", nullable = false)
    private Double leyCu;

    @Column(name = "leyAg", nullable = false)
    private Double leyAg;

    @Column(name = "leyAu", nullable = false)
    private Double leyAu;

    @Column(name = "adelanto", nullable = false)
    private Double adelanto;

    @Column(name = "precioCu", nullable = false)
    private Double precioCu;

    @Column(name = "precioAg", nullable = false)
    private Double precioAg;

    @Column(name = "precioAu", nullable = false)
    private Double precioAu;

    @Column(name = "preciosUpdatedAt")
    private LocalDateTime preciosUpdatedAt;

    @Column(name = "tmHumedas", nullable = false)
    private Double tmHumedas;

    @Column(name = "tmns", nullable = false)
    private Double tmns;

    @Column(name = "contenidoPagableCu", nullable = false)
    private Double contenidoPagableCu;

    @Column(name = "precioAjustadoCu", nullable = false)
    private Double precioAjustadoCu;

    @Column(name = "pagoCu", nullable = false)
    private Double pagoCu;

    @Column(name = "contenidoPagableAg", nullable = false)
    private Double contenidoPagableAg;

    @Column(name = "precioAjustadoAg", nullable = false)
    private Double precioAjustadoAg;

    @Column(name = "pagoAg", nullable = false)
    private Double pagoAg;

    @Column(name = "contenidoPagableAu", nullable = false)
    private Double contenidoPagableAu;

    @Column(name = "precioAjustadoAu", nullable = false)
    private Double precioAjustadoAu;

    @Column(name = "pagoAu", nullable = false)
    private Double pagoAu;

    @Column(name = "pagoTotal", nullable = false)
    private Double pagoTotal;

    @Column(name = "reduccionRefinacion", nullable = false)
    private Double reduccionRefinacion;

    @Column(name = "analisisPorTmns", nullable = false)
    private Double analisisPorTmns;

    @Column(name = "trasladoPorTmns", nullable = false)
    private Double trasladoPorTmns;

    @Column(name = "pagoPorTm", nullable = false)
    private Double pagoPorTm;

    @Column(name = "valorNeto", nullable = false)
    private Double valorNeto;

    @Column(name = "igv", nullable = false)
    private Double igv;

    @Column(name = "valorTotal", nullable = false)
    private Double valorTotal;

    @Column(name = "detraccion", nullable = false)
    private Double detraccion;

    @Column(name = "alquilerPlanta", nullable = false)
    private Double alquilerPlanta;

    @Column(name = "alquilerTotal", nullable = false)
    private Double alquilerTotal;

    @Column(name = "totalAPagar", nullable = false)
    private Double totalAPagar;

    @Column(name = "calculatedAt", nullable = false)
    private LocalDateTime calculatedAt;

    @Column(name = "isSynced", nullable = false)
    private Boolean isSynced;
}
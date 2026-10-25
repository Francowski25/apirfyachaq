package com.minero.appirfyachaq.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "tterms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityTerms {

    @Id
    @Column(name = "idTerm", length = 36)
    private String idTerm;

    @Column(name = "factorConcentrado")
    private Double factorConcentrado;

    @Column(name = "humedad")
    private Double humedad;

    @Column(name = "merma")
    private Double merma;

    @Column(name = "leyConcentradoCu")
    private Double leyConcentradoCu;

    @Column(name = "leyConcentradoAg")
    private Double leyConcentradoAg;

    @Column(name = "leyConcentradoAu")
    private Double leyConcentradoAu;

    @Column(name = "pagableCu")
    private Double pagableCu;

    @Column(name = "deduccionCu")
    private Double deduccionCu;

    @Column(name = "gastosRefCu")
    private Double gastosRefCu;

    @Column(name = "proteccionCu")
    private Double proteccionCu;

    @Column(name = "pagableAg")
    private Double pagableAg;

    @Column(name = "deduccionAg")
    private Double deduccionAg;

    @Column(name = "gastosRefAg")
    private Double gastosRefAg;

    @Column(name = "pagableAu")
    private Double pagableAu;

    @Column(name = "deduccionAu")
    private Double deduccionAu;

    @Column(name = "gastosRefAu")
    private Double gastosRefAu;

    @Column(name = "maquila")
    private Double maquila;

    @Column(name = "analisis")
    private Double analisis;

    @Column(name = "trasladoInterno")
    private Double trasladoInterno;

    @Column(name = "igvPorcentaje")
    private Double igvPorcentaje;

    @Column(name = "detraccionPorcentaje")
    private Double detraccionPorcentaje;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;
}
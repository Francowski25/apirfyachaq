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

    @Column(name = "material", length = 50, nullable = false)
    private String material;

    @Column(name = "tonelaje", nullable = false)
    private Double tonelaje;

    @Column(name = "ley", nullable = false)
    private Double ley;

    @Column(name = "precio", nullable = false)
    private Double precio;

    @Column(name = "alquilerPlanta", nullable = false)
    private Double alquilerPlanta;

    @Column(name = "tmHumedas", nullable = false)
    private Double tmHumedas;

    @Column(name = "tmns", nullable = false)
    private Double tmns;

    @Column(name = "pagoTotal", nullable = false)
    private Double pagoTotal;

    @Column(name = "totalAPagar", nullable = false)
    private Double totalAPagar;

    @Column(name = "calculatedAt")
    private LocalDateTime calculatedAt;

    @Column(name = "isSynced")
    private Boolean isSynced;
}
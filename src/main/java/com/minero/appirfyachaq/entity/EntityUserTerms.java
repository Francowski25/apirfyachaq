package com.minero.appirfyachaq.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tuserTerms",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_userterms_official_plant", columnNames = {"idUser", "idPlant"}),
                @UniqueConstraint(name = "uq_userterms_personal_plant", columnNames = {"idUser", "idUserPlant"})
        },
        indexes = {
                @Index(name = "idx_userterms_user", columnList = "idUser"),
                @Index(name = "idx_userterms_plant", columnList = "idPlant"),
                @Index(name = "idx_userterms_userplant", columnList = "idUserPlant")
        })
@Check(constraints = "(idPlant IS NOT NULL AND idUserPlant IS NULL) OR (idPlant IS NULL AND idUserPlant IS NOT NULL)")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityUserTerms {

    @Id
    @Column(name = "idUserTerm", length = 36)
    private String idUserTerm;

    @Column(name = "idUser", length = 36, nullable = false)
    private String idUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUser", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_userterms_user"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private EntityUser user;

    @Column(name = "idPlant", length = 36)
    private String idPlant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPlant", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_userterms_plant"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private EntityPlant plant;

    @Column(name = "idUserPlant", length = 36)
    private String idUserPlant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(
            value = {
                    @JoinColumn(name = "idUserPlant", referencedColumnName = "idUserPlant",
                            insertable = false, updatable = false),
                    @JoinColumn(name = "idUser", referencedColumnName = "idUser",
                            insertable = false, updatable = false)
            },
            foreignKey = @ForeignKey(name = "fk_userterms_userplant_owner"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private EntityUserPlant userPlant;

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

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;
}
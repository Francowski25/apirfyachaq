package com.minero.appirfyachaq.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tuserPlant",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_userplant_owner", columnNames = {"idUserPlant", "idUser"}),
        indexes = {
                @Index(name = "idx_userplant_user", columnList = "idUser"),
                @Index(name = "idx_userplant_term", columnList = "idTerm")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityUserPlant {

    @Id
    @Column(name = "idUserPlant", length = 36)
    private String idUserPlant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUser", referencedColumnName = "idUser", nullable = false,
            foreignKey = @ForeignKey(name = "fk_userplant_user"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private EntityUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTerm", referencedColumnName = "idTerm", nullable = false,
            foreignKey = @ForeignKey(name = "fk_userplant_terms"))
    private EntityTerms terms;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "location", length = 200, nullable = false)
    private String location;

    @Column(name = "capacityTMAD", nullable = false)
    private Double capacityTMAD;

    @Column(name = "rentalCostPerTM", nullable = false)
    private Double rentalCostPerTM;

    @Column(name = "status", length = 15, nullable = false)
    private String status;

    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;
}

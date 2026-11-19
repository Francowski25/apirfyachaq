package com.minero.appirfyachaq.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "tplant", indexes = @Index(name = "idx_plant_term", columnList = "idTerm"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityPlant {

    @Id
    @Column(name = "idPlant", length = 36)
    private String idPlant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTerm", referencedColumnName = "idTerm", nullable = false,
            foreignKey = @ForeignKey(name = "fk_plant_terms"))
    @OnDelete(action = OnDeleteAction.CASCADE)
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
}
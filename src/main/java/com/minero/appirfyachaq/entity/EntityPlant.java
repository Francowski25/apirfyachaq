package com.minero.appirfyachaq.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tplant")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityPlant {

    @Id
    @Column(name = "idPlant", length = 36)
    private String idPlant;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "idTerm", referencedColumnName = "idTerm", nullable = false)
    private EntityTerms terms;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "location", length = 200, nullable = false)
    private String location;

    @Column(name = "capacityTMAD", nullable = false)
    private Double capacityTMAD;

    @Column(name = "rentalCostPerTM", nullable = false)
    private Double rentalCostPerTM;

    @Column(name = "status")
    private String status;
}
package com.minero.appirfyachaq.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "ttracking")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityTracking {

    @Id
    @Column(name = "idTracking", length = 36)
    private String idTracking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUser", nullable = false)
    private EntityUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPlant", nullable = false)
    private EntityPlant plant;

    @Column(name = "mineral", length = 50, nullable = false)
    private String mineral;

    @Column(name = "grossTonnage", nullable = false)
    private Double grossTonnage;

    @Column(name = "entryDate", nullable = false)
    private LocalDate entryDate;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "daysInPlant")
    private Integer daysInPlant;
}
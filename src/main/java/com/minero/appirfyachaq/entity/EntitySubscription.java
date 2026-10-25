package com.minero.appirfyachaq.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "tsubscription")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntitySubscription {

    @Id
    @Column(name = "idSubscription", length = 36)
    private String idSubscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUser", nullable = false)
    private EntityUser user;

    @Column(name = "googlePurchaseToken", length = 500, nullable = false)
    private String googlePurchaseToken;

    @Column(name = "googleProductId", length = 100, nullable = false)
    private String googleProductId;

    @Column(name = "status")
    private String status;

    @Column(name = "startDate")
    private LocalDateTime startDate;

    @Column(name = "expirationDate")
    private LocalDateTime expirationDate;

    @Column(name = "autoRenew")
    private Boolean autoRenew;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;
}
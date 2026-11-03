package com.minero.appirfyachaq.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "tmarketPrice",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_market_price_metal",
        columnNames = "metal"
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityMarketPrice {

    @Id
    @Column(name = "idMarketPrice", length = 36)
    private String idMarketPrice;

    @Column(name = "metal", length = 10, nullable = false)
    private String metal;

    @Column(name = "price", precision = 18, scale = 6, nullable = false)
    private BigDecimal price;

    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;
}
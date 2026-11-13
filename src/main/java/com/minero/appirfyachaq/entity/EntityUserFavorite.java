package com.minero.appirfyachaq.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "tuserFavorite")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityUserFavorite {

    @Id
    @Column(name = "idUserFavorite", length = 36)
    private String idUserFavorite;

    @Column(name = "idUser", length = 36)
    private String idUser;

    @Column(name = "idPlant", length = 36)
    private String idPlant;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;
}
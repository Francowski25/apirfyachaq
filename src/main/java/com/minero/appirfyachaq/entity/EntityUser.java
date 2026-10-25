package com.minero.appirfyachaq.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "tuser")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityUser {

    @Id
    @Column(name = "idUser", length = 36)
    private String idUser;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "email", length = 150, nullable = false, unique = true)
    private String email;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "role")
    private String role; // ADMIN o MINERO

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "status")
    private Boolean status;
}
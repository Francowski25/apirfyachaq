package com.minero.appirfyachaq.security;

import com.minero.appirfyachaq.entity.EntityUser;
import com.minero.appirfyachaq.repository.RepositoryUser;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private final RepositoryUser users;
    private final String email;
    private final String password;

    public AdminBootstrap(RepositoryUser users,
            @Value("${YACHAQ_ADMIN_EMAIL:}") String email,
            @Value("${YACHAQ_ADMIN_PASSWORD:}") String password) {
        this.users = users;
        this.email = email == null ? "" : email.trim().toLowerCase();
        this.password = password == null ? "" : password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() && password.isBlank()) return;
        if (!email.contains("@") || password.length() < 12) {
            throw new IllegalStateException("Configure YACHAQ_ADMIN_EMAIL y contraseña con 12 caracteres o más");
        }
        if (!users.existsByEmail(email)) {
            EntityUser admin = EntityUser.builder()
                .idUser(UUID.randomUUID().toString())
                .name("Administrador Yachaq")
                .email(email)
                .password(new BCryptPasswordEncoder().encode(password))
                .role("admin")
                .createdAt(LocalDateTime.now())
                .status(true)
                .build();
            users.save(admin);
        }
    }
}

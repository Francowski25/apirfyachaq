package com.minero.appirfyachaq.business;

import com.minero.appirfyachaq.dto.request.RequestLogin;
import com.minero.appirfyachaq.dto.request.RequestRegister;
import com.minero.appirfyachaq.dto.response.ResponseLogin;
import com.minero.appirfyachaq.dto.response.ResponseRegister;
import com.minero.appirfyachaq.entity.EntityUser;
import com.minero.appirfyachaq.repository.RepositoryUser;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BusinessAuth {

    private final RepositoryUser userRepository;
    private final com.minero.appirfyachaq.security.TokenService tokens;
    private final BCryptPasswordEncoder passwordEncoder;

    public BusinessAuth(RepositoryUser userRepository, com.minero.appirfyachaq.security.TokenService tokens) {
        this.tokens = tokens;
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public ResponseLogin login(RequestLogin request) {
        ResponseLogin response = new ResponseLogin();

        EntityUser user = userRepository.findByEmail(request.getEmail().trim().toLowerCase()).orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            response.error("Credenciales incorrectas. Verifique su correo y contraseña.");
            return response;
        }

        if (user.getStatus() != null && !user.getStatus()) {
            response.error("Su cuenta está desactivada. Contacte al administrador.");
            return response;
        }

        response.success("Login exitoso");
        response.setIdUser(user.getIdUser());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setIsPremium(false);
        response.setToken(tokens.create(user.getIdUser()));

        return response;
    }

    public ResponseRegister register(RequestRegister request) {
        ResponseRegister response = new ResponseRegister();

        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            response.error("El correo ya está registrado en el sistema.");
            return response;
        }

        EntityUser newUser = EntityUser.builder()
                .idUser(UUID.randomUUID().toString())
                .name(request.getName())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("minero") 
                .createdAt(LocalDateTime.now())
                .status(true)
                .build();

        userRepository.save(newUser);

        response.success("Usuario registrado exitosamente");
        response.setIdUser(newUser.getIdUser());
        response.setName(newUser.getName());
        response.setEmail(newUser.getEmail());
        response.setRole(newUser.getRole());
        response.setIsPremium(false);
        response.setToken(tokens.create(newUser.getIdUser()));

        return response;
    }
}
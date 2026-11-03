package com.minero.appirfyachaq.business;

import com.minero.appirfyachaq.dto.request.RequestLogin;
import com.minero.appirfyachaq.dto.request.RequestRegister;
import com.minero.appirfyachaq.dto.response.ResponseLogin;
import com.minero.appirfyachaq.dto.response.ResponseRegister;
import com.minero.appirfyachaq.entity.EntityUser;
import com.minero.appirfyachaq.repository.RepositoryUser;
import com.minero.appirfyachaq.security.TokenService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BusinessAuth {

    private final RepositoryUser userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public BusinessAuth(RepositoryUser userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.tokenService = tokenService;
    }

    public ResponseLogin login(RequestLogin request) {
        ResponseLogin response = new ResponseLogin();

        EntityUser user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            response.error("Credenciales incorrectas.");
            return response;
        }

        if (user.getStatus() != null && !user.getStatus()) {
            response.error("Su cuenta está desactivada.");
            return response;
        }

        String token = tokenService.create(user.getIdUser());

        response.success("Login exitoso");
        response.setIdUser(user.getIdUser());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setIsPremium(false);
        response.setToken(token);

        return response;
    }

    public ResponseRegister register(RequestRegister request) {
        ResponseRegister response = new ResponseRegister();

        if (userRepository.existsByEmail(request.getEmail())) {
            response.error("El correo ya está registrado.");
            return response;
        }

        EntityUser newUser = EntityUser.builder()
                .idUser(UUID.randomUUID().toString())
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("minero")
                .createdAt(LocalDateTime.now())
                .status(true)
                .build();

        userRepository.save(newUser);

        String token = tokenService.create(newUser.getIdUser());

        response.success("Usuario registrado exitosamente.");
        response.setIdUser(newUser.getIdUser());
        response.setName(newUser.getName());
        response.setEmail(newUser.getEmail());
        response.setRole(newUser.getRole());
        response.setIsPremium(false);
        response.setToken(token);

        return response;
    }
}
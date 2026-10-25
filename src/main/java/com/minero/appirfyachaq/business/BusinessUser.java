package com.minero.appirfyachaq.business;

import com.minero.appirfyachaq.dto.request.RequestUserAdmin;
import com.minero.appirfyachaq.dto.response.ResponseUser;
import com.minero.appirfyachaq.entity.EntityUser;
import com.minero.appirfyachaq.repository.RepositoryUser;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BusinessUser {

    private final RepositoryUser userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public BusinessUser(RepositoryUser userRepository) {
        this.userRepository = userRepository;
    }

    public ResponseUser crear(RequestUserAdmin request) {
        ResponseUser response = new ResponseUser();

        if (userRepository.existsByEmail(request.getEmail())) {
            response.error("El correo ya está registrado en el sistema.");
            return response;
        }

        try {
            EntityUser newUser = EntityUser.builder()
                    .idUser(UUID.randomUUID().toString())
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(request.getRole() != null ? request.getRole() : "minero")
                    .createdAt(LocalDateTime.now())
                    .status(request.getStatus() != null ? request.getStatus() : true)
                    .build();

            userRepository.save(newUser);
            response.success("Usuario creado exitosamente.");
            mapearRespuesta(newUser, response);

        } catch (Exception e) {
            response.exception("Error al crear usuario: " + e.getMessage());
        }
        return response;
    }

    public List<ResponseUser> listar() {
        List<EntityUser> usuarios = userRepository.findAll();
        List<ResponseUser> responses = new ArrayList<>();

        for (EntityUser u : usuarios) {
            ResponseUser res = new ResponseUser();
            res.success("OK");
            mapearRespuesta(u, res);
            responses.add(res);
        }
        return responses;
    }

    public ResponseUser editar(String idUser, RequestUserAdmin request) {
        ResponseUser response = new ResponseUser();
        Optional<EntityUser> optional = userRepository.findById(idUser);

        if (optional.isEmpty()) {
            response.error("Usuario no encontrado.");
            return response;
        }

        try {
            EntityUser user = optional.get();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setRole(request.getRole());
            user.setStatus(request.getStatus());

            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            userRepository.save(user);
            response.success("Usuario actualizado correctamente.");
            mapearRespuesta(user, response);
        } catch (Exception e) {
            response.exception("Error al actualizar: " + e.getMessage());
        }
        return response;
    }

    public ResponseUser eliminar(String idUser) {
        ResponseUser response = new ResponseUser();
        Optional<EntityUser> optional = userRepository.findById(idUser);

        if (optional.isEmpty()) {
            response.error("Usuario no encontrado.");
            return response;
        }

        try {
            userRepository.delete(optional.get());
            response.success("Usuario eliminado correctamente.");
        } catch (Exception e) {
            response.exception("Error al eliminar: " + e.getMessage());
        }
        return response;
    }

    private void mapearRespuesta(EntityUser u, ResponseUser res) {
        res.setIdUser(u.getIdUser());
        res.setName(u.getName());
        res.setEmail(u.getEmail());
        res.setRole(u.getRole());
        res.setCreatedAt(u.getCreatedAt());
        res.setStatus(u.getStatus());
    }
}
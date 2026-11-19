package com.minero.appirfyachaq.controller;

import com.minero.appirfyachaq.business.BusinessUser;
import com.minero.appirfyachaq.dto.request.RequestUserAdmin;
import com.minero.appirfyachaq.dto.response.ResponseUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(path = "user")
public class ControllerUser {

    private final BusinessUser businessUser;

    public ControllerUser(BusinessUser businessUser) {
        this.businessUser = businessUser;
    }

    @PostMapping(path = "add")
    public ResponseEntity<ResponseUser> crear(@Valid @RequestBody RequestUserAdmin request) {
        return ResponseEntity.ok(businessUser.crear(request));
    }

    @GetMapping(path = "list")
    public ResponseEntity<List<ResponseUser>> listar() {
        return ResponseEntity.ok(businessUser.listar());
    }

    @PutMapping(path = "update/{id}")
    public ResponseEntity<ResponseUser> editar(
            @PathVariable String id,
            @Valid @RequestBody RequestUserAdmin request,
            Authentication authentication) {
        if (authentication == null) {
            ResponseUser response = new ResponseUser();
            response.error("Autenticación requerida.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (!admin && !Objects.equals(id, authentication.getName())) {
            ResponseUser response = new ResponseUser();
            response.error("No tiene permiso para actualizar este usuario.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        return ResponseEntity.ok(businessUser.editar(id, request, authentication));
    }

    @DeleteMapping(path = "delete/{id}")
    public ResponseEntity<ResponseUser> eliminar(@PathVariable String id) {
        return ResponseEntity.ok(businessUser.eliminar(id));
    }
}
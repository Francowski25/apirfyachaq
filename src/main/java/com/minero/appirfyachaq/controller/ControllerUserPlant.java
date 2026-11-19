package com.minero.appirfyachaq.controller;

import com.minero.appirfyachaq.business.BusinessUserPlant;
import com.minero.appirfyachaq.dto.request.RequestUserPlant;
import com.minero.appirfyachaq.dto.response.ResponseUserPlant;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "user-plant")
@PreAuthorize("hasAuthority('ROLE_MINERO')")
public class ControllerUserPlant {

    private final BusinessUserPlant businessUserPlant;

    public ControllerUserPlant(BusinessUserPlant businessUserPlant) {
        this.businessUserPlant = businessUserPlant;
    }

    @PostMapping(path = "add")
    public ResponseEntity<ResponseUserPlant> crear(
            @Valid @RequestBody RequestUserPlant request, Authentication authentication) {
        return ResponseEntity.ok(businessUserPlant.crear(authentication.getName(), request));
    }

    @GetMapping(path = "list")
    public ResponseEntity<List<ResponseUserPlant>> listar(Authentication authentication) {
        return ResponseEntity.ok(businessUserPlant.listar(authentication.getName()));
    }

    @GetMapping(path = "{id}")
    public ResponseEntity<ResponseUserPlant> obtener(
            @PathVariable String id, Authentication authentication) {
        return ResponseEntity.ok(businessUserPlant.obtener(authentication.getName(), id));
    }

    @PutMapping(path = "update/{id}")
    public ResponseEntity<ResponseUserPlant> editar(
            @PathVariable String id,
            @Valid @RequestBody RequestUserPlant request,
            Authentication authentication) {
        return ResponseEntity.ok(businessUserPlant.editar(authentication.getName(), id, request));
    }

    @DeleteMapping(path = "delete/{id}")
    public ResponseEntity<ResponseUserPlant> eliminar(
            @PathVariable String id, Authentication authentication) {
        return ResponseEntity.ok(businessUserPlant.eliminar(authentication.getName(), id));
    }
}

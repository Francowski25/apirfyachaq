package com.minero.appirfyachaq.controller;

import com.minero.appirfyachaq.business.BusinessCalculation;
import com.minero.appirfyachaq.dto.request.RequestCalculation;
import com.minero.appirfyachaq.dto.response.ResponseCalculation;
import com.minero.appirfyachaq.dto.response.ResponseHistorial;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path = "calculation")
public class ControllerCalculation {

    private final BusinessCalculation businessCalculation;

    public ControllerCalculation(BusinessCalculation businessCalculation) {
        this.businessCalculation = businessCalculation;
    }

    @PostMapping(path = "calcular")
    public ResponseEntity<ResponseCalculation> calcular(@Valid @RequestBody RequestCalculation request, Authentication auth) {
        validarPropietario(request.getIdUser(), auth);
        return ResponseEntity.ok(businessCalculation.calcular(request));
    }

    @GetMapping(path = "historial/{idUser}")
    public ResponseEntity<List<ResponseHistorial>> obtenerHistorial(@PathVariable String idUser, Authentication auth) {
        validarPropietario(idUser, auth);
        return ResponseEntity.ok(businessCalculation.listarHistorial(idUser));
    }
    private void validarPropietario(String idUser, Authentication auth) {
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!admin && !auth.getName().equals(idUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado al historial de otro usuario");
        }
    }
}
package com.minero.appirfyachaq.controller;

import com.minero.appirfyachaq.business.BusinessTerms;
import com.minero.appirfyachaq.dto.request.RequestTerms;
import com.minero.appirfyachaq.dto.response.ResponseTerms;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "terms")
public class ControllerTerms {

    private final BusinessTerms businessTerms;

    public ControllerTerms(BusinessTerms businessTerms) {
        this.businessTerms = businessTerms;
    }

    @GetMapping(path = "get/{idPlant}")
    public ResponseEntity<ResponseTerms> obtener(@PathVariable String idPlant) {
        return ResponseEntity.ok(businessTerms.obtenerPorPlanta(idPlant));
    }

    @PutMapping(path = "update/{idPlant}")
    public ResponseEntity<ResponseTerms> actualizar(
            @PathVariable String idPlant, 
            @Valid @RequestBody RequestTerms request) {
        return ResponseEntity.ok(businessTerms.actualizar(idPlant, request));
    }
}
package com.minero.appirfyachaq.controller;

import com.minero.appirfyachaq.business.BusinessCalculation;
import com.minero.appirfyachaq.dto.request.RequestCalculation;
import com.minero.appirfyachaq.dto.response.ResponseCalculation;
import com.minero.appirfyachaq.dto.response.ResponseHistorial;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "calculation")
public class ControllerCalculation {

    private final BusinessCalculation businessCalculation;

    public ControllerCalculation(BusinessCalculation businessCalculation) {
        this.businessCalculation = businessCalculation;
    }

    @PostMapping(path = "calcular")
    public ResponseEntity<ResponseCalculation> calcular(@Valid @RequestBody RequestCalculation request) {
        return ResponseEntity.ok(businessCalculation.calcular(request));
    }

    @GetMapping(path = "historial/{idUser}")
    public ResponseEntity<List<ResponseHistorial>> obtenerHistorial(@PathVariable String idUser) {
        return ResponseEntity.ok(businessCalculation.listarHistorial(idUser));
    }
}
package com.minero.appirfyachaq.controller;

import com.minero.appirfyachaq.business.BusinessPlant;
import com.minero.appirfyachaq.dto.request.RequestPlant;
import com.minero.appirfyachaq.dto.response.ResponsePlant;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "plant")
public class ControllerPlant {

    private final BusinessPlant businessPlant;

    public ControllerPlant(BusinessPlant businessPlant) {
        this.businessPlant = businessPlant;
    }

    @PostMapping(path = "add")
    public ResponseEntity<ResponsePlant> crear(@Valid @RequestBody RequestPlant request) {
        return ResponseEntity.ok(businessPlant.crear(request));
    }

    @GetMapping(path = "list")
    public ResponseEntity<List<ResponsePlant>> listar() {
        return ResponseEntity.ok(businessPlant.listar());
    }

    @PutMapping(path = "update/{id}")
    public ResponseEntity<ResponsePlant> editar(@PathVariable String id, @Valid @RequestBody RequestPlant request) {
        return ResponseEntity.ok(businessPlant.editar(id, request));
    }

    @DeleteMapping(path = "delete/{id}")
    public ResponseEntity<ResponsePlant> eliminar(@PathVariable String id) {
        return ResponseEntity.ok(businessPlant.eliminar(id));
    }
}
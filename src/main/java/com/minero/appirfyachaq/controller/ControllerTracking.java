package com.minero.appirfyachaq.controller;

import com.minero.appirfyachaq.business.BusinessTracking;
import com.minero.appirfyachaq.dto.request.RequestTracking;
import com.minero.appirfyachaq.dto.response.ResponseTracking;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "tracking")
public class ControllerTracking {

    private final BusinessTracking businessTracking;

    public ControllerTracking(BusinessTracking businessTracking) {
        this.businessTracking = businessTracking;
    }

    @PostMapping(path = "add")
    public ResponseEntity<ResponseTracking> crear(@Valid @RequestBody RequestTracking request) {
        return ResponseEntity.ok(businessTracking.crear(request));
    }

    @GetMapping(path = "list/{idUser}")
    public ResponseEntity<List<ResponseTracking>> listar(@PathVariable String idUser) {
        return ResponseEntity.ok(businessTracking.listarPorUsuario(idUser));
    }

    @PutMapping(path = "status/{id}")
    public ResponseEntity<ResponseTracking> actualizarEstado(
            @PathVariable String id,
            @RequestParam String nuevoEstado) {
        return ResponseEntity.ok(businessTracking.actualizarEstado(id, nuevoEstado));
    }

    @DeleteMapping(path = "delete/{id}")
    public ResponseEntity<ResponseTracking> eliminar(@PathVariable String id) {
        return ResponseEntity.ok(businessTracking.eliminar(id));
    }
}
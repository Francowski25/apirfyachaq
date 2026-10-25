package com.minero.appirfyachaq.controller;

import com.minero.appirfyachaq.business.BusinessUser;
import com.minero.appirfyachaq.dto.request.RequestUserAdmin;
import com.minero.appirfyachaq.dto.response.ResponseUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ResponseUser> editar(@PathVariable String id, @Valid @RequestBody RequestUserAdmin request) {
        return ResponseEntity.ok(businessUser.editar(id, request));
    }

    @DeleteMapping(path = "delete/{id}")
    public ResponseEntity<ResponseUser> eliminar(@PathVariable String id) {
        return ResponseEntity.ok(businessUser.eliminar(id));
    }
}
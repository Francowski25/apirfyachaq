package com.minero.appirfyachaq.controller;

import com.minero.appirfyachaq.business.BusinessUserTerms;
import com.minero.appirfyachaq.dto.request.RequestUserTerms;
import com.minero.appirfyachaq.dto.response.ResponseUserTerms;
import com.minero.appirfyachaq.generic.ResponseGeneric;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "userterms")
public class ControllerUserTerms {

    private final BusinessUserTerms businessUserTerms;

    public ControllerUserTerms(BusinessUserTerms businessUserTerms) {
        this.businessUserTerms = businessUserTerms;
    }

    @GetMapping(path = "listar/{idUser}")
    public ResponseEntity<List<ResponseUserTerms>> listar(@PathVariable String idUser) {
        return ResponseEntity.ok(businessUserTerms.listar(idUser));
    }

    @PutMapping(path = "guardar")
    public ResponseEntity<ResponseGeneric> guardar(@Valid @RequestBody RequestUserTerms request) {
        return ResponseEntity.ok(businessUserTerms.guardar(request));
    }

    @PostMapping(path = "restablecer")
    public ResponseEntity<ResponseGeneric> restablecer(@Valid @RequestBody RequestUserTerms request) {
        return ResponseEntity.ok(businessUserTerms.restablecer(request));
    }
}
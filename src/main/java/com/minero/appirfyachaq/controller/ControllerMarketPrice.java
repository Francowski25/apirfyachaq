package com.minero.appirfyachaq.controller;

import com.minero.appirfyachaq.business.BusinessMarketPrice;
import com.minero.appirfyachaq.dto.request.RequestMarketPrice;
import com.minero.appirfyachaq.dto.response.ResponseMarketPrice;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "market-prices")
public class ControllerMarketPrice {

    private final BusinessMarketPrice businessMarketPrice;

    public ControllerMarketPrice(BusinessMarketPrice businessMarketPrice) {
        this.businessMarketPrice = businessMarketPrice;
    }

    @GetMapping(path = "list")
    public ResponseEntity<List<ResponseMarketPrice>> listar() {
        return ResponseEntity.ok(businessMarketPrice.listar());
    }

    @PutMapping(path = "update")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ResponseMarketPrice> actualizar(@Valid @RequestBody RequestMarketPrice request) {
        return ResponseEntity.ok(businessMarketPrice.actualizar(request));
    }
}
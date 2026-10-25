package com.minero.appirfyachaq.controller;

import com.minero.appirfyachaq.business.BusinessAuth;
import com.minero.appirfyachaq.dto.request.RequestLogin;
import com.minero.appirfyachaq.dto.request.RequestRegister;
import com.minero.appirfyachaq.dto.response.ResponseLogin;
import com.minero.appirfyachaq.dto.response.ResponseRegister;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "auth")
public class ControllerAuth {

    private final BusinessAuth businessAuth;

    public ControllerAuth(BusinessAuth businessAuth) {
        this.businessAuth = businessAuth;
    }

    @PostMapping(path = "login")
    public ResponseEntity<ResponseLogin> login(@Valid @RequestBody RequestLogin request) {
        ResponseLogin response = businessAuth.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "register")
    public ResponseEntity<ResponseRegister> register(@Valid @RequestBody RequestRegister request) {
        ResponseRegister response = businessAuth.register(request);
        return ResponseEntity.ok(response);
    }
}
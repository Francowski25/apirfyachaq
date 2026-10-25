package com.minero.appirfyachaq.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestLogin {
    
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Debe ingresar un formato de correo válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
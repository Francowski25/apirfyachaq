package com.minero.appirfyachaq.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestRegister {
    @NotBlank @Size(max=100)
    private String name;
    @NotBlank @Email @Size(max=150)
    private String email;
    @NotBlank @Size(min=8, max=128)
    private String password;
}

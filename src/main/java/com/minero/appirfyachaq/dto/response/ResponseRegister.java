package com.minero.appirfyachaq.dto.response;

import com.minero.appirfyachaq.generic.ResponseGeneric;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseRegister extends ResponseGeneric {
    private String idUser;
    private String name;
    private String email;
    private String role;
    private Boolean isPremium;
}
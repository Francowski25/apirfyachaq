package com.minero.appirfyachaq.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestRegister {
    private String name;
    private String email;
    private String password;
}
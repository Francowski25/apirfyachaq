package com.minero.appirfyachaq.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestUserAdmin {
    private String name;
    private String email;
    private String password;
    private String role;
    private Boolean status;
}
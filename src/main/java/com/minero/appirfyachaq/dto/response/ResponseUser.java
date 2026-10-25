package com.minero.appirfyachaq.dto.response;

import com.minero.appirfyachaq.generic.ResponseGeneric;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ResponseUser extends ResponseGeneric {
    private String idUser;
    private String name;
    private String email;
    private String role;
    private LocalDateTime createdAt;
    private Boolean status;
}
package com.minero.appirfyachaq.dto.response;

import com.minero.appirfyachaq.generic.ResponseGeneric;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ResponseHistorial extends ResponseGeneric {
    private String idCalculation;
    private String material;
    private Double tonelaje;
    private Double ley;
    private Double precio;
    private Double tmns;
    private Double totalAPagar;
    private LocalDateTime calculatedAt;
}
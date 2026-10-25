package com.minero.appirfyachaq.dto.response;

import com.minero.appirfyachaq.generic.ResponseGeneric;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseCalculation extends ResponseGeneric {
    private String idCalculation;
    private String material;
    private Double tmHumedas;
    private Double tmns;
    private Double pagoTotal;
    private Double totalAPagar;
    
    private Double valorNeto;
    private Double igv;
    private Double detraccion;
    private Double alquilerTotal;
}
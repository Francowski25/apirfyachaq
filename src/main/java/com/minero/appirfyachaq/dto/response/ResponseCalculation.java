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
    
    private Double contenidoPagableCu;
    private Double precioAjustadoCu;
    private Double pagoCu;
    
    private Double reduccionRefinacion;
    private Double analisisPorTmns;
    private Double trasladoPorTmns;
    private Double pagoPorTm;
    
    private Double valorNeto;
    private Double igv;
    private Double valorTotal;
    private Double detraccion;
    private Double alquilerTotal;
    private Double totalAPagar;
}
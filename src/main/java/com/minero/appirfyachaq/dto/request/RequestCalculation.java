package com.minero.appirfyachaq.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestCalculation {
    private String idUser;
    private String idPlant;
    private String material;
    private Double tonelaje;
    private Double ley;
    private Double precio;
}
package com.minero.appirfyachaq.dto.response;

import com.minero.appirfyachaq.generic.ResponseGeneric;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseTerms extends ResponseGeneric {
    private String idTerm;
    private String idPlant;
    private String plantName;

    private Double factorConcentrado;
    private Double humedad;
    private Double merma;
    private Double leyConcentradoCu;
    private Double leyConcentradoAg;
    private Double leyConcentradoAu;
    private Double pagableCu;
    private Double deduccionCu;
    private Double gastosRefCu;
    private Double proteccionCu;
    private Double pagableAg;
    private Double deduccionAg;
    private Double gastosRefAg;
    private Double pagableAu;
    private Double deduccionAu;
    private Double gastosRefAu;
    private Double maquila;
    private Double analisis;
    private Double trasladoInterno;
    private Double igvPorcentaje;
    private Double detraccionPorcentaje;
}
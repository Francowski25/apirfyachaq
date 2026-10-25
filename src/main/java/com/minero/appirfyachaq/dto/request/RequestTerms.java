package com.minero.appirfyachaq.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestTerms {
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
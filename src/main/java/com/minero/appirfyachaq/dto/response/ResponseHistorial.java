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

    private String idPlant;
    private String idUserPlant;
    private String plantType;
    private String plantName;
    private String plantLocation;
    private Double plantRentalCostPerTM;
    private String termsSource;

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

    private Double leyCu;
    private Double leyAg;
    private Double leyAu;
    private Double adelanto;

    private Double precioCu;
    private Double precioAg;
    private Double precioAu;
    private LocalDateTime preciosUpdatedAt;

    private Double tmHumedas;

    private Double contenidoPagableCu;
    private Double precioAjustadoCu;
    private Double pagoCu;
    private Double contenidoPagableAg;
    private Double precioAjustadoAg;
    private Double pagoAg;
    private Double contenidoPagableAu;
    private Double precioAjustadoAu;
    private Double pagoAu;

    private Double pagoTotal;
    private Double reduccionRefinacion;
    private Double analisisPorTmns;
    private Double trasladoPorTmns;
    private Double pagoPorTm;
    private Double valorNeto;
    private Double igv;
    private Double valorTotal;
    private Double detraccion;
    private Double alquilerPlanta;
    private Double alquilerTotal;

    private Boolean isSynced;
}
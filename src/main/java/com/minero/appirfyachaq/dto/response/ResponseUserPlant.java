package com.minero.appirfyachaq.dto.response;

import com.minero.appirfyachaq.generic.ResponseGeneric;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ResponseUserPlant extends ResponseGeneric {

    private String idUserPlant;
    private String idTerm;
    private String name;
    private String location;
    private Double capacityTMAD;
    private Double rentalCostPerTM;
    private String status;
    private Double maquila;
    private Double analisis;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
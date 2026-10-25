package com.minero.appirfyachaq.dto.response;

import com.minero.appirfyachaq.generic.ResponseGeneric;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponsePlant extends ResponseGeneric {
    private String idPlant;
    private String idTerm;
    private String name;
    private String location;
    private Double capacityTMAD;
    private Double rentalCostPerTM;
    private String status;
}
package com.minero.appirfyachaq.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestPlant {
    private String name;
    private String location;
    private Double capacityTMAD;
    private Double rentalCostPerTM;
    private String status;
}
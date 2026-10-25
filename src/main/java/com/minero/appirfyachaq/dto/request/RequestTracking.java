package com.minero.appirfyachaq.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class RequestTracking {
    private String idUser;
    private String idPlant;
    private String mineral;
    private Double grossTonnage;
    private LocalDate entryDate;
    private String status;
}
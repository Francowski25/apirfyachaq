package com.minero.appirfyachaq.dto.response;

import com.minero.appirfyachaq.generic.ResponseGeneric;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class ResponseTracking extends ResponseGeneric {
    private String idTracking;
    private String idUser;
    private String idPlant;
    private String plantName;
    private String mineral;
    private Double grossTonnage;
    private LocalDate entryDate;
    private String status;
    private Integer daysInPlant;
}
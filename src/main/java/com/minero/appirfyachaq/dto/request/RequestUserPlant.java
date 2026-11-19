package com.minero.appirfyachaq.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestUserPlant {

    @NotBlank
    @Size(max = 150)
    private String name;

    @NotBlank
    @Size(max = 200)
    private String location;

    @NotNull
    @Positive
    private Double capacityTMAD;

    @NotNull
    @PositiveOrZero
    private Double rentalCostPerTM;

    @Pattern(regexp = "Operativa|Mantenimiento")
    private String status;
}

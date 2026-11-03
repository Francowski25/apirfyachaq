package com.minero.appirfyachaq.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RequestMarketPrice {
    @NotBlank
    private String metal;

    @NotNull
    @Positive
    private BigDecimal price;
}
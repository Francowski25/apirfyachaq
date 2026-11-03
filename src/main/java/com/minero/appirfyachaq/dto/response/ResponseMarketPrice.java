package com.minero.appirfyachaq.dto.response;

import com.minero.appirfyachaq.generic.ResponseGeneric;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ResponseMarketPrice extends ResponseGeneric {
    private String idMarketPrice;
    private String metal;
    private BigDecimal price;
    private LocalDateTime updatedAt;
}
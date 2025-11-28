package com.huongcung.webstore.checkout.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculateFeeResponseDTO {
    private BigDecimal total;
    private BigDecimal serviceFee;
    private String expectedDeliveryTime;
}


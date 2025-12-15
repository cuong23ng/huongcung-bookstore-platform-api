package com.huongcung.core.logistics.external.ghn.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GhnCalculateFeeDTO {
    private BigDecimal total;
    private BigDecimal serviceFee;
}


package com.huongcung.core.logistics.model.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CalculateFeeDTO {
    private BigDecimal total;
    private BigDecimal serviceFee;
}

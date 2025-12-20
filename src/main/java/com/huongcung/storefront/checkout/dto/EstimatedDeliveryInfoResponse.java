package com.huongcung.storefront.checkout.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Data
public class EstimatedDeliveryInfoResponse {
    private int warehouseCount;
    private LocalDate expectedDeliveryTime;
    private BigDecimal totalFee;
}

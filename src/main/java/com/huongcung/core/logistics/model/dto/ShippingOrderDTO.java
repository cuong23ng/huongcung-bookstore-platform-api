package com.huongcung.core.logistics.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShippingOrderDTO {
    private String orderCode;
    private String expectedDeliveryTime;
    private Integer totalFee;
}

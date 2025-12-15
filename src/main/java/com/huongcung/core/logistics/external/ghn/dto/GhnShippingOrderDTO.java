package com.huongcung.core.logistics.external.ghn.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GhnShippingOrderDTO {
    private String expectedDeliveryTime;
    private String orderCode;
    private Integer totalFee;
}

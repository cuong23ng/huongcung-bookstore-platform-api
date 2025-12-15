package com.huongcung.core.logistics.external.ghn.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GhnCalculateExpectedDeliveryTimeDTO {
    private String leadTime;
    private String orderDate;
}

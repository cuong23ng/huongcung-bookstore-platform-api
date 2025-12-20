package com.huongcung.core.logistics.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExpectedDeliveryTimeDTO {
    private String leadTime;
    private String orderDate;
}

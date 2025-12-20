package com.huongcung.core.order.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShipmentGroupDTO {
    private String warehouseCode;
    private List<Long> bookIds;
    private Double shippingFee;
    private Integer expectedLeadTime;
}

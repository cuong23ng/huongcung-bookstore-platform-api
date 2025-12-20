package com.huongcung.core.order.model.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllocationPlanDTO {
    private int warehouseCount;
    private int expectedDeliveryTime;
    private BigDecimal totalFee;
    private List<ShipmentGroupDTO> groups;
}

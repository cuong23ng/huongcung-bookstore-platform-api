package com.huongcung.core.inventory.model.domain;

import com.huongcung.core.common.model.domain.BaseDomain;
import com.huongcung.core.user.model.domain.Staff;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class StockAdjustment extends BaseDomain {
    private StockLevel stockLevel;
    private Integer previousQuantity;
    private Integer newQuantity;
    private String reason;
    private Staff adjustedBy;
    private LocalDateTime adjustedAt;

    public Integer getDifferentQuantity() {
        return (previousQuantity != null && newQuantity != null) ? newQuantity - previousQuantity : null;
    }
}

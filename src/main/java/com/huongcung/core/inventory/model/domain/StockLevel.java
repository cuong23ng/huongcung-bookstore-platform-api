package com.huongcung.core.inventory.model.domain;

import com.huongcung.core.catalog.model.domain.PhysicalBookInformation;
import com.huongcung.core.common.model.domain.BaseDomain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class StockLevel extends BaseDomain {
    private PhysicalBookInformation book;
    private Warehouse warehouse;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer reorderLevel = 5;
    private Integer reorderQuantity = 50;
    private LocalDateTime lastRestocked;

    public Integer getAvailableQuantity() {
        return this.quantity - this.reservedQuantity;
    }

    public boolean isLowStock() {
        return quantity != null &&
                reorderLevel != null &&
                quantity <= reorderLevel;
    }

    public boolean isOutOfStock() {
        return getAvailableQuantity() != null && getAvailableQuantity() <= 0;
    }
}

package com.huongcung.core.order.strategy;

import com.huongcung.core.logistics.model.dto.AddressDTO;
import com.huongcung.core.order.model.dto.AllocationPlanDTO;
import com.huongcung.core.order.model.entity.OrderEntryEntity;

import java.util.List;

public interface SplitOrderStrategy {
    AllocationPlanDTO simulateSplitOrder(List<OrderEntryEntity> items, AddressDTO customerAddress);
}

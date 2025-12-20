package com.huongcung.core.logistics.strategy;

import com.huongcung.core.logistics.model.entity.ConsignmentEntity;
import com.huongcung.core.order.model.entity.OrderEntity;

import java.util.List;

public interface SplitOrderStrategy {
    List<ConsignmentEntity> splitOrder(OrderEntity order);
}

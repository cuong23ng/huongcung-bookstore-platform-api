package com.huongcung.core.logistics.service;

import com.huongcung.core.logistics.enumeration.ConsignmentStatus;
import com.huongcung.core.logistics.model.entity.ConsignmentEntity;
import com.huongcung.core.order.model.entity.OrderEntity;

import java.util.List;

public interface LogisticsService {
    void fulfillOrder(Long orderId);
    void updateConsignmentStatusByTrackingNumber(String trackingNumber, ConsignmentStatus status);
    ConsignmentStatus mapGhnStatus(String ghnStatus);
    List<ConsignmentEntity> splitOrderStrategy(OrderEntity order);
}

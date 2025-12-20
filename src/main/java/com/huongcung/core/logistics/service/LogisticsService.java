package com.huongcung.core.logistics.service;

import com.huongcung.core.logistics.enumeration.ConsignmentStatus;
import com.huongcung.core.logistics.model.entity.ConsignmentEntity;

import java.util.List;

public interface LogisticsService {
    List<ConsignmentEntity> planFulfillment(Long orderId);
    void updateConsignmentStatusByTrackingNumber(String trackingNumber, ConsignmentStatus status);
    ConsignmentStatus mapGhnStatus(String ghnStatus);
}

package com.huongcung.core.logistics.service;

import com.huongcung.core.logistics.enumeration.ConsignmentStatus;

public interface LogisticsService {
    void fulfillOrder(Long orderId);
    void updateConsignmentStatusByTrackingNumber(String trackingNumber, ConsignmentStatus status);
}

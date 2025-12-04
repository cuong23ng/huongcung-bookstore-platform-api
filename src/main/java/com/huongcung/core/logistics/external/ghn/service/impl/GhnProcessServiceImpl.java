package com.huongcung.core.logistics.external.ghn.service.impl;

import com.huongcung.core.logistics.enumeration.ConsignmentStatus;
import com.huongcung.core.logistics.external.ghn.dto.WebhookDTO;
import com.huongcung.core.logistics.external.ghn.service.GhnProcessService;
import com.huongcung.core.logistics.service.LogisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GhnProcessServiceImpl implements GhnProcessService {

    private final LogisticsService logisticsService;

    @Override
    public void processUpdate(WebhookDTO data) {
        log.info("Received GHN update for tracking: {}, status: {}", data.getOrderCode(), data.getStatus());

        ConsignmentStatus newStatus = mapGhnStatus(data.getStatus());
        if (newStatus != null) {
            logisticsService.updateConsignmentStatusByTrackingNumber(data.getOrderCode(), newStatus);
        }
    }

    private ConsignmentStatus mapGhnStatus(String ghnStatus) {
        return switch (ghnStatus.toLowerCase()) {
            case "ready_to_pick" -> ConsignmentStatus.PENDING;
            case "picking", "picked" -> ConsignmentStatus.PICKED_UP;
            case "storing", "transporting", "sorting" -> ConsignmentStatus.IN_TRANSIT;
            case "delivering" -> ConsignmentStatus.OUT_FOR_DELIVERY;
            case "delivered" -> ConsignmentStatus.DELIVERED;
            case "delivery_fail" -> ConsignmentStatus.FAILED_DELIVERY;
            case "return", "returning", "returned" -> ConsignmentStatus.RETURNED;
            default -> null;
        };
    }
}

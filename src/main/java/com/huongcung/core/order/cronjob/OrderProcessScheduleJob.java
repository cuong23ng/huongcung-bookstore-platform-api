package com.huongcung.core.order.cronjob;

import com.huongcung.core.order.enumeration.OrderStatus;
import com.huongcung.core.order.model.entity.OrderEntity;
import com.huongcung.core.order.service.OrderConfirmationService;
import com.huongcung.core.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderProcessScheduleJob {

    private final OrderConfirmationService orderConfirmationService;
    private final OrderService orderService;

    @Scheduled(fixedRate = 60000)
    public void processPendingOrders() {
        log.info("OrderProcessScheduleJob PENDING Orders are processing!");
        List<OrderEntity> pendingOrders = orderService.findAllByStatus(OrderStatus.PENDING);
        for (OrderEntity order : pendingOrders) {
            orderConfirmationService.autoConfirmOrder(order.getId());
        }
    }
}

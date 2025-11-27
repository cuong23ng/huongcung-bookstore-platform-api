package com.huongcung.core.order.service.impl;

import com.huongcung.core.order.enumeration.OrderStatus;
import com.huongcung.core.order.enumeration.PaymentMethod;
import com.huongcung.core.order.enumeration.PaymentStatus;
import com.huongcung.core.order.event.OrderConfirmedEvent;
import com.huongcung.core.order.model.entity.OrderEntity;
import com.huongcung.core.order.repository.OrderRepository;
import com.huongcung.core.order.service.OrderConfirmationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderConfirmationServiceImpl implements OrderConfirmationService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public boolean autoConfirmOrder(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Check conditions
        if (!canAutoConfirm(order)) {
            log.info("Order {} cannot be auto-confirmed", orderId);
            return false;
        }

        // Confirm order
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        handleOrderConfirmed(order);

        log.info("Order {} auto-confirmed successfully", order.getOrderNumber());
        return true;
    }

    private boolean canAutoConfirm(OrderEntity order) {
        // 1. Status must be PENDING
        if (order.getStatus() != OrderStatus.PENDING) {
            return false;
        }

        // 2. Payment check
        if (order.getPaymentMethod() != PaymentMethod.COD
                && order.getPaymentStatus() != PaymentStatus.PAID) {
                return false;
        }

        // 3. Stock validation (check reserved quantities are still valid)
        if (!validateStockAvailability(order)) {
            return false;
        }

        return true;
    }

    private boolean validateStockAvailability(OrderEntity order) {
        // Check all physical items have sufficient stock
        // Implementation depends on your stock checking logic
        return true; // Simplified
    }

    @Transactional
    public void handleOrderConfirmed(OrderEntity order) {
        eventPublisher.publishEvent(new OrderConfirmedEvent(this, order));
    }
}

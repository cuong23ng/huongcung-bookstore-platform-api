package com.huongcung.core.order.service.impl;

import com.huongcung.core.order.enumeration.OrderStatus;
import com.huongcung.core.order.enumeration.PaymentStatus;
import com.huongcung.core.order.event.OrderConfirmedEvent;
import com.huongcung.core.order.model.entity.OrderEntity;
import com.huongcung.core.order.repository.OrderRepository;
import com.huongcung.core.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<OrderEntity> findAllByStatus(OrderStatus status) {
        return orderRepository.findAllByStatus(status);
    }

    @Override
    @Transactional
    public void handlePaymentSuccess(Long orderId) {
        log.info("Processing payment success for order ID: {}", orderId);

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            log.warn("Order {} is already paid. Skipping processing.", order.getOrderNumber());
            return;
        }

        order.setPaymentStatus(PaymentStatus.PAID);

        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
        }

        OrderEntity savedOrder = orderRepository.save(order);
        log.info("Order {} status updated to CONFIRMED and PAID.", savedOrder.getOrderNumber());

        eventPublisher.publishEvent(new OrderConfirmedEvent(this, savedOrder));
    }
}

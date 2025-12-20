package com.huongcung.core.payment.service.impl;

import com.huongcung.core.order.enumeration.OrderStatus;
import com.huongcung.core.order.enumeration.PaymentStatus;
import com.huongcung.core.order.event.OrderConfirmedEvent;
import com.huongcung.core.order.model.entity.OrderEntity;
import com.huongcung.core.order.repository.OrderRepository;
import com.huongcung.core.payment.service.PaymentConfirmationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentConfirmationServiceImpl implements PaymentConfirmationService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void handlePaymentSuccess(String orderNumber) {
        log.info("Processing payment success for order number: {}", orderNumber);

        OrderEntity order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with number: " + orderNumber));

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

    @Override
    public boolean checkValidOrderNumber(String orderNumber) {
        OrderEntity order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with number: " + orderNumber));
        return Objects.nonNull(order);
    }

    @Override
    public boolean checkReceivedAmountForOrder(String orderNumber, Long amount) {
        OrderEntity order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with number: " + orderNumber));
        return order.getTotalAmount().longValue() == amount;
    }
}

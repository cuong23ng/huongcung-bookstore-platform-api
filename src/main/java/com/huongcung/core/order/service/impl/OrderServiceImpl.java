package com.huongcung.core.order.service.impl;

import com.huongcung.core.order.enumeration.OrderStatus;
import com.huongcung.core.order.enumeration.PaymentStatus;
import com.huongcung.core.order.event.OrderConfirmedEvent;
import com.huongcung.core.order.model.entity.OrderEntity;
import com.huongcung.core.order.repository.OrderRepository;
import com.huongcung.core.order.service.OrderConfirmationService;
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

    @Override
    public List<OrderEntity> findAllByStatus(OrderStatus status) {
        return orderRepository.findAllByStatus(status);
    }
}

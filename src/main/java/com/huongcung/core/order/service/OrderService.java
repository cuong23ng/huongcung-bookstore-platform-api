package com.huongcung.core.order.service;

import com.huongcung.core.order.enumeration.OrderStatus;
import com.huongcung.core.order.model.entity.OrderEntity;

import java.util.List;

public interface OrderService {
    List<OrderEntity> findAllByStatus(OrderStatus status);

    void handlePaymentSuccess(Long orderId);
}

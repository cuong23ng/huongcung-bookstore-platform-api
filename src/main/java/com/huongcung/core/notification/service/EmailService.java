package com.huongcung.core.notification.service;

import com.huongcung.core.order.model.entity.OrderEntity;

public interface EmailService {
    void sendOrderConfirmationEmail(OrderEntity order);
}

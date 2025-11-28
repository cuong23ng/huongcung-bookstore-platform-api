package com.huongcung.core.order.service;

public interface OrderConfirmationService {

    boolean autoConfirmOrder(Long orderId);
}

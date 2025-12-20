package com.huongcung.core.payment.service.impl;

import com.huongcung.core.order.enumeration.OrderStatus;
import com.huongcung.core.order.enumeration.PaymentStatus;
import com.huongcung.core.order.event.OrderConfirmedEvent;
import com.huongcung.core.order.repository.OrderRepository;
import com.huongcung.core.order.service.OrderConfirmationService;
import com.huongcung.core.order.service.OrderService;
import com.huongcung.core.payment.configuration.VnpayConfig;
import com.huongcung.core.order.model.entity.OrderEntity;
import com.huongcung.core.payment.external.vnpay.service.VnPayService;
import com.huongcung.core.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final VnPayService vnPayService;
    private final OrderRepository orderRepository;

    public String createPaymentUrl(Long orderId, HttpServletRequest request) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String orderNumber = order.getOrderNumber();
        String ipAddress = VnpayConfig.getIpAddress(request);
        Long amount = order.getTotalAmount().longValue() * 100;

        return vnPayService.createPaymentUrl(orderNumber, amount, ipAddress);
    }
}
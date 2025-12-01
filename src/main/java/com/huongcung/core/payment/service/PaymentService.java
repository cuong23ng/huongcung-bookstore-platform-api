package com.huongcung.core.payment.service;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface PaymentService {
    String createPaymentUrl(Long orderId, HttpServletRequest request);

    Map<String, String> processIpn(Map<String, String> fields);
}

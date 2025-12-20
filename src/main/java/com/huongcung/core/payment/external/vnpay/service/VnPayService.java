package com.huongcung.core.payment.external.vnpay.service;

import java.util.Map;

public interface VnPayService {

    String createPaymentUrl(String orderId, Long amount, String ipAddress);

    Map<String, String> processIpn(Map<String, String> fields);
}

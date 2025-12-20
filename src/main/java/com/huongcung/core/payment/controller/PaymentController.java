package com.huongcung.core.payment.controller;

import com.huongcung.core.common.model.dto.response.BaseResponse;
import com.huongcung.core.order.model.entity.OrderEntity;
import com.huongcung.core.payment.external.vnpay.service.VnPayService;
import com.huongcung.core.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/create-payment/{orderId}")
    public ResponseEntity<BaseResponse> createPayment(@PathVariable Long orderId, HttpServletRequest request) {

        String paymentUrl = paymentService.createPaymentUrl(orderId, request);

        return ResponseEntity.ok(BaseResponse.builder()
                .message("Payment URL created")
                .data(paymentUrl)
                .build());
    }
}

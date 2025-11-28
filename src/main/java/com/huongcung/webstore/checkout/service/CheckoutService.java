package com.huongcung.webstore.checkout.service;

import com.huongcung.webstore.checkout.dto.CheckoutRequest;
import com.huongcung.webstore.checkout.dto.CheckoutResponse;

import java.util.List;

public interface CheckoutService {
    CheckoutResponse createOrder(CheckoutRequest request);
}

package com.huongcung.storefront.checkout.service;

import com.huongcung.core.logistics.model.dto.CalculateFeeDTO;
import com.huongcung.core.logistics.model.dto.request.CalculateFeeRequest;
import com.huongcung.storefront.checkout.dto.*;

import java.util.List;

public interface CheckoutService {
    CheckoutResponse createOrder(CheckoutRequest request);

    EstimatedDeliveryInfoResponse calculateEstimatedDeliveryFee(CalculateFeeRequest request);
}

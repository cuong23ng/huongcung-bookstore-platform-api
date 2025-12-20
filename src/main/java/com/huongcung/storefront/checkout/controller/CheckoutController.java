package com.huongcung.storefront.checkout.controller;

import com.huongcung.core.common.model.dto.response.BaseResponse;
import com.huongcung.core.logistics.model.dto.*;
import com.huongcung.core.logistics.model.dto.request.CalculateFeeRequest;
import com.huongcung.storefront.checkout.dto.*;
import com.huongcung.storefront.checkout.service.CheckoutService;
import com.huongcung.core.logistics.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/checkout")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class CheckoutController {
    
    private final CheckoutService checkoutService;
    private final DeliveryService deliveryService;

    @GetMapping("/services")
    public ResponseEntity<BaseResponse> getServices() {
        List<ServiceDTO> services = deliveryService.getServices(null);
        return ResponseEntity.ok(BaseResponse.builder()
                .data(services)
                .build());
    }

    @GetMapping("/provinces")
    public ResponseEntity<BaseResponse> getProvinces() {
        List<ProvinceDTO> provinces = deliveryService.getProvinces();
        return ResponseEntity.ok(BaseResponse.builder()
                .data(provinces)
                .build());
    }

    @GetMapping("/districts")
    public ResponseEntity<BaseResponse> getDistricts(@RequestParam String provinceId) {
        List<DistrictDTO> districts = deliveryService.getDistricts(provinceId);
        return ResponseEntity.ok(BaseResponse.builder()
                .data(districts)
                .build());
    }

    @GetMapping("/wards")
    public ResponseEntity<BaseResponse> getWards(@RequestParam String districtId) {
        List<WardDTO> wards = deliveryService.getWards(districtId);
        return ResponseEntity.ok(BaseResponse.builder()
                .data(wards)
                .build());
    }
    
    @PostMapping("/calculate-fee")
    public ResponseEntity<BaseResponse> calculateFee(@Valid @RequestBody CalculateFeeRequest request) {
        EstimatedDeliveryInfoResponse dto = checkoutService.calculateEstimatedDeliveryFee(request);
        return ResponseEntity.ok(BaseResponse.builder()
                .data(dto)
                .build());
    }
    
    @PostMapping("/orders")
    public ResponseEntity<BaseResponse> createOrder(@Valid @RequestBody CheckoutRequest request) {
        CheckoutResponse response = checkoutService.createOrder(request);

        return ResponseEntity.ok(BaseResponse.builder()
            .data(response)
            .build());
    }
}


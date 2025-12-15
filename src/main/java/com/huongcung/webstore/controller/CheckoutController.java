package com.huongcung.webstore.controller;

import com.huongcung.core.common.model.dto.response.BaseResponse;
import com.huongcung.core.logistics.model.dto.*;
import com.huongcung.core.logistics.model.dto.request.CalculateFeeRequest;
import com.huongcung.webstore.checkout.dto.*;
import com.huongcung.webstore.checkout.service.CheckoutService;
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

    @GetMapping("/ghn/services")
    public ResponseEntity<BaseResponse> getServices() {
        List<ServiceDTO> services = deliveryService.getServices(null);
        return ResponseEntity.ok(BaseResponse.builder()
                .data(services)
                .build());
    }

    @GetMapping("/ghn/provinces")
    public ResponseEntity<BaseResponse> getProvinces() {
        List<ProvinceDTO> provinces = deliveryService.getProvinces();
        return ResponseEntity.ok(BaseResponse.builder()
                .data(provinces)
                .build());
    }
    
    @GetMapping("/ghn/districts")
    public ResponseEntity<BaseResponse> getDistricts(@RequestParam String provinceId) {
        List<DistrictDTO> districts = deliveryService.getDistricts(provinceId);
        return ResponseEntity.ok(BaseResponse.builder()
                .data(districts)
                .build());
    }
    
    @GetMapping("/ghn/wards")
    public ResponseEntity<BaseResponse> getWards(@RequestParam String districtId) {
        List<WardDTO> wards = deliveryService.getWards(districtId);
        return ResponseEntity.ok(BaseResponse.builder()
                .data(wards)
                .build());
    }
    
    @PostMapping("/ghn/calculate-fee")
    public ResponseEntity<BaseResponse> calculateFee(@Valid @RequestBody CalculateFeeRequest request) {
        CalculateFeeDTO dto = deliveryService.calculateEstimatedDeliveryFee(request.getServiceTypeId(), request.getDistrictId(), request.getWardCode(), request.getWeight());
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


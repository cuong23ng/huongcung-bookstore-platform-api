package com.huongcung.webstore.controller;

import com.huongcung.core.common.model.dto.response.BaseResponse;
import com.huongcung.webstore.checkout.dto.*;
import com.huongcung.webstore.checkout.external.ghn.dto.*;
import com.huongcung.webstore.checkout.service.CheckoutService;
import com.huongcung.webstore.checkout.service.DeliveryService;
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
        List<GhnServiceDTO> services = deliveryService.getServices();
        return ResponseEntity.ok(BaseResponse.builder()
                .data(services)
                .build());
    }

    @GetMapping("/ghn/provinces")
    public ResponseEntity<BaseResponse> getProvinces() {
        List<GhnProvinceDTO> provinces = deliveryService.getProvinces();
        return ResponseEntity.ok(BaseResponse.builder()
                .data(provinces)
                .build());
    }
    
    @GetMapping("/ghn/districts")
    public ResponseEntity<BaseResponse> getDistricts(@RequestParam Integer provinceId) {
        List<GhnDistrictDTO> districts = deliveryService.getDistricts(provinceId);
        return ResponseEntity.ok(BaseResponse.builder()
                .data(districts)
                .build());
    }
    
    @GetMapping("/ghn/wards")
    public ResponseEntity<BaseResponse> getWards(@RequestParam Integer districtId) {
        List<GhnWardDTO> wards = deliveryService.getWards(districtId);
        return ResponseEntity.ok(BaseResponse.builder()
                .data(wards)
                .build());
    }
    
    @PostMapping("/ghn/calculate-fee")
    public ResponseEntity<BaseResponse> calculateFee(@Valid @RequestBody CalculateFeeRequestDTO request) {
        CalculateFeeResponseDTO dto = deliveryService.calculateEstimatedDeliveryFee(request.getServiceTypeId(), request.getDistrictId(), request.getWardCode(), request.getWeight());
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


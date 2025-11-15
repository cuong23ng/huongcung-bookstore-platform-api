package com.huongcung.webstore.controller;

import com.huongcung.core.common.model.response.BaseResponse;
import com.huongcung.core.security.model.dto.CustomUserDetails;
import com.huongcung.webstore.checkout.dto.*;
import com.huongcung.webstore.checkout.external.ghn.GhnApiClient;
import com.huongcung.webstore.checkout.external.ghn.dto.*;
import com.huongcung.webstore.checkout.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/checkout")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class CheckoutController {
    
    private final GhnApiClient ghnApiClient;
    private final CheckoutService checkoutService;
    
    @GetMapping("/ghn/provinces")
    public ResponseEntity<BaseResponse> getProvinces() {
        try {
            List<GhnProvinceDTO> provinces = ghnApiClient.getProvinces();
            return ResponseEntity.ok(BaseResponse.builder()
                .data(provinces)
                .build());
        } catch (GhnApiClient.GhnApiException e) {
            log.error("Error fetching provinces: {}", e.getMessage());
            return ResponseEntity.ok(BaseResponse.builder()
                .errorCode("GHN_API_ERROR")
                .message("Failed to fetch provinces: " + e.getMessage())
                .data(List.of())
                .build());
        }
    }
    
    @GetMapping("/ghn/districts")
    public ResponseEntity<BaseResponse> getDistricts(@RequestParam Integer provinceId) {
        try {
            List<GhnDistrictDTO> districts = ghnApiClient.getDistricts(provinceId);
            return ResponseEntity.ok(BaseResponse.builder()
                .data(districts)
                .build());
        } catch (GhnApiClient.GhnApiException e) {
            log.error("Error fetching districts: {}", e.getMessage());
            return ResponseEntity.ok(BaseResponse.builder()
                .errorCode("GHN_API_ERROR")
                .message("Failed to fetch districts: " + e.getMessage())
                .data(List.of())
                .build());
        }
    }
    
    @GetMapping("/ghn/wards")
    public ResponseEntity<BaseResponse> getWards(@RequestParam Integer districtId) {
        try {
            List<GhnWardDTO> wards = ghnApiClient.getWards(districtId);
            return ResponseEntity.ok(BaseResponse.builder()
                .data(wards)
                .build());
        } catch (GhnApiClient.GhnApiException e) {
            log.error("Error fetching wards: {}", e.getMessage());
            return ResponseEntity.ok(BaseResponse.builder()
                .errorCode("GHN_API_ERROR")
                .message("Failed to fetch wards: " + e.getMessage())
                .data(List.of())
                .build());
        }
    }
    
    @PostMapping("/ghn/calculate-fee")
    public ResponseEntity<BaseResponse> calculateFee(@Valid @RequestBody CalculateFeeRequestDTO request) {
        try {
            CalculateFeeRequest ghnRequest = CalculateFeeRequest.builder()
                .serviceTypeId(request.getServiceTypeId() != null ? request.getServiceTypeId() : 2)
                .serviceId(53321) // Standard service
                .toDistrictId(request.getDistrictId())
                .toWardCode(request.getWardCode())
                .weight(request.getWeight())
                .length(20)
                .width(15)
                .height(5)
                .build();
            
            CalculateFeeResponse response = ghnApiClient.calculateFee(ghnRequest);
            
            CalculateFeeResponseDTO dto = CalculateFeeResponseDTO.builder()
                .total(response.getTotal())
                .serviceFee(response.getServiceFee())
                .expectedDeliveryTime("2-3 days") // GHN API doesn't always return this
                .build();
            
            return ResponseEntity.ok(BaseResponse.builder()
                .data(dto)
                .build());
        } catch (GhnApiClient.GhnApiException e) {
            log.error("Error calculating fee: {}", e.getMessage());
            return ResponseEntity.ok(BaseResponse.builder()
                .errorCode("GHN_API_ERROR")
                .message("Failed to calculate fee: " + e.getMessage())
                .build());
        }
    }
    
    @PostMapping("/orders")
    public ResponseEntity<BaseResponse> createOrder(@Valid @RequestBody CheckoutRequest request) {
        try {
            // Get customer ID from authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long customerId = null;
            
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                customerId = userDetails.getId();
                log.debug("Extracted customer ID from authentication: {}", customerId);
            }
            
            if (customerId == null) {
                log.warn("No customer ID found in authentication");
                return ResponseEntity.status(401).body(BaseResponse.builder()
                    .errorCode("UNAUTHORIZED")
                    .message("Customer authentication required")
                    .build());
            }
            
            CheckoutResponse response = checkoutService.createOrder(request, customerId);
            
            return ResponseEntity.ok(BaseResponse.builder()
                .data(response)
                .build());
        } catch (IllegalArgumentException e) {
            log.error("Invalid checkout request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .message(e.getMessage())
                .build());
        } catch (Exception e) {
            log.error("Error creating order: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(BaseResponse.builder()
                .errorCode("INTERNAL_ERROR")
                .message("Failed to create order: " + e.getMessage())
                .build());
        }
    }
}


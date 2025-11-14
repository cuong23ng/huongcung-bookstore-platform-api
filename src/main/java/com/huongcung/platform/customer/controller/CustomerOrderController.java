package com.huongcung.platform.customer.controller;

import com.huongcung.core.common.model.response.BaseResponse;
import com.huongcung.platform.auth.dto.CustomUserDetails;
import com.huongcung.platform.customer.dto.OrderDetailsDTO;
import com.huongcung.platform.customer.dto.OrderHistoryDTO;
import com.huongcung.platform.customer.service.OrderHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class CustomerOrderController {
    
    private final OrderHistoryService orderHistoryService;
    
    @GetMapping
    public ResponseEntity<BaseResponse> getOrderHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
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
            
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderHistoryDTO> orders = orderHistoryService.getOrderHistory(customerId, pageable);
            
            return ResponseEntity.ok(BaseResponse.builder()
                .data(orders)
                .build());
        } catch (Exception e) {
            log.error("Error fetching order history: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(BaseResponse.builder()
                .errorCode("INTERNAL_ERROR")
                .message("Failed to fetch order history: " + e.getMessage())
                .build());
        }
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<BaseResponse> getOrderDetails(@PathVariable Long orderId) {
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
            
            OrderDetailsDTO orderDetails = orderHistoryService.getOrderDetails(orderId, customerId);
            
            return ResponseEntity.ok(BaseResponse.builder()
                .data(orderDetails)
                .build());
        } catch (SecurityException e) {
            log.warn("Access denied for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(403).body(BaseResponse.builder()
                .errorCode("FORBIDDEN")
                .message("Access denied")
                .build());
        } catch (IllegalArgumentException e) {
            log.error("Order not found: {}", orderId);
            return ResponseEntity.status(404).body(BaseResponse.builder()
                .errorCode("NOT_FOUND")
                .message(e.getMessage())
                .build());
        } catch (Exception e) {
            log.error("Error fetching order details: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(BaseResponse.builder()
                .errorCode("INTERNAL_ERROR")
                .message("Failed to fetch order details: " + e.getMessage())
                .build());
        }
    }
}


package com.huongcung.businessmanagement.controller;

import com.huongcung.businessmanagement.admin.service.AdminOrderService;
import com.huongcung.businessmanagement.fulfillment.model.ConsignmentDTO;
import com.huongcung.businessmanagement.fulfillment.model.ConsignmentShipRequest;
import com.huongcung.businessmanagement.fulfillment.service.FulfillmentService;
import com.huongcung.core.common.enumeration.City;
import com.huongcung.core.common.model.dto.response.BaseResponse;
import com.huongcung.core.logistics.enumeration.ConsignmentStatus;
import com.huongcung.core.logistics.model.entity.ConsignmentEntity;
import com.huongcung.core.logistics.service.impl.LogisticsServiceImpl;
import com.huongcung.core.order.enumeration.OrderStatus;
import com.huongcung.core.security.model.dto.CustomUserDetails;
import com.huongcung.webstore.customer.dto.OrderDetailsDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller for Admin order operations
 * Handles fulfillment queue viewing for all cities with optional city filtering
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminOrderController {
    
    private final FulfillmentService fulfillmentService;
    private final LogisticsServiceImpl fulfillmentServiceImplv2;
    private final AdminOrderService adminOrderService;
    
    /**
     * Get fulfillment queue for all cities (or filtered by city)
     * Admin can view all confirmed orders or filter by specific city
     * 
     * @param pageable pagination parameters (page, size, sort) - defaults to page=0, size=20
     * @param city optional filter by city (e.g., "Hanoi", "HCMC", "DaNang") - if not provided, returns all cities
     * @param fromDate optional filter by order date from (format: yyyy-MM-dd)
     * @param toDate optional filter by order date to (format: yyyy-MM-dd)
     * @param sortBy optional sort field (orderDate, totalAmount) - defaults to orderDate
     * @return BaseResponse containing paginated fulfillment queue
     */
    @GetMapping("/fulfillment-queue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse> getFulfillmentQueue(
            @PageableDefault(size = 20, page = 0) Pageable pageable,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String sortBy) {
        
        log.info("Admin requesting fulfillment queue - city: {}, page: {}, size: {}, fromDate: {}, toDate: {}, sortBy: {}",
                city, pageable.getPageNumber(), pageable.getPageSize(), fromDate, toDate, sortBy);
        
        // Convert city string to City enum if provided
        City cityEnum = null;
        if (city != null && !city.isBlank()) {
            try {
                cityEnum = City.valueOf(city.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid city value: {}", city);
                throw new IllegalArgumentException("Invalid city: " + city);
            }
        }
        
        FulfillmentService.PaginatedFulfillmentQueueResponse response = 
                fulfillmentService.getFulfillmentQueue(cityEnum, pageable, fromDate, toDate, sortBy);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(Map.of(
                        "orders", response.orders(),
                        "pagination", response.pagination()
                ))
                .build());
    }

    /**
     * Plan fulfillment for an order (consignment splitting)
     * Creates consignments based on stock availability and optimization
     * 
     * @param orderId the order ID to plan fulfillment for
     * @return BaseResponse with created consignments
     */
    @PostMapping("/{orderId}/plan-fulfillment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse> planFulfillment(@PathVariable Long orderId) {
        log.info("Planning fulfillment for order ID: {}", orderId);
        
        List<ConsignmentEntity> consignments = fulfillmentServiceImplv2.planFulfillment(orderId);
        
        // Convert to DTOs using AdminOrderService
        List<ConsignmentDTO> consignmentDTOs = consignments.stream()
                .map(adminOrderService::toConsignmentDTO)
                .toList();
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.builder()
                        .message("Fulfillment planned successfully. Consignments created and stock reserved.")
                        .data(consignmentDTOs)
                        .build());
    }
    
    /**
     * Create shipping order for a consignment
     * This endpoint sends a request to GHN to create a shipping order for a consignment
     * 
     * @param consignmentId the consignment ID
     * @return BaseResponse with tracking number
     */
    @PostMapping("/consignments/{consignmentId}/create-shipping-order")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse> createShippingOrderForConsignment(@PathVariable Long consignmentId) {
        log.info("Creating shipping order for consignment ID: {}", consignmentId);
        
        try {
            String trackingNumber = fulfillmentServiceImplv2.createShippingOrderForConsignment(consignmentId);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.builder()
                            .message("Shipping order created successfully")
                            .data(Map.of("trackingNumber", trackingNumber))
                            .build());
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.error("Error creating shipping order for consignment {}: {}", consignmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(BaseResponse.builder()
                            .errorCode("INVALID_CONSIGNMENT_STATUS")
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Unexpected error creating shipping order for consignment {}: {}", consignmentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.builder()
                            .errorCode("INTERNAL_ERROR")
                            .message("Failed to create shipping order: " + e.getMessage())
                            .build());
        }
    }
    
    /**
     * Fulfill order (legacy endpoint - calls plan-fulfillment internally)
     * @deprecated Use /{orderId}/plan-fulfillment instead
     */
    @PostMapping("/fulfill")
    @PreAuthorize("hasRole('ADMIN')")
    @Deprecated
    public ResponseEntity<BaseResponse> fulfillOrder(
            @RequestParam(required = true) Long orderId) {
        return planFulfillment(orderId);
    }
    
    /**
     * Get consignments for all cities (or filtered by city)
     * Admin can view all consignments or filter by specific city and status
     * 
     * @param pageable pagination parameters (page, size, sort) - defaults to page=0, size=20
     * @param city optional filter by city (e.g., "Hanoi", "HCMC", "DaNang") - if not provided, returns all cities
     * @param status optional filter by status (default: PENDING)
     * @return BaseResponse containing paginated consignments
     */
    @GetMapping("/consignments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse> getConsignments(
            @PageableDefault(size = 20, page = 0) Pageable pageable,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) ConsignmentStatus status) {
        
        log.info("Admin requesting consignments - city: {}, status: {}, page: {}, size: {}",
                city, status, pageable.getPageNumber(), pageable.getPageSize());
        
        // Convert city string to City enum if provided
        City cityEnum = null;
        if (city != null && !city.isBlank()) {
            try {
                cityEnum = City.valueOf(city.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid city value: {}", city);
                throw new IllegalArgumentException("Invalid city: " + city);
            }
        }
        
        FulfillmentService.PaginatedConsignmentResponse response = 
                fulfillmentService.getConsignments(cityEnum, status, pageable);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(Map.of(
                        "consignments", response.consignments(),
                        "pagination", response.pagination()
                ))
                .build());
    }
    
    /**
     * Ship a consignment (update status and commit stock)
     * Admin can ship consignments from any city
     * 
     * @param consignmentId the consignment ID
     * @param request the ship request with tracking number, shipping company, status, and estimated delivery date
     * @return BaseResponse
     */
    @PutMapping("/consignments/{consignmentId}/ship")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse> shipConsignment(
            @PathVariable Long consignmentId,
            @Valid @RequestBody ConsignmentShipRequest request) {
        
        log.info("Admin shipping consignment {}, status: {}",
                consignmentId, request.getStatus());
        
        // Get current authenticated user ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long shippedBy = userDetails.getId();
        
        fulfillmentService.shipConsignment(consignmentId, request, shippedBy);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .message("Consignment shipped successfully")
                .build());
    }
    
    /**
     * Get all orders with pagination and filters for admin
     * 
     * @param pageable pagination parameters (page, size, sort) - defaults to page=0, size=20
     * @param status optional status filter (e.g., "PENDING", "CONFIRMED", "DELIVERED")
     * @param city optional city filter (e.g., "HANOI", "HCMC", "DANANG")
     * @return BaseResponse containing paginated orders
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse> getAllOrders(
            @PageableDefault(size = 20, page = 0) Pageable pageable,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String city) {
        
        log.info("Admin requesting all orders - status: {}, city: {}, page: {}, size: {}",
                status, city, pageable.getPageNumber(), pageable.getPageSize());
        
        // Convert status string to OrderStatus enum if provided
        OrderStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status value: {}", status);
                throw new IllegalArgumentException("Invalid status: " + status);
            }
        }
        
        // Convert city string to City enum if provided
        City cityEnum = null;
        if (city != null && !city.isBlank()) {
            try {
                cityEnum = City.valueOf(city.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid city value: {}", city);
                throw new IllegalArgumentException("Invalid city: " + city);
            }
        }
        
        var ordersPage = adminOrderService.getAllOrders(pageable, statusEnum, cityEnum);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(Map.of(
                        "orders", ordersPage.getContent(),
                        "pagination", Map.of(
                                "page", ordersPage.getNumber(),
                                "size", ordersPage.getSize(),
                                "totalElements", ordersPage.getTotalElements(),
                                "totalPages", ordersPage.getTotalPages()
                        )
                ))
                .build());
    }
    
    /**
     * Get order details by ID for admin
     * 
     * @param orderId the order ID
     * @return BaseResponse containing order details
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse> getOrderDetails(@PathVariable Long orderId) {
        log.info("Admin requesting order details for order ID: {}", orderId);
        
        OrderDetailsDTO orderDetails = adminOrderService.getOrderDetails(orderId);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(Map.of("order", orderDetails))
                .build());
    }
}


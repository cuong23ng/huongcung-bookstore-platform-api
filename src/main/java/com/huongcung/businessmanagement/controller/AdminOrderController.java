package com.huongcung.businessmanagement.controller;

import com.huongcung.businessmanagement.fulfillment.service.FulfillmentService;
import com.huongcung.core.common.enumeration.City;
import com.huongcung.core.common.model.dto.response.BaseResponse;
import com.huongcung.core.logistics.service.impl.LogisticsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    @PostMapping("/fulfill")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse> fulfillOrder(
            @RequestParam(required = true) Long orderId) {

        fulfillmentServiceImplv2.fulfillOrder(orderId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.builder()
                        .message("Fulfill Order successfully")
                        .build());
    }
}


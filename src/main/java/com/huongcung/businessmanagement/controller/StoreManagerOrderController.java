package com.huongcung.businessmanagement.controller;

import com.huongcung.businessmanagement.fulfillment.model.ConsignmentShipRequest;
import com.huongcung.businessmanagement.fulfillment.service.FulfillmentService;
import com.huongcung.core.common.enumeration.City;
import com.huongcung.core.common.model.dto.response.BaseResponse;
import com.huongcung.core.logistics.enumeration.ConsignmentStatus;
import com.huongcung.core.security.model.dto.CustomUserDetails;
import com.huongcung.core.user.model.entity.StaffEntity;
import com.huongcung.core.user.repository.StaffRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * REST controller for Store Manager order operations
 * Handles fulfillment queue viewing with automatic city filtering
 */
@RestController
@RequestMapping("/api/store-manager/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class StoreManagerOrderController {
    
    private final FulfillmentService fulfillmentService;
    private final StaffRepository staffRepository;
    
    /**
     * Get fulfillment queue for Store Manager's assigned city
     * Automatically filters by Store Manager's assignedCity
     * 
     * @param pageable pagination parameters (page, size, sort) - defaults to page=0, size=20
     * @param fromDate optional filter by order date from (format: yyyy-MM-dd)
     * @param toDate optional filter by order date to (format: yyyy-MM-dd)
     * @param sortBy optional sort field (orderDate, totalAmount) - defaults to orderDate
     * @return BaseResponse containing paginated fulfillment queue
     */
    @GetMapping("/fulfillment-queue")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    public ResponseEntity<BaseResponse> getFulfillmentQueue(
            @PageableDefault(size = 20, page = 0) Pageable pageable,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String sortBy) {
        
        log.debug("Store Manager requesting fulfillment queue - page: {}, size: {}, fromDate: {}, toDate: {}, sortBy: {}",
                pageable.getPageNumber(), pageable.getPageSize(), fromDate, toDate, sortBy);
        
        // Get current authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        // Get StaffEntity to retrieve assignedCity
        StaffEntity staff = staffRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Staff not found with email: " + email));
        
        if (staff.getAssignedCity() == null) {
            throw new IllegalStateException("Store Manager does not have an assigned city");
        }
        
        City assignedCity = staff.getAssignedCity();
        
        log.debug("Store Manager assigned city: {}", assignedCity);
        
        FulfillmentService.PaginatedFulfillmentQueueResponse response = 
                fulfillmentService.getFulfillmentQueue(assignedCity, pageable, fromDate, toDate, sortBy);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(Map.of(
                        "orders", response.orders(),
                        "pagination", response.pagination()
                ))
                .build());
    }
    
    /**
     * Get consignments for Store Manager's assigned city
     * Automatically filters by Store Manager's assignedCity and status = PENDING
     * 
     * @param pageable pagination parameters (page, size, sort) - defaults to page=0, size=20
     * @param status optional status filter (default: PENDING)
     * @return BaseResponse containing paginated consignments
     */
    @GetMapping("/consignments")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    public ResponseEntity<BaseResponse> getConsignments(
            @PageableDefault(size = 20, page = 0) Pageable pageable,
            @RequestParam(required = false) ConsignmentStatus status) {
        
        log.info("Store Manager requesting consignments - page: {}, size: {}, status: {}",
                pageable.getPageNumber(), pageable.getPageSize(), status);
        
        // Get current authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        // Get StaffEntity to retrieve assignedCity
        StaffEntity staff = staffRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Staff not found with email: " + email));
        
        if (staff.getAssignedCity() == null) {
            throw new IllegalStateException("Store Manager does not have an assigned city");
        }
        
        City assignedCity = staff.getAssignedCity();
        
        log.debug("Store Manager assigned city: {}", assignedCity);
        
        // Default status to PENDING if not provided
        ConsignmentStatus filterStatus = status != null ? status : ConsignmentStatus.PENDING;
        
        FulfillmentService.PaginatedConsignmentResponse response = 
                fulfillmentService.getConsignments(assignedCity, filterStatus, pageable);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(Map.of(
                        "consignments", response.consignments(),
                        "pagination", response.pagination()
                ))
                .build());
    }
    
    /**
     * Ship a consignment (update status and commit stock)
     * 
     * @param consignmentId the consignment ID
     * @param request the ship request with tracking number, shipping company, status, and estimated delivery date
     * @return BaseResponse
     */
    @PutMapping("/consignments/{consignmentId}/ship")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    public ResponseEntity<BaseResponse> shipConsignment(
            @PathVariable Long consignmentId,
            @Valid @RequestBody ConsignmentShipRequest request) {
        
        log.info("Store Manager shipping consignment {}, status: {}",
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
}


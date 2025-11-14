package com.huongcung.businessmanagement.controller;

import com.huongcung.businessmanagement.admin.model.StaffCreateRequest;
import com.huongcung.businessmanagement.admin.model.StaffDTO;
import com.huongcung.businessmanagement.admin.model.StaffUpdateRequest;
import com.huongcung.businessmanagement.admin.service.StaffService;
import com.huongcung.core.common.model.response.BaseResponse;
import com.huongcung.core.user.enumeration.StaffType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for Admin staff management operations
 * All endpoints require ADMIN role (enforced by Spring Security /api/admin/** pattern)
 */
@RestController
@RequestMapping("api/admin/staff")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminStaffController {
    
    private final StaffService staffService;
    
    /**
     * Create a new staff account
     * 
     * @param request the staff creation request containing email, password, firstName, lastName, phone, staffType, and optional assignedCity
     * @return BaseResponse containing StaffDTO with created staff information (excludes passwordHash)
     */
    @PostMapping
    public ResponseEntity<BaseResponse> createStaff(@Valid @RequestBody StaffCreateRequest request) {
        log.info("Creating staff account for email: {}, staffType: {}", request.getEmail(), request.getStaffType());
        
        StaffDTO staffDTO = staffService.createStaff(request);
        
        log.info("Staff account created successfully with ID: {}", staffDTO.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.builder()
                        .data(staffDTO)
                        .message("Staff account created successfully")
                        .build());
    }
    
    /**
     * Get paginated list of all staff accounts with optional filtering
     * 
     * @param pageable pagination parameters (page, size, sort) - defaults to page=0, size=20
     * @param staffType optional filter by staff type (STORE_MANAGER, SUPPORT_AGENT)
     * @param assignedCity optional filter by assigned city (Hanoi, HCMC, DaNang)
     * @return BaseResponse containing paginated list of StaffDTO with PaginationInfo
     */
    @GetMapping
    public ResponseEntity<BaseResponse> getAllStaff(
            @PageableDefault(size = 20, page = 0) Pageable pageable,
            @RequestParam(required = false) StaffType staffType,
            @RequestParam(required = false) String assignedCity) {
        
        log.debug("Fetching staff list - page: {}, size: {}, staffType: {}, assignedCity: {}", 
                pageable.getPageNumber(), pageable.getPageSize(), staffType, assignedCity);
        
        StaffService.PaginatedStaffResponse response = staffService.getAllStaff(pageable, staffType, assignedCity);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(Map.of(
                        "staff", response.staff(),
                        "pagination", response.pagination()
                ))
                .build());
    }
    
    /**
     * Get staff account by ID
     * 
     * @param id the staff ID
     * @return BaseResponse containing StaffDTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> getStaffById(@PathVariable Long id) {
        log.debug("Fetching staff by ID: {}", id);
        
        StaffDTO staffDTO = staffService.getStaffById(id);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(staffDTO)
                .build());
    }
    
    /**
     * Update staff account
     * 
     * @param id the staff ID
     * @param request the update request (partial update - only non-null fields are updated)
     * @return BaseResponse containing updated StaffDTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse> updateStaff(
            @PathVariable Long id,
            @Valid @RequestBody StaffUpdateRequest request) {
        
        String updatedBy = getCurrentAdminEmail();
        log.info("Updating staff account ID: {}, updatedBy: {}", id, updatedBy);
        
        StaffDTO staffDTO = staffService.updateStaff(id, request, updatedBy);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(staffDTO)
                .message("Staff account updated successfully")
                .build());
    }
    
    /**
     * Deactivate staff account
     * 
     * @param id the staff ID
     * @return BaseResponse with success message
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<BaseResponse> deactivateStaff(@PathVariable Long id) {
        String deactivatedBy = getCurrentAdminEmail();
        log.info("Deactivating staff account ID: {}, deactivatedBy: {}", id, deactivatedBy);
        
        StaffDTO staffDTO = staffService.deactivateStaff(id, deactivatedBy);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(staffDTO)
                .message("Staff account deactivated successfully")
                .build());
    }
    
    /**
     * Extract current admin user email from SecurityContext for audit logging
     * @return the email of the currently authenticated admin user
     */
    private String getCurrentAdminEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName(); // Returns the email (username)
        }
        return "system"; // Fallback if no authentication found
    }
}


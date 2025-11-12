package com.huongcung.businessmanagement.admin.service;

import com.huongcung.businessmanagement.admin.model.StaffCreateRequest;
import com.huongcung.businessmanagement.admin.model.StaffDTO;
import com.huongcung.businessmanagement.admin.model.StaffUpdateRequest;
import com.huongcung.core.search.model.dto.PaginationInfo;
import com.huongcung.core.user.enumeration.StaffType;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StaffService {
    
    /**
     * Create a new staff account
     * @param request the staff creation request
     * @return StaffDTO containing the created staff information (excludes passwordHash)
     * @throws IllegalArgumentException if validation fails (invalid staffType, missing assignedCity for STORE_MANAGER, etc.)
     * @throws RuntimeException if email already exists
     */
    StaffDTO createStaff(StaffCreateRequest request);
    
    /**
     * Get paginated list of all staff accounts with optional filtering
     * @param pageable pagination parameters (page, size, sort)
     * @param staffType optional filter by staff type
     * @param assignedCity optional filter by assigned city
     * @return PaginatedStaffResponse containing list of StaffDTO and PaginationInfo
     */
    PaginatedStaffResponse getAllStaff(Pageable pageable, StaffType staffType, String assignedCity);
    
    /**
     * Get staff account by ID
     * @param id the staff ID
     * @return StaffDTO containing staff information
     * @throws RuntimeException if staff not found
     */
    StaffDTO getStaffById(Long id);
    
    /**
     * Update staff account
     * @param id the staff ID
     * @param request the update request (partial update - only non-null fields are updated)
     * @param updatedBy the admin user ID who made the update (for audit logging)
     * @return StaffDTO containing updated staff information
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException if staff not found
     */
    StaffDTO updateStaff(Long id, StaffUpdateRequest request, String updatedBy);
    
    /**
     * Deactivate staff account
     * @param id the staff ID
     * @param deactivatedBy the admin user ID who deactivated the account (for audit logging)
     * @return StaffDTO containing deactivated staff information
     * @throws RuntimeException if staff not found
     */
    StaffDTO deactivateStaff(Long id, String deactivatedBy);
    
    /**
     * Response wrapper for paginated staff list
     */
    record PaginatedStaffResponse(List<StaffDTO> staff, PaginationInfo pagination) {}
}


package com.huongcung.businessmanagement.admin.service.impl;

import com.huongcung.businessmanagement.admin.mapper.StaffMapper;
import com.huongcung.businessmanagement.admin.model.StaffCreateRequest;
import com.huongcung.businessmanagement.admin.model.StaffDTO;
import com.huongcung.businessmanagement.admin.model.StaffUpdateRequest;
import com.huongcung.businessmanagement.admin.service.StaffService;
import com.huongcung.core.search.model.dto.PaginationInfo;
import com.huongcung.core.user.enumeration.StaffType;
import com.huongcung.core.user.model.entity.StaffEntity;
import com.huongcung.core.user.repository.StaffRepository;
import com.huongcung.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffServiceImpl implements StaffService {
    
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final StaffMapper staffMapper;
    
    private static final List<String> VALID_CITIES = Arrays.asList("Hanoi", "HCMC", "DaNang");
    
    @Override
    @Transactional
    public StaffDTO createStaff(StaffCreateRequest request) {
        log.info("Creating staff account for email: {}, staffType: {}", request.getEmail(), request.getStaffType());
        
        // Validate staffType is not ADMIN
        if (request.getStaffType() == StaffType.ADMIN) {
            throw new IllegalArgumentException("ADMIN staff type cannot be created via this endpoint. Admins must be created separately.");
        }
        
        // Validate staffType is either STORE_MANAGER or SUPPORT_AGENT
        if (request.getStaffType() != StaffType.STORE_MANAGER && request.getStaffType() != StaffType.SUPPORT_AGENT) {
            throw new IllegalArgumentException("Staff type must be either STORE_MANAGER or SUPPORT_AGENT");
        }
        
        // Validate assignedCity for STORE_MANAGER
        if (request.getStaffType() == StaffType.STORE_MANAGER) {
            if (request.getAssignedCity() == null || request.getAssignedCity().isBlank()) {
                throw new IllegalArgumentException("assignedCity is required when staffType is STORE_MANAGER");
            }
            if (!VALID_CITIES.contains(request.getAssignedCity())) {
                throw new IllegalArgumentException("assignedCity must be one of: Hanoi, HCMC, or DaNang");
            }
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }
        
        // Create StaffEntity
        StaffEntity staff = new StaffEntity();
        staff.setUid(UUID.randomUUID().toString());
        staff.setEmail(request.getEmail());
        staff.setPasswordHash(passwordEncoder.encode(request.getPassword())); // Hash password using bcrypt
        staff.setFirstName(request.getFirstName());
        staff.setLastName(request.getLastName());
        staff.setPhone(request.getPhone());
        staff.setStaffType(request.getStaffType());
        staff.setAssignedCity(request.getAssignedCity());
        staff.setIsActive(true); // Set isActive = true by default (AC6)
        staff.setEmailVerified(false);
        
        // Save using UserRepository (StaffEntity extends UserEntity)
        // The @DiscriminatorValue("STAFF") annotation will set user_type = "STAFF" automatically
        StaffEntity savedStaff = (StaffEntity) userRepository.save(staff);
        
        log.info("Staff account created successfully with ID: {}, email: {}", savedStaff.getId(), savedStaff.getEmail());
        
        // Map to DTO (excludes passwordHash)
        return staffMapper.toDTO(savedStaff);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaginatedStaffResponse getAllStaff(Pageable pageable, StaffType staffType, String assignedCity) {
        log.debug("Fetching staff list - page: {}, size: {}, staffType: {}, assignedCity: {}", 
                pageable.getPageNumber(), pageable.getPageSize(), staffType, assignedCity);
        
        Page<StaffEntity> staffPage = staffRepository.findAllWithFilters(staffType, assignedCity, pageable);
        
        List<StaffDTO> staffDTOs = staffPage.getContent().stream()
                .map(staffMapper::toDTO)
                .collect(Collectors.toList());
        
        // Convert Spring Data Page (0-based) to PaginationInfo (1-based)
        PaginationInfo pagination = PaginationInfo.builder()
                .currentPage(pageable.getPageNumber() + 1) // Convert 0-based to 1-based
                .pageSize(pageable.getPageSize())
                .totalResults(staffPage.getTotalElements())
                .totalPages(staffPage.getTotalPages())
                .hasNext(staffPage.hasNext())
                .hasPrevious(staffPage.hasPrevious())
                .build();
        
        log.debug("Found {} staff accounts (page {} of {})", 
                staffPage.getTotalElements(), pagination.getCurrentPage(), pagination.getTotalPages());
        
        return new PaginatedStaffResponse(staffDTOs, pagination);
    }
    
    @Override
    @Transactional(readOnly = true)
    public StaffDTO getStaffById(Long id) {
        log.debug("Fetching staff by ID: {}", id);
        
        StaffEntity staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + id));
        
        return staffMapper.toDTO(staff);
    }
    
    @Override
    @Transactional
    public StaffDTO updateStaff(Long id, StaffUpdateRequest request, String updatedBy) {
        log.info("Updating staff account ID: {}, updatedBy: {}", id, updatedBy);
        
        StaffEntity staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + id));
        
        // Track changes for audit logging
        StringBuilder changes = new StringBuilder();
        
        // Validate staffType changes
        if (request.getStaffType() != null) {
            if (request.getStaffType() == StaffType.ADMIN) {
                throw new IllegalArgumentException("Staff type cannot be changed to ADMIN. Admins must be managed separately.");
            }
            
            if (request.getStaffType() == StaffType.STORE_MANAGER) {
                if (request.getAssignedCity() == null || request.getAssignedCity().isBlank()) {
                    throw new IllegalArgumentException("assignedCity is required when staffType is STORE_MANAGER");
                }
                if (!VALID_CITIES.contains(request.getAssignedCity())) {
                    throw new IllegalArgumentException("assignedCity must be one of: Hanoi, HCMC, or DaNang");
                }
            }
            
            if (!staff.getStaffType().equals(request.getStaffType())) {
                changes.append(String.format("staffType: %s -> %s; ", staff.getStaffType(), request.getStaffType()));
            }
        }
        
        // Validate assignedCity if staffType is being set to STORE_MANAGER
        if (request.getStaffType() == null && staff.getStaffType() == StaffType.STORE_MANAGER) {
            if (request.getAssignedCity() != null && !VALID_CITIES.contains(request.getAssignedCity())) {
                throw new IllegalArgumentException("assignedCity must be one of: Hanoi, HCMC, or DaNang");
            }
        }
        
        // Track field changes (using Objects.equals for null-safe comparison)
        if (request.getFirstName() != null && !Objects.equals(staff.getFirstName(), request.getFirstName())) {
            changes.append(String.format("firstName: %s -> %s; ", staff.getFirstName(), request.getFirstName()));
        }
        if (request.getLastName() != null && !Objects.equals(staff.getLastName(), request.getLastName())) {
            changes.append(String.format("lastName: %s -> %s; ", staff.getLastName(), request.getLastName()));
        }
        if (request.getPhone() != null && !Objects.equals(staff.getPhone(), request.getPhone())) {
            changes.append(String.format("phone: %s -> %s; ", staff.getPhone(), request.getPhone()));
        }
        if (request.getAssignedCity() != null && !Objects.equals(staff.getAssignedCity(), request.getAssignedCity())) {
            changes.append(String.format("assignedCity: %s -> %s; ", 
                    staff.getAssignedCity(), request.getAssignedCity()));
        }
        if (request.getIsActive() != null && !Objects.equals(staff.getIsActive(), request.getIsActive())) {
            changes.append(String.format("isActive: %s -> %s; ", staff.getIsActive(), request.getIsActive()));
        }
        
        // Apply updates using mapper (handles null values correctly)
        staffMapper.updateEntityFromRequest(request, staff);
        
        // Save updated entity
        StaffEntity updatedStaff = staffRepository.save(staff);
        
        // Audit logging
        String changeLog = changes.length() > 0 ? changes.toString() : "no changes";
        log.info("Staff account updated: staffId={}, updatedBy={}, changes={}, timestamp={}", 
                id, updatedBy, changeLog, LocalDateTime.now());
        
        return staffMapper.toDTO(updatedStaff);
    }
    
    @Override
    @Transactional
    public StaffDTO deactivateStaff(Long id, String deactivatedBy) {
        log.info("Deactivating staff account ID: {}, deactivatedBy: {}", id, deactivatedBy);
        
        StaffEntity staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + id));
        
        if (!staff.getIsActive()) {
            log.warn("Staff account ID: {} is already deactivated", id);
        }
        
        staff.setIsActive(false);
        StaffEntity deactivatedStaff = staffRepository.save(staff);
        
        // Audit logging
        log.info("Staff account deactivated: staffId={}, deactivatedBy={}, timestamp={}", 
                id, deactivatedBy, LocalDateTime.now());
        
        return staffMapper.toDTO(deactivatedStaff);
    }
}


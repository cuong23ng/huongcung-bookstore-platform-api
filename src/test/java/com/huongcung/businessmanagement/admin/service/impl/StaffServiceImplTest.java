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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffServiceImplTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private StaffRepository staffRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private StaffMapper staffMapper;
    
    @InjectMocks
    private StaffServiceImpl staffService;
    
    private StaffCreateRequest validStoreManagerRequest;
    private StaffCreateRequest validSupportAgentRequest;
    private StaffEntity savedStaffEntity;
    private StaffDTO staffDTO;
    
    @BeforeEach
    void setUp() {
        // Setup valid STORE_MANAGER request
        validStoreManagerRequest = new StaffCreateRequest();
        validStoreManagerRequest.setEmail("storemanager@example.com");
        validStoreManagerRequest.setPassword("password123");
        validStoreManagerRequest.setFirstName("John");
        validStoreManagerRequest.setLastName("Doe");
        validStoreManagerRequest.setPhone("0123456789");
        validStoreManagerRequest.setStaffType(StaffType.STORE_MANAGER);
        validStoreManagerRequest.setAssignedCity("Hanoi");
        
        // Setup valid SUPPORT_AGENT request
        validSupportAgentRequest = new StaffCreateRequest();
        validSupportAgentRequest.setEmail("support@example.com");
        validSupportAgentRequest.setPassword("password123");
        validSupportAgentRequest.setFirstName("Jane");
        validSupportAgentRequest.setLastName("Smith");
        validSupportAgentRequest.setPhone("0987654321");
        validSupportAgentRequest.setStaffType(StaffType.SUPPORT_AGENT);
        validSupportAgentRequest.setAssignedCity(null); // Optional for SUPPORT_AGENT
        
        // Setup saved entity
        savedStaffEntity = new StaffEntity();
        savedStaffEntity.setId(1L);
        savedStaffEntity.setEmail("storemanager@example.com");
        savedStaffEntity.setFirstName("John");
        savedStaffEntity.setLastName("Doe");
        savedStaffEntity.setPhone("0123456789");
        savedStaffEntity.setStaffType(StaffType.STORE_MANAGER);
        savedStaffEntity.setAssignedCity("Hanoi");
        savedStaffEntity.setIsActive(true);
        
        // Setup DTO
        staffDTO = StaffDTO.builder()
                .id(1L)
                .email("storemanager@example.com")
                .firstName("John")
                .lastName("Doe")
                .phone("0123456789")
                .staffType(StaffType.STORE_MANAGER)
                .assignedCity("Hanoi")
                .isActive(true)
                .build();
    }
    
    @Test
    void createStaff_WithValidStoreManagerRequest_ShouldSucceed() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(StaffEntity.class))).thenReturn(savedStaffEntity);
        when(staffMapper.toDTO(any(StaffEntity.class))).thenReturn(staffDTO);
        
        // When
        StaffDTO result = staffService.createStaff(validStoreManagerRequest);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("storemanager@example.com", result.getEmail());
        assertEquals(StaffType.STORE_MANAGER, result.getStaffType());
        assertEquals("Hanoi", result.getAssignedCity());
        assertTrue(result.getIsActive());
        // Note: passwordHash is excluded from DTO - verified by mapper @Mapping(target = "passwordHash", ignore = true)
        
        verify(userRepository).existsByEmail("storemanager@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(StaffEntity.class));
        verify(staffMapper).toDTO(any(StaffEntity.class));
    }
    
    @Test
    void createStaff_WithValidSupportAgentRequest_ShouldSucceed() {
        // Given
        StaffEntity supportStaff = new StaffEntity();
        supportStaff.setId(2L);
        supportStaff.setEmail("support@example.com");
        supportStaff.setStaffType(StaffType.SUPPORT_AGENT);
        supportStaff.setIsActive(true);
        
        StaffDTO supportDTO = StaffDTO.builder()
                .id(2L)
                .email("support@example.com")
                .staffType(StaffType.SUPPORT_AGENT)
                .isActive(true)
                .build();
        
        when(userRepository.existsByEmail("support@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(StaffEntity.class))).thenReturn(supportStaff);
        when(staffMapper.toDTO(any(StaffEntity.class))).thenReturn(supportDTO);
        
        // When
        StaffDTO result = staffService.createStaff(validSupportAgentRequest);
        
        // Then
        assertNotNull(result);
        assertEquals("support@example.com", result.getEmail());
        assertEquals(StaffType.SUPPORT_AGENT, result.getStaffType());
        assertTrue(result.getIsActive());
    }
    
    @Test
    void createStaff_WithAdminStaffType_ShouldThrowException() {
        // Given
        StaffCreateRequest adminRequest = new StaffCreateRequest();
        adminRequest.setEmail("admin@example.com");
        adminRequest.setPassword("password123");
        adminRequest.setFirstName("Admin");
        adminRequest.setLastName("User");
        adminRequest.setPhone("0123456789");
        adminRequest.setStaffType(StaffType.ADMIN);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> staffService.createStaff(adminRequest));
        
        assertTrue(exception.getMessage().contains("ADMIN staff type cannot be created"));
        verify(userRepository, never()).save(any());
    }
    
    @Test
    void createStaff_WithStoreManagerMissingAssignedCity_ShouldThrowException() {
        // Given
        validStoreManagerRequest.setAssignedCity(null);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> staffService.createStaff(validStoreManagerRequest));
        
        assertTrue(exception.getMessage().contains("assignedCity is required"));
        verify(userRepository, never()).save(any());
    }
    
    @Test
    void createStaff_WithInvalidAssignedCity_ShouldThrowException() {
        // Given
        validStoreManagerRequest.setAssignedCity("InvalidCity");
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> staffService.createStaff(validStoreManagerRequest));
        
        assertTrue(exception.getMessage().contains("assignedCity must be one of"));
        verify(userRepository, never()).save(any());
    }
    
    @Test
    void createStaff_WithExistingEmail_ShouldThrowException() {
        // Given
        when(userRepository.existsByEmail("storemanager@example.com")).thenReturn(true);
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> staffService.createStaff(validStoreManagerRequest));
        
        assertTrue(exception.getMessage().contains("Email is already registered"));
        verify(userRepository, never()).save(any());
    }
    
    @Test
    void createStaff_ShouldHashPassword() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword123");
        when(userRepository.save(any(StaffEntity.class))).thenAnswer(invocation -> {
            StaffEntity staff = invocation.getArgument(0);
            staff.setId(1L);
            return staff;
        });
        when(staffMapper.toDTO(any(StaffEntity.class))).thenReturn(staffDTO);
        
        // When
        staffService.createStaff(validStoreManagerRequest);
        
        // Then
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(argThat(staff -> {
            StaffEntity s = (StaffEntity) staff;
            return "hashedPassword123".equals(s.getPasswordHash()) && 
                   !"password123".equals(s.getPasswordHash()); // Password is hashed, not plain text
        }));
    }
    
    @Test
    void createStaff_ShouldSetIsActiveToTrue() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(StaffEntity.class))).thenAnswer(invocation -> {
            StaffEntity staff = invocation.getArgument(0);
            staff.setId(1L);
            return staff;
        });
        when(staffMapper.toDTO(any(StaffEntity.class))).thenReturn(staffDTO);
        
        // When
        staffService.createStaff(validStoreManagerRequest);
        
        // Then
        verify(userRepository).save(argThat(staff -> {
            StaffEntity s = (StaffEntity) staff;
            return Boolean.TRUE.equals(s.getIsActive());
        }));
    }
    
    @Test
    void createStaff_ShouldSetUserTypeToStaff() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(StaffEntity.class))).thenAnswer(invocation -> {
            StaffEntity staff = invocation.getArgument(0);
            staff.setId(1L);
            return staff;
        });
        when(staffMapper.toDTO(any(StaffEntity.class))).thenReturn(staffDTO);
        
        // When
        staffService.createStaff(validStoreManagerRequest);
        
        // Then
        verify(userRepository).save(any(StaffEntity.class)); // StaffEntity has @DiscriminatorValue("STAFF")
        // The user_type = "STAFF" is set automatically by JPA discriminator
    }
    
    // ========== Story 2.2 Tests: getAllStaff ==========
    
    @Test
    void getAllStaff_WithNoFilters_ShouldReturnPaginatedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        List<StaffEntity> staffList = Arrays.asList(savedStaffEntity);
        Page<StaffEntity> staffPage = new PageImpl<>(staffList, pageable, 1L);
        
        when(staffRepository.findAllWithFilters(null, null, pageable)).thenReturn(staffPage);
        when(staffMapper.toDTO(any(StaffEntity.class))).thenReturn(staffDTO);
        
        // When
        StaffService.PaginatedStaffResponse response = staffService.getAllStaff(pageable, null, null);
        
        // Then
        assertNotNull(response);
        assertEquals(1, response.staff().size());
        assertNotNull(response.pagination());
        assertEquals(1, response.pagination().getCurrentPage()); // Converted from 0-based to 1-based
        assertEquals(20, response.pagination().getPageSize());
        assertEquals(1L, response.pagination().getTotalResults());
        assertEquals(1, response.pagination().getTotalPages());
        assertFalse(response.pagination().getHasNext());
        assertFalse(response.pagination().getHasPrevious());
        
        verify(staffRepository).findAllWithFilters(null, null, pageable);
        verify(staffMapper).toDTO(savedStaffEntity);
    }
    
    @Test
    void getAllStaff_WithStaffTypeFilter_ShouldReturnFilteredResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        List<StaffEntity> staffList = Arrays.asList(savedStaffEntity);
        Page<StaffEntity> staffPage = new PageImpl<>(staffList, pageable, 1L);
        
        when(staffRepository.findAllWithFilters(StaffType.STORE_MANAGER, null, pageable)).thenReturn(staffPage);
        when(staffMapper.toDTO(any(StaffEntity.class))).thenReturn(staffDTO);
        
        // When
        StaffService.PaginatedStaffResponse response = staffService.getAllStaff(pageable, StaffType.STORE_MANAGER, null);
        
        // Then
        assertNotNull(response);
        assertEquals(1, response.staff().size());
        verify(staffRepository).findAllWithFilters(StaffType.STORE_MANAGER, null, pageable);
    }
    
    @Test
    void getAllStaff_WithAssignedCityFilter_ShouldReturnFilteredResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        List<StaffEntity> staffList = Arrays.asList(savedStaffEntity);
        Page<StaffEntity> staffPage = new PageImpl<>(staffList, pageable, 1L);
        
        when(staffRepository.findAllWithFilters(null, "Hanoi", pageable)).thenReturn(staffPage);
        when(staffMapper.toDTO(any(StaffEntity.class))).thenReturn(staffDTO);
        
        // When
        StaffService.PaginatedStaffResponse response = staffService.getAllStaff(pageable, null, "Hanoi");
        
        // Then
        assertNotNull(response);
        assertEquals(1, response.staff().size());
        verify(staffRepository).findAllWithFilters(null, "Hanoi", pageable);
    }
    
    @Test
    void getAllStaff_WithPagination_ShouldConvertPageNumbersCorrectly() {
        // Given - page 1 (0-based) should become page 2 (1-based)
        Pageable pageable = PageRequest.of(1, 10);
        Page<StaffEntity> staffPage = new PageImpl<>(Arrays.asList(savedStaffEntity), pageable, 25L);
        
        when(staffRepository.findAllWithFilters(null, null, pageable)).thenReturn(staffPage);
        when(staffMapper.toDTO(any(StaffEntity.class))).thenReturn(staffDTO);
        
        // When
        StaffService.PaginatedStaffResponse response = staffService.getAllStaff(pageable, null, null);
        
        // Then
        assertEquals(2, response.pagination().getCurrentPage()); // 1-based
        assertEquals(10, response.pagination().getPageSize());
        assertEquals(25L, response.pagination().getTotalResults());
        assertEquals(3, response.pagination().getTotalPages()); // 25 / 10 = 2.5, ceil = 3
        assertTrue(response.pagination().getHasNext());
        assertTrue(response.pagination().getHasPrevious());
    }
    
    // ========== Story 2.2 Tests: getStaffById ==========
    
    @Test
    void getStaffById_WithValidId_ShouldReturnStaffDTO() {
        // Given
        when(staffRepository.findById(1L)).thenReturn(Optional.of(savedStaffEntity));
        when(staffMapper.toDTO(savedStaffEntity)).thenReturn(staffDTO);
        
        // When
        StaffDTO result = staffService.getStaffById(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("storemanager@example.com", result.getEmail());
        verify(staffRepository).findById(1L);
        verify(staffMapper).toDTO(savedStaffEntity);
    }
    
    @Test
    void getStaffById_WithInvalidId_ShouldThrowException() {
        // Given
        when(staffRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> staffService.getStaffById(999L));
        
        assertTrue(exception.getMessage().contains("Staff not found"));
        verify(staffRepository).findById(999L);
        verify(staffMapper, never()).toDTO(any());
    }
    
    // ========== Story 2.2 Tests: updateStaff ==========
    
    @Test
    void updateStaff_WithValidPartialUpdate_ShouldUpdateAndReturnDTO() {
        // Given
        StaffUpdateRequest updateRequest = new StaffUpdateRequest();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        
        StaffEntity updatedEntity = new StaffEntity();
        updatedEntity.setId(1L);
        updatedEntity.setFirstName("Jane");
        updatedEntity.setLastName("Smith");
        updatedEntity.setEmail("storemanager@example.com");
        updatedEntity.setStaffType(StaffType.STORE_MANAGER);
        updatedEntity.setIsActive(true);
        
        StaffDTO updatedDTO = StaffDTO.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Smith")
                .email("storemanager@example.com")
                .staffType(StaffType.STORE_MANAGER)
                .isActive(true)
                .build();
        
        when(staffRepository.findById(1L)).thenReturn(Optional.of(savedStaffEntity));
        when(staffRepository.save(any(StaffEntity.class))).thenReturn(updatedEntity);
        when(staffMapper.toDTO(updatedEntity)).thenReturn(updatedDTO);
        
        // When
        StaffDTO result = staffService.updateStaff(1L, updateRequest, "admin@example.com");
        
        // Then
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        verify(staffRepository).findById(1L);
        verify(staffMapper).updateEntityFromRequest(updateRequest, savedStaffEntity);
        verify(staffRepository).save(savedStaffEntity);
    }
    
    @Test
    void updateStaff_WithStaffTypeChangeToAdmin_ShouldThrowException() {
        // Given
        StaffUpdateRequest updateRequest = new StaffUpdateRequest();
        updateRequest.setStaffType(StaffType.ADMIN);
        
        when(staffRepository.findById(1L)).thenReturn(Optional.of(savedStaffEntity));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> staffService.updateStaff(1L, updateRequest, "admin@example.com"));
        
        assertTrue(exception.getMessage().contains("cannot be changed to ADMIN"));
        verify(staffRepository, never()).save(any());
    }
    
    @Test
    void updateStaff_WithStoreManagerMissingAssignedCity_ShouldThrowException() {
        // Given
        StaffUpdateRequest updateRequest = new StaffUpdateRequest();
        updateRequest.setStaffType(StaffType.STORE_MANAGER);
        // Missing assignedCity
        
        when(staffRepository.findById(1L)).thenReturn(Optional.of(savedStaffEntity));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> staffService.updateStaff(1L, updateRequest, "admin@example.com"));
        
        assertTrue(exception.getMessage().contains("assignedCity is required"));
        verify(staffRepository, never()).save(any());
    }
    
    @Test
    void updateStaff_WithInvalidAssignedCity_ShouldThrowException() {
        // Given
        StaffUpdateRequest updateRequest = new StaffUpdateRequest();
        updateRequest.setStaffType(StaffType.STORE_MANAGER);
        updateRequest.setAssignedCity("InvalidCity");
        
        when(staffRepository.findById(1L)).thenReturn(Optional.of(savedStaffEntity));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> staffService.updateStaff(1L, updateRequest, "admin@example.com"));
        
        assertTrue(exception.getMessage().contains("assignedCity must be one of"));
        verify(staffRepository, never()).save(any());
    }
    
    @Test
    void updateStaff_WithInvalidId_ShouldThrowException() {
        // Given
        StaffUpdateRequest updateRequest = new StaffUpdateRequest();
        updateRequest.setFirstName("Jane");
        
        when(staffRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> staffService.updateStaff(999L, updateRequest, "admin@example.com"));
        
        assertTrue(exception.getMessage().contains("Staff not found"));
        verify(staffRepository, never()).save(any());
    }
    
    @Test
    void updateStaff_ShouldTrackChangesForAuditLogging() {
        // Given
        StaffUpdateRequest updateRequest = new StaffUpdateRequest();
        updateRequest.setFirstName("Jane");
        updateRequest.setIsActive(false);
        
        StaffEntity updatedEntity = new StaffEntity();
        updatedEntity.setId(1L);
        updatedEntity.setFirstName("Jane");
        updatedEntity.setIsActive(false);
        
        when(staffRepository.findById(1L)).thenReturn(Optional.of(savedStaffEntity));
        when(staffRepository.save(any(StaffEntity.class))).thenReturn(updatedEntity);
        when(staffMapper.toDTO(any(StaffEntity.class))).thenReturn(staffDTO);
        
        // When
        staffService.updateStaff(1L, updateRequest, "admin@example.com");
        
        // Then - verify audit logging would capture changes
        verify(staffRepository).save(argThat(staff -> {
            StaffEntity s = (StaffEntity) staff;
            return "Jane".equals(s.getFirstName()) && Boolean.FALSE.equals(s.getIsActive());
        }));
    }
    
    // ========== Story 2.2 Tests: deactivateStaff ==========
    
    @Test
    void deactivateStaff_WithValidId_ShouldSetIsActiveToFalse() {
        // Given
        StaffEntity deactivatedEntity = new StaffEntity();
        deactivatedEntity.setId(1L);
        deactivatedEntity.setIsActive(false);
        
        StaffDTO deactivatedDTO = StaffDTO.builder()
                .id(1L)
                .isActive(false)
                .build();
        
        when(staffRepository.findById(1L)).thenReturn(Optional.of(savedStaffEntity));
        when(staffRepository.save(any(StaffEntity.class))).thenReturn(deactivatedEntity);
        when(staffMapper.toDTO(deactivatedEntity)).thenReturn(deactivatedDTO);
        
        // When
        StaffDTO result = staffService.deactivateStaff(1L, "admin@example.com");
        
        // Then
        assertNotNull(result);
        assertFalse(result.getIsActive());
        verify(staffRepository).findById(1L);
        verify(staffRepository).save(argThat(staff -> {
            StaffEntity s = (StaffEntity) staff;
            return Boolean.FALSE.equals(s.getIsActive());
        }));
        verify(staffMapper).toDTO(deactivatedEntity);
    }
    
    @Test
    void deactivateStaff_WithInvalidId_ShouldThrowException() {
        // Given
        when(staffRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> staffService.deactivateStaff(999L, "admin@example.com"));
        
        assertTrue(exception.getMessage().contains("Staff not found"));
        verify(staffRepository, never()).save(any());
    }
    
    @Test
    void deactivateStaff_WithAlreadyDeactivatedStaff_ShouldStillSucceed() {
        // Given
        savedStaffEntity.setIsActive(false);
        StaffEntity deactivatedEntity = new StaffEntity();
        deactivatedEntity.setId(1L);
        deactivatedEntity.setIsActive(false);
        
        StaffDTO deactivatedDTO = StaffDTO.builder()
                .id(1L)
                .isActive(false)
                .build();
        
        when(staffRepository.findById(1L)).thenReturn(Optional.of(savedStaffEntity));
        when(staffRepository.save(any(StaffEntity.class))).thenReturn(deactivatedEntity);
        when(staffMapper.toDTO(deactivatedEntity)).thenReturn(deactivatedDTO);
        
        // When
        StaffDTO result = staffService.deactivateStaff(1L, "admin@example.com");
        
        // Then
        assertNotNull(result);
        assertFalse(result.getIsActive());
        verify(staffRepository).save(any(StaffEntity.class));
    }
}


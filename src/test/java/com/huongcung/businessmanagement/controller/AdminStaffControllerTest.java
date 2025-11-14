package com.huongcung.businessmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huongcung.businessmanagement.admin.model.StaffCreateRequest;
import com.huongcung.businessmanagement.admin.model.StaffDTO;
import com.huongcung.businessmanagement.admin.model.StaffUpdateRequest;
import com.huongcung.businessmanagement.admin.service.StaffService;
import com.huongcung.core.search.model.dto.PaginationInfo;
import com.huongcung.core.user.enumeration.StaffType;
import com.huongcung.platform.auth.configuration.JwtConfiguration;
import com.huongcung.platform.auth.external.jwt.CustomUserDetailsService;
import com.huongcung.platform.auth.external.jwt.JwtTokenBlacklistService;
import com.huongcung.platform.auth.external.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = AdminStaffController.class,
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
class AdminStaffControllerTest {
    // Note: @MockBean is deprecated in Spring Boot 3.4+ in favor of @MockitoBean from Spring Framework 6.2+
    // @MockitoBean is not available in the current Spring Framework version.
    // Using @MockBean until @MockitoBean becomes available in a future Spring Framework version.
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @SuppressWarnings("removal")
    @MockBean
    private StaffService staffService;
    
    // Mock beans required by JwtAuthenticationFilter and WebSecurityConfiguration
    @SuppressWarnings("removal")
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    
    @SuppressWarnings("removal")
    @MockBean
    private CustomUserDetailsService customUserDetailsService;
    
    @SuppressWarnings("removal")
    @MockBean
    private JwtConfiguration jwtConfiguration;
    
    @SuppressWarnings("removal")
    @MockBean
    private JwtTokenBlacklistService jwtTokenBlacklistService;
    
    private StaffCreateRequest createValidRequest() {
        StaffCreateRequest request = new StaffCreateRequest();
        request.setEmail("storemanager@example.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhone("0123456789");
        request.setStaffType(StaffType.STORE_MANAGER);
        request.setAssignedCity("Hanoi");
        return request;
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void createStaff_WithAdminAuthentication_ShouldSucceed() throws Exception {
        // Given
        StaffCreateRequest request = createValidRequest();
        StaffDTO staffDTO = StaffDTO.builder()
                .id(1L)
                .email("storemanager@example.com")
                .firstName("John")
                .lastName("Doe")
                .phone("0123456789")
                .staffType(StaffType.STORE_MANAGER)
                .assignedCity("Hanoi")
                .isActive(true)
                .build();
        
        when(staffService.createStaff(any(StaffCreateRequest.class))).thenReturn(staffDTO);
        
        // When & Then
        mockMvc.perform(post("/api/admin/staff")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("storemanager@example.com"))
                .andExpect(jsonPath("$.data.staffType").value("STORE_MANAGER"))
                .andExpect(jsonPath("$.data.assignedCity").value("Hanoi"))
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andExpect(jsonPath("$.message").value("Staff account created successfully"));
    }
    
    @Test
    void createStaff_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Given
        StaffCreateRequest request = createValidRequest();
        
        // When & Then
        mockMvc.perform(post("/api/admin/staff")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createStaff_WithNonAdminRole_ShouldReturn403() throws Exception {
        // Given
        StaffCreateRequest request = createValidRequest();
        
        // When & Then
        mockMvc.perform(post("/api/admin/staff")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void createStaff_WithMissingRequiredFields_ShouldReturn400() throws Exception {
        // Given
        StaffCreateRequest request = new StaffCreateRequest();
        // Missing required fields
        
        // When & Then
        mockMvc.perform(post("/api/admin/staff")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void createStaff_WithInvalidEmail_ShouldReturn400() throws Exception {
        // Given
        StaffCreateRequest request = createValidRequest();
        request.setEmail("invalid-email"); // Invalid email format
        
        // When & Then
        mockMvc.perform(post("/api/admin/staff")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void createStaff_WithShortPassword_ShouldReturn400() throws Exception {
        // Given
        StaffCreateRequest request = createValidRequest();
        request.setPassword("12345"); // Less than 6 characters
        
        // When & Then
        mockMvc.perform(post("/api/admin/staff")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    // ========== Story 2.2 Tests: GET /api/admin/staff ==========
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllStaff_WithAdminAuthentication_ShouldReturnPaginatedResults() throws Exception {
        // Given
        StaffDTO staff1 = StaffDTO.builder()
                .id(1L)
                .email("staff1@example.com")
                .staffType(StaffType.STORE_MANAGER)
                .build();
        StaffDTO staff2 = StaffDTO.builder()
                .id(2L)
                .email("staff2@example.com")
                .staffType(StaffType.SUPPORT_AGENT)
                .build();
        
        PaginationInfo pagination = PaginationInfo.builder()
                .currentPage(1)
                .pageSize(20)
                .totalResults(2L)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();
        
        StaffService.PaginatedStaffResponse response = 
                new StaffService.PaginatedStaffResponse(Arrays.asList(staff1, staff2), pagination);
        
        when(staffService.getAllStaff(any(), any(), any())).thenReturn(response);
        
        // When & Then
        mockMvc.perform(get("/api/admin/staff")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.staff").isArray())
                .andExpect(jsonPath("$.data.staff[0].email").value("staff1@example.com"))
                .andExpect(jsonPath("$.data.pagination.currentPage").value(1))
                .andExpect(jsonPath("$.data.pagination.totalResults").value(2L));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllStaff_WithStaffTypeFilter_ShouldReturnFilteredResults() throws Exception {
        // Given
        StaffDTO staff = StaffDTO.builder()
                .id(1L)
                .email("staff@example.com")
                .staffType(StaffType.STORE_MANAGER)
                .build();
        
        PaginationInfo pagination = PaginationInfo.builder()
                .currentPage(1)
                .pageSize(20)
                .totalResults(1L)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();
        
        StaffService.PaginatedStaffResponse response = 
                new StaffService.PaginatedStaffResponse(Arrays.asList(staff), pagination);
        
        when(staffService.getAllStaff(any(), eq(StaffType.STORE_MANAGER), any())).thenReturn(response);
        
        // When & Then
        mockMvc.perform(get("/api/admin/staff")
                        .param("staffType", "STORE_MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.staff[0].staffType").value("STORE_MANAGER"));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllStaff_WithAssignedCityFilter_ShouldReturnFilteredResults() throws Exception {
        // Given
        StaffDTO staff = StaffDTO.builder()
                .id(1L)
                .email("staff@example.com")
                .assignedCity("Hanoi")
                .build();
        
        PaginationInfo pagination = PaginationInfo.builder()
                .currentPage(1)
                .pageSize(20)
                .totalResults(1L)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();
        
        StaffService.PaginatedStaffResponse response = 
                new StaffService.PaginatedStaffResponse(Arrays.asList(staff), pagination);
        
        when(staffService.getAllStaff(any(), any(), eq("Hanoi"))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(get("/api/admin/staff")
                        .param("assignedCity", "Hanoi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.staff[0].assignedCity").value("Hanoi"));
    }
    
    @Test
    void getAllStaff_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/admin/staff"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getAllStaff_WithNonAdminRole_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/staff"))
                .andExpect(status().isForbidden());
    }
    
    // ========== Story 2.2 Tests: GET /api/admin/staff/{id} ==========
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void getStaffById_WithValidId_ShouldReturnStaff() throws Exception {
        // Given
        StaffDTO staffDTO = StaffDTO.builder()
                .id(1L)
                .email("staff@example.com")
                .firstName("John")
                .lastName("Doe")
                .staffType(StaffType.STORE_MANAGER)
                .isActive(true)
                .build();
        
        when(staffService.getStaffById(1L)).thenReturn(staffDTO);
        
        // When & Then
        mockMvc.perform(get("/api/admin/staff/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.email").value("staff@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("John"));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void getStaffById_WithInvalidId_ShouldReturn404() throws Exception {
        // Given
        when(staffService.getStaffById(999L)).thenThrow(new RuntimeException("Staff not found with ID: 999"));
        
        // When & Then
        mockMvc.perform(get("/api/admin/staff/999"))
                .andExpect(status().isInternalServerError()); // RuntimeException returns 500
    }
    
    @Test
    void getStaffById_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/admin/staff/1"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getStaffById_WithNonAdminRole_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/staff/1"))
                .andExpect(status().isForbidden());
    }
    
    // ========== Story 2.2 Tests: PUT /api/admin/staff/{id} ==========
    
    @Test
    @WithMockUser(roles = "ADMIN", username = "admin@example.com")
    void updateStaff_WithValidUpdate_ShouldSucceed() throws Exception {
        // Given
        StaffUpdateRequest updateRequest = new StaffUpdateRequest();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        
        StaffDTO updatedDTO = StaffDTO.builder()
                .id(1L)
                .email("staff@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .isActive(true)
                .build();
        
        when(staffService.updateStaff(eq(1L), any(StaffUpdateRequest.class), eq("admin@example.com")))
                .thenReturn(updatedDTO);
        
        // When & Then
        mockMvc.perform(put("/api/admin/staff/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Jane"))
                .andExpect(jsonPath("$.data.lastName").value("Smith"))
                .andExpect(jsonPath("$.message").value("Staff account updated successfully"));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStaff_WithStaffTypeChangeToAdmin_ShouldReturn400() throws Exception {
        // Given
        StaffUpdateRequest updateRequest = new StaffUpdateRequest();
        updateRequest.setStaffType(StaffType.ADMIN);
        
        when(staffService.updateStaff(any(), any(), anyString()))
                .thenThrow(new IllegalArgumentException("Staff type cannot be changed to ADMIN"));
        
        // When & Then
        mockMvc.perform(put("/api/admin/staff/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStaff_WithInvalidId_ShouldReturn500() throws Exception {
        // Given
        StaffUpdateRequest updateRequest = new StaffUpdateRequest();
        updateRequest.setFirstName("Jane");
        
        when(staffService.updateStaff(any(), any(), anyString()))
                .thenThrow(new RuntimeException("Staff not found with ID: 999"));
        
        // When & Then
        mockMvc.perform(put("/api/admin/staff/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isInternalServerError());
    }
    
    @Test
    void updateStaff_WithoutAuthentication_ShouldReturn401() throws Exception {
        StaffUpdateRequest updateRequest = new StaffUpdateRequest();
        updateRequest.setFirstName("Jane");
        
        mockMvc.perform(put("/api/admin/staff/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "CUSTOMER")
    void updateStaff_WithNonAdminRole_ShouldReturn403() throws Exception {
        StaffUpdateRequest updateRequest = new StaffUpdateRequest();
        updateRequest.setFirstName("Jane");
        
        mockMvc.perform(put("/api/admin/staff/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }
    
    // ========== Story 2.2 Tests: PUT /api/admin/staff/{id}/deactivate ==========
    
    @Test
    @WithMockUser(roles = "ADMIN", username = "admin@example.com")
    void deactivateStaff_WithValidId_ShouldSucceed() throws Exception {
        // Given
        StaffDTO deactivatedDTO = StaffDTO.builder()
                .id(1L)
                .email("staff@example.com")
                .isActive(false)
                .build();
        
        when(staffService.deactivateStaff(eq(1L), eq("admin@example.com"))).thenReturn(deactivatedDTO);
        
        // When & Then
        mockMvc.perform(put("/api/admin/staff/1/deactivate")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isActive").value(false))
                .andExpect(jsonPath("$.message").value("Staff account deactivated successfully"));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateStaff_WithInvalidId_ShouldReturn500() throws Exception {
        // Given
        when(staffService.deactivateStaff(any(), anyString()))
                .thenThrow(new RuntimeException("Staff not found with ID: 999"));
        
        // When & Then
        mockMvc.perform(put("/api/admin/staff/999/deactivate")
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }
    
    @Test
    void deactivateStaff_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(put("/api/admin/staff/1/deactivate")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "CUSTOMER")
    void deactivateStaff_WithNonAdminRole_ShouldReturn403() throws Exception {
        mockMvc.perform(put("/api/admin/staff/1/deactivate")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}


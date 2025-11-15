package com.huongcung.core.security.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles;
    private String userType; // CUSTOMER, ADMIN, STORE_MANAGER, SUPPORT_AGENT
}

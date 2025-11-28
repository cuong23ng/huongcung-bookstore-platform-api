package com.huongcung.core.security.service.impl;

import com.huongcung.core.security.model.dto.LoginRequest;
import com.huongcung.core.security.model.dto.LoginResponse;
import com.huongcung.core.security.model.dto.LogoutResponse;
import com.huongcung.core.security.enumeration.UserRole;
import com.huongcung.core.security.model.dto.CustomUserDetails;
import com.huongcung.core.security.external.jwt.JwtTokenProvider;
import com.huongcung.core.user.model.entity.StaffEntity;
import com.huongcung.core.user.repository.StaffRepository;
import com.huongcung.core.security.service.StaffAuthService;
import com.huongcung.core.security.external.jwt.JwtTokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffAuthServiceImpl implements StaffAuthService {
    
    @Qualifier("staffAuthenticationManager")
    private final AuthenticationManager staffAuthenticationManager;
    private final StaffRepository staffRepository;
    private final JwtTokenProvider tokenProvider;
    private final JwtTokenBlacklistService jwtTokenBlacklistService;
    
    /**
     * Authenticate staff and return JWT token
     * @param loginRequest the login credentials
     * @return LoginResponse with JWT token and user info
     */
    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = staffAuthenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = tokenProvider.generateToken(authentication);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Load full staff entity for first and last name
        StaffEntity staff = staffRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        return LoginResponse.builder()
                .token(token)
                .id(staff.getId())
                .email(staff.getEmail())
                .firstName(staff.getFirstName())
                .lastName(staff.getLastName())
                .roles(roles)
                .userType(determineUserType(roles))
                .build();
    }
    
    /**
     * Determine user type from roles
     * @param roles the list of roles
     * @return user type string
     */
    private String determineUserType(List<String> roles) {
        return roles.stream()
                .map(UserRole::fromSpringSecurityRole)
                .filter(role -> role != null)
                .map(UserRole::getCode)
                .findFirst()
                .orElse("UNKNOWN");
    }
    
    /**
     * Logout staff by extracting token from Authorization header
     * @param authHeader the Authorization header value
     * @return LogoutResponse indicating success or failure
     */
    public LogoutResponse logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return LogoutResponse.failure("Invalid authorization header");
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        // Validate token
        if (!tokenProvider.validateToken(token)) {
            return LogoutResponse.failure("Invalid token provided");
        }

        // Blacklist the token
        jwtTokenBlacklistService.blacklistToken(token);

        log.info("Staff logged out successfully");
        return LogoutResponse.success();
    }
}


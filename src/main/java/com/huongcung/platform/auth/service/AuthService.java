package com.huongcung.platform.auth.service;

import com.huongcung.platform.auth.dto.LoginRequest;
import com.huongcung.platform.auth.dto.LoginResponse;
import com.huongcung.platform.auth.dto.LogoutRequest;
import com.huongcung.platform.auth.dto.LogoutResponse;
import com.huongcung.platform.auth.dto.RegisterRequest;
import com.huongcung.platform.auth.enumeration.UserRole;
import com.huongcung.platform.auth.security.dto.CustomUserDetails;
import com.huongcung.platform.auth.security.jwt.JwtTokenProvider;
import com.huongcung.core.user.entity.CustomerEntity;
import com.huongcung.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    
    /**
     * Authenticate user and return JWT token
     * @param loginRequest the login credentials
     * @return LoginResponse with JWT token and user info
     */
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
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

            return LoginResponse.builder()
                    .token(token)
                    .id(userDetails.getId())
                    .email(userDetails.getUsername())
                    .firstName(getFirstName(userDetails))
                    .lastName(getLastName(userDetails))
                    .roles(roles)
                    .userType(determineUserType(roles))
                    .build();

        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", loginRequest.getEmail());
            throw new RuntimeException("Invalid email or password", e);
        }
    }
    
    /**
     * Register a new customer
     * @param registerRequest the registration data
     * @return LoginResponse with JWT token and user info
     */
    @Transactional
    public LoginResponse register(RegisterRequest registerRequest) {
        // Check if user already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        // Create new customer
        CustomerEntity customer = new CustomerEntity();
        customer.setUid(UUID.randomUUID().toString());
        customer.setEmail(registerRequest.getEmail());
        customer.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        customer.setFirstName(registerRequest.getFirstName());
        customer.setLastName(registerRequest.getLastName());
        customer.setPhone(registerRequest.getPhone());
        customer.setGender(registerRequest.getGender());
        customer.setIsActive(true);
        customer.setEmailVerified(false); // TODO: Implement email verification

        CustomerEntity savedCustomer = userRepository.save(customer);

        // Generate token for the new user
        String token = tokenProvider.generateToken(
            savedCustomer.getEmail(),
            List.of(UserRole.CUSTOMER.getSpringSecurityRole())
        );

        return LoginResponse.builder()
                .token(token)
                .id(savedCustomer.getId())
                .email(savedCustomer.getEmail())
                .firstName(savedCustomer.getFirstName())
                .lastName(savedCustomer.getLastName())
                .roles(List.of(UserRole.CUSTOMER.getSpringSecurityRole()))
                .userType(UserRole.CUSTOMER.getCode())
                .build();
    }
    
    /**
     * Get first name from user details (placeholder implementation)
     * @param userDetails the user details
     * @return first name
     */
    private String getFirstName(CustomUserDetails userDetails) {
        // TODO: Load full user entity to get first name
        return "User";
    }
    
    /**
     * Get last name from user details (placeholder implementation)
     * @param userDetails the user details
     * @return last name
     */
    private String getLastName(CustomUserDetails userDetails) {
        // TODO: Load full user entity to get last name
        return "Name";
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
     * Logout user by blacklisting the JWT token
     * @param logoutRequest the logout request containing the token
     * @return LogoutResponse indicating success or failure
     */
    public LogoutResponse logout(LogoutRequest logoutRequest) {
        try {
            String token = logoutRequest.getToken();
            
            // Validate token format
            if (token == null || token.trim().isEmpty()) {
                log.warn("Logout attempt with empty token");
                return LogoutResponse.failure("Invalid token provided");
            }
            
            // Check if token is valid before blacklisting
            if (!tokenProvider.validateToken(token)) {
                log.warn("Logout attempt with invalid token");
                return LogoutResponse.failure("Invalid token provided");
            }
            
            // Blacklist the token
            tokenBlacklistService.blacklistToken(token);
            
            log.info("User logged out successfully");
            return LogoutResponse.success();
            
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            return LogoutResponse.failure("Logout failed: " + e.getMessage());
        }
    }
    
    /**
     * Logout user by extracting token from Authorization header
     * @param authHeader the Authorization header value
     * @return LogoutResponse indicating success or failure
     */
    public LogoutResponse logout(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return LogoutResponse.failure("Invalid authorization header");
            }
            
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            
            // Validate token
            if (!tokenProvider.validateToken(token)) {
                return LogoutResponse.failure("Invalid token provided");
            }
            
            // Blacklist the token
            tokenBlacklistService.blacklistToken(token);
            
            log.info("User logged out successfully");
            return LogoutResponse.success();
            
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            return LogoutResponse.failure("Logout failed: " + e.getMessage());
        }
    }
}

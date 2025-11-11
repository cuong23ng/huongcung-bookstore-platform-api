package com.huongcung.platform.auth.service.impl;

import com.huongcung.platform.auth.dto.LoginRequest;
import com.huongcung.platform.auth.dto.LoginResponse;
import com.huongcung.platform.auth.dto.LogoutResponse;
import com.huongcung.platform.auth.dto.RegisterRequest;
import com.huongcung.platform.auth.enumeration.UserRole;
import com.huongcung.platform.auth.dto.CustomUserDetails;
import com.huongcung.platform.auth.external.jwt.JwtTokenProvider;
import com.huongcung.core.user.model.entity.CustomerEntity;
import com.huongcung.core.user.model.entity.UserEntity;
import com.huongcung.core.user.repository.UserRepository;
import com.huongcung.platform.auth.service.AuthService;
import com.huongcung.platform.auth.external.jwt.JwtTokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
public class AuthServiceImpl implements AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final JwtTokenBlacklistService jwtTokenBlacklistService;
    
    /**
     * Authenticate user and return JWT token
     * @param loginRequest the login credentials
     * @return LoginResponse with JWT token and user info
     */
    public LoginResponse login(LoginRequest loginRequest) {
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

        // Load full user entity for first and last name
        UserEntity user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return LoginResponse.builder()
                .token(token)
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .userType(determineUserType(roles))
                .build();
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
     * Logout user by extracting token from Authorization header
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

        log.info("User logged out successfully");
        return LogoutResponse.success();
    }
}

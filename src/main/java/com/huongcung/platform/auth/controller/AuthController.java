package com.huongcung.platform.auth.controller;

import com.huongcung.platform.auth.dto.LoginRequest;
import com.huongcung.platform.auth.dto.LoginResponse;
import com.huongcung.platform.auth.dto.LogoutRequest;
import com.huongcung.platform.auth.dto.LogoutResponse;
import com.huongcung.platform.auth.dto.RegisterRequest;
import com.huongcung.platform.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Authenticate user and return JWT token
     * @param loginRequest the login credentials
     * @return LoginResponse with JWT token and user info
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());
        
        try {
            LoginResponse response = authService.login(loginRequest);
            log.info("Login successful for user: {}", loginRequest.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed for user: {}", loginRequest.getEmail(), e);
            throw e;
        }
    }
    
    /**
     * Register a new customer
     * @param registerRequest the registration data
     * @return LoginResponse with JWT token and user info
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registration attempt for email: {}", registerRequest.getEmail());
        
        try {
            LoginResponse response = authService.register(registerRequest);
            log.info("Registration successful for user: {}", registerRequest.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Registration failed for user: {}", registerRequest.getEmail(), e);
            throw e;
        }
    }
    
    /**
     * Logout user by token
     * @param logoutRequest the logout request containing the token
     * @return LogoutResponse indicating success or failure
     */
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@Valid @RequestBody LogoutRequest logoutRequest) {
        log.info("Logout attempt with token");
        
        try {
            LogoutResponse response = authService.logout(logoutRequest);
            if (response.isSuccess()) {
                log.info("Logout successful");
            } else {
                log.warn("Logout failed: {}", response.getMessage());
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Logout error", e);
            return ResponseEntity.ok(LogoutResponse.failure("Logout failed: " + e.getMessage()));
        }
    }
    
    /**
     * Logout user using Authorization header
     * @param authHeader the Authorization header
     * @return LogoutResponse indicating success or failure
     */
    @PostMapping("/logout-header")
    public ResponseEntity<LogoutResponse> logoutWithHeader(@RequestHeader("Authorization") String authHeader) {
        log.info("Logout attempt with Authorization header");
        
        try {
            LogoutResponse response = authService.logout(authHeader);
            if (response.isSuccess()) {
                log.info("Logout successful");
            } else {
                log.warn("Logout failed: {}", response.getMessage());
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Logout error", e);
            return ResponseEntity.ok(LogoutResponse.failure("Logout failed: " + e.getMessage()));
        }
    }
    
    /**
     * Health check endpoint for authentication service
     * @return status message
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "Authentication service is running"));
    }
}

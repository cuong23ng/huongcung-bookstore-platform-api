package com.huongcung.platform.controller;

import com.huongcung.platform.auth.dto.LoginRequest;
import com.huongcung.platform.auth.dto.LoginResponse;
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
        LoginResponse response = authService.login(loginRequest);
        log.info("Login successful for user: {}", loginRequest.getEmail());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Register a new customer
     * @param registerRequest the registration data
     * @return LoginResponse with JWT token and user info
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registration attempt for email: {}", registerRequest.getEmail());
        LoginResponse response = authService.register(registerRequest);
        log.info("Registration successful for user: {}", registerRequest.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Logout user using Authorization header
     * @param authHeader the Authorization header
     * @return LogoutResponse indicating success or failure
     */
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@RequestHeader("Authorization") String authHeader) {
        log.info("Logout attempt with Authorization header");
        LogoutResponse response = authService.logout(authHeader);
        log.info("Logout result: {}, {}", response.isSuccess(), response.getMessage());
        return ResponseEntity.ok(response);
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

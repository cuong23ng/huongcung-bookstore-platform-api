package com.huongcung.businessmanagement.controller;

import com.huongcung.core.security.model.dto.LoginRequest;
import com.huongcung.core.security.model.dto.LoginResponse;
import com.huongcung.core.security.model.dto.LogoutResponse;
import com.huongcung.core.security.service.StaffAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/admin/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class StaffAuthController {
    
    private final StaffAuthService staffAuthService;
    
    /**
     * Authenticate staff and return JWT token
     * @param loginRequest the login credentials
     * @return LoginResponse with JWT token and user info
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Staff login attempt for email: {}", loginRequest.getEmail());
        LoginResponse response = staffAuthService.login(loginRequest);
        log.info("Staff login successful for user: {}", loginRequest.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Logout staff using Authorization header
     * @param authHeader the Authorization header
     * @return LogoutResponse indicating success or failure
     */
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@RequestHeader("Authorization") String authHeader) {
        log.info("Staff logout attempt with Authorization header");
        LogoutResponse response = staffAuthService.logout(authHeader);
        log.info("Staff logout result: {}, {}", response.isSuccess(), response.getMessage());
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint for staff authentication service
     * @return status message
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "Staff authentication service is running"));
    }
}


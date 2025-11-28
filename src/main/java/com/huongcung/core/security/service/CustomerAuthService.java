package com.huongcung.core.security.service;

import com.huongcung.core.security.model.dto.LoginRequest;
import com.huongcung.core.security.model.dto.LoginResponse;
import com.huongcung.core.security.model.dto.LogoutResponse;
import com.huongcung.core.security.model.dto.RegisterRequest;

/**
 * Authentication service for customers (webstore module)
 */
public interface CustomerAuthService {
    LoginResponse login(LoginRequest loginRequest);
    LoginResponse register(RegisterRequest registerRequest);
    LogoutResponse logout(String authHeader);
}


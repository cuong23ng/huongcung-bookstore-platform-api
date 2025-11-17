package com.huongcung.core.security.service;

import com.huongcung.core.security.model.dto.LoginRequest;
import com.huongcung.core.security.model.dto.LoginResponse;
import com.huongcung.core.security.model.dto.LogoutResponse;

/**
 * Authentication service for staff (businessmanagement module)
 */
public interface StaffAuthService {
    LoginResponse login(LoginRequest loginRequest);
    LogoutResponse logout(String authHeader);
}


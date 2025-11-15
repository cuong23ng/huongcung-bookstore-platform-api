package com.huongcung.core.security.service;

import com.huongcung.core.security.model.dto.LoginRequest;
import com.huongcung.core.security.model.dto.LoginResponse;
import com.huongcung.core.security.model.dto.LogoutResponse;
import com.huongcung.core.security.model.dto.RegisterRequest;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);

    LoginResponse register(RegisterRequest registerRequest);

    LogoutResponse logout(String authHeader);
}

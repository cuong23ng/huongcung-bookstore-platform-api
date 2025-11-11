package com.huongcung.platform.auth.service;

import com.huongcung.platform.auth.dto.LoginRequest;
import com.huongcung.platform.auth.dto.LoginResponse;
import com.huongcung.platform.auth.dto.LogoutResponse;
import com.huongcung.platform.auth.dto.RegisterRequest;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);

    LoginResponse register(RegisterRequest registerRequest);

    LogoutResponse logout(String authHeader);
}

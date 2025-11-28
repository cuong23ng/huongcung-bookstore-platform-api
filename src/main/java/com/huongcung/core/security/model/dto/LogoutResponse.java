package com.huongcung.core.security.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutResponse {
    
    private String message;
    private boolean success;
    private long timestamp;
    
    public static LogoutResponse success() {
        return LogoutResponse.builder()
                .message("Logout successful")
                .success(true)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    public static LogoutResponse failure(String message) {
        return LogoutResponse.builder()
                .message(message)
                .success(false)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}

package com.huongcung.platform.auth.security.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    
    private String secret = "mySecretKey123456789012345678901234567890";
    private long expiration = 86400000; // 24 hours in milliseconds
    private String tokenPrefix = "Bearer ";
    private String headerName = "Authorization";
}

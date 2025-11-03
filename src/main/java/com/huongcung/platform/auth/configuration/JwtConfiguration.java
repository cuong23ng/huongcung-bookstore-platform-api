package com.huongcung.platform.auth.configuration;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfiguration {
    private String secret;
    private long expiration;
    private String tokenPrefix = "Bearer ";
    private String headerName = "Authorization";
}

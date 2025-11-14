package com.huongcung.platform.checkout.external.ghn.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ghn")
@Getter
@Setter
public class GhnApiConfig {
    // Spring Boot relaxed binding maps api-token to apiToken automatically
    private String apiToken;
    private String baseUrl = "https://dev-online-gateway.ghn.vn";
    private Integer shopId;
    private Integer clientId;
    private Integer timeout = 10000; // milliseconds
    private Integer retryMaxAttempts = 3;
    private Integer retryDelayMs = 1000;
}


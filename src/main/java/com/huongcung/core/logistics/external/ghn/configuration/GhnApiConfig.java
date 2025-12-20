package com.huongcung.core.logistics.external.ghn.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ghn")
@Getter
@Setter
public class GhnApiConfig {
    private String apiToken;
    private String baseUrl;
    private String shopId;
    private Integer clientId;
    private Integer timeout = 10000; // milliseconds
    private Integer retryMaxAttempts = 3;
    private Integer retryDelayMs = 1000;
}


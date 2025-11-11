package com.huongcung.core.search.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Health indicator for Solr connectivity
 * Non-blocking: Application will start even if Solr is unavailable
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SolrHealthIndicator implements HealthIndicator {
    
    private final SolrConfig solrConfig;
    
    @Override
    public Health health() {
        try {
            // Test Solr ping endpoint
            String pingUrl = String.format("%s/admin/ping", solrConfig.getBaseUrl());
            URL url = new URL(pingUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(solrConfig.getConnectionTimeout());
            connection.setReadTimeout(solrConfig.getSocketTimeout());
            connection.setRequestMethod("GET");
            
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            
            if (responseCode == 200) {
                return Health.up()
                    .withDetail("host", solrConfig.getHost())
                    .withDetail("port", solrConfig.getPort())
                    .withDetail("core", solrConfig.getCore())
                    .withDetail("url", solrConfig.getCoreUrl())
                    .build();
            } else {
                return Health.down()
                    .withDetail("host", solrConfig.getHost())
                    .withDetail("port", solrConfig.getPort())
                    .withDetail("core", solrConfig.getCore())
                    .withDetail("error", "Solr ping returned status: " + responseCode)
                    .build();
            }
        } catch (Exception e) {
            log.warn("Solr health check failed: {}", e.getMessage());
            // Return down status but don't throw exception (graceful degradation)
            return Health.down()
                .withDetail("host", solrConfig.getHost())
                .withDetail("port", solrConfig.getPort())
                .withDetail("core", solrConfig.getCore())
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}


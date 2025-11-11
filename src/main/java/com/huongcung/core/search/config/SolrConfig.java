package com.huongcung.core.search.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Solr configuration properties
 * Loads from application.yml under 'solr' prefix
 */
@Configuration
@ConfigurationProperties(prefix = "solr")
@Getter
@Setter
public class SolrConfig {
    
    /**
     * Solr server host (default: localhost)
     */
    private String host = "localhost";
    
    /**
     * Solr server port (default: 8983)
     */
    private int port = 8983;
    
    /**
     * Solr core name (default: books)
     */
    private String core = "books";
    
    /**
     * Connection timeout in milliseconds (default: 5000)
     */
    private int connectionTimeout = 5000;
    
    /**
     * Socket timeout in milliseconds (default: 10000)
     */
    private int socketTimeout = 10000;
    
    /**
     * Get the base URL for Solr
     * @return Base URL (e.g., http://localhost:8983/solr)
     */
    public String getBaseUrl() {
        return String.format("http://%s:%d/solr", host, port);
    }
    
    /**
     * Get the core URL for Solr
     * @return Core URL (e.g., http://localhost:8983/solr/books)
     */
    public String getCoreUrl() {
        return String.format("%s/%s", getBaseUrl(), core);
    }
}


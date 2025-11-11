package com.huongcung.core.search.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for search result caching
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    /**
     * Cache manager for search results
     * Uses in-memory cache (ConcurrentMapCacheManager)
     * For production, consider using Redis or Caffeine cache
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(java.util.Arrays.asList(
            "searchResults",
            "searchFacets",
            "searchSuggestions"
        ));
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }
}


package com.huongcung.core.search.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for book search
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    
    /**
     * Query string for full-text search
     */
    private String q;
    
    /**
     * Genre filters (multi-select)
     */
    private List<String> genres;
    
    /**
     * Language filters (multi-select)
     */
    private List<String> languages;
    
    /**
     * Format filters: PHYSICAL, DIGITAL, BOTH
     */
    private List<String> formats;
    
    /**
     * Minimum price filter
     */
    private Double minPrice;
    
    /**
     * Maximum price filter
     */
    private Double maxPrice;
    
    /**
     * City availability filters: HANOI, HCMC, DANANG
     */
    private List<String> cities;
    
    /**
     * Page number (1-based, default: 1)
     */
    @Builder.Default
    private Integer page = 1;
    
    /**
     * Page size (default: 20)
     */
    @Builder.Default
    private Integer size = 20;
    
    /**
     * Sort option (e.g., "relevance", "price_asc", "price_desc", "date_desc")
     */
    private String sort;
    
    /**
     * Generate cache key string for this request
     * Used by Spring Cache for cache key generation
     */
    @Override
    public String toString() {
        return String.format("q=%s|genres=%s|languages=%s|formats=%s|minPrice=%s|maxPrice=%s|cities=%s|page=%d|size=%d|sort=%s",
            q != null ? q : "",
            genres != null ? String.join(",", genres) : "",
            languages != null ? String.join(",", languages) : "",
            formats != null ? String.join(",", formats) : "",
            minPrice != null ? minPrice : "",
            maxPrice != null ? maxPrice : "",
            cities != null ? String.join(",", cities) : "",
            page != null ? page : 1,
            size != null ? size : 20,
            sort != null ? sort : "");
    }
}


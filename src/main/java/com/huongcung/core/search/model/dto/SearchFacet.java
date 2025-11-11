package com.huongcung.core.search.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Facet information for search results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchFacet {
    
    /**
     * Facet value (e.g., genre name, language, format)
     */
    private String value;
    
    /**
     * Count of results for this facet value
     */
    private Long count;
}


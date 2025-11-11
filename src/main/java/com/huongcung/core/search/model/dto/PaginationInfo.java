package com.huongcung.core.search.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pagination metadata for search results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationInfo {
    
    /**
     * Current page number (1-based)
     */
    private Integer currentPage;
    
    /**
     * Page size
     */
    private Integer pageSize;
    
    /**
     * Total number of results
     */
    private Long totalResults;
    
    /**
     * Total number of pages
     */
    private Integer totalPages;
    
    /**
     * Whether there is a next page
     */
    private Boolean hasNext;
    
    /**
     * Whether there is a previous page
     */
    private Boolean hasPrevious;
}


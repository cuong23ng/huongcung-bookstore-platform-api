package com.huongcung.core.search.service;

import com.huongcung.core.search.model.dto.SearchRequest;
import com.huongcung.core.search.model.dto.SearchResponse;
import com.huongcung.core.search.model.dto.SearchFacet;

import java.util.List;
import java.util.Map;

/**
 * Service interface for book search operations
 */
public interface SearchService {
    
    /**
     * Search books with filters, pagination, and faceting
     * 
     * @param request Search request with query, filters, pagination
     * @return Search response with results, facets, and pagination
     */
    SearchResponse searchBooks(SearchRequest request);
    
    /**
     * Get autocomplete suggestions for search query
     * 
     * @param query Partial query string
     * @return List of suggestion strings
     */
    List<String> getSuggestions(String query);
    
    /**
     * Get facet counts for search request
     * 
     * @param request Search request (filters applied, but results not needed)
     * @return Map of field name to list of facets with counts
     */
    Map<String, List<SearchFacet>> getFacets(SearchRequest request);
}


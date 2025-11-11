package com.huongcung.platform.controller;

import com.huongcung.core.common.model.response.BaseResponse;
import com.huongcung.core.search.model.dto.SearchRequest;
import com.huongcung.core.search.model.dto.SearchResponse;
import com.huongcung.core.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for book search functionality
 */
@RestController
@RequestMapping("api/books")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class SearchController {

    private final SearchService searchService;

    /**
     * Search books with filters, pagination, and faceting
     * 
     * @param q Search query string
     * @param genre Genre filters (can be multiple)
     * @param language Language filters (can be multiple)
     * @param format Format filters: PHYSICAL, DIGITAL, BOTH (can be multiple)
     * @param minPrice Minimum price filter
     * @param maxPrice Maximum price filter
     * @param city City availability filters: HANOI, HCMC, DANANG (can be multiple)
     * @param page Page number (default: 1)
     * @param size Page size (default: 20)
     * @param sort Sort option: relevance, price_asc, price_desc, date_desc, rating_desc
     * @return Search results with books, facets, and pagination
     */
    @GetMapping("/search")
    public ResponseEntity<BaseResponse> searchBooks(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<String> genre,
            @RequestParam(required = false) List<String> language,
            @RequestParam(required = false) List<String> format,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) List<String> city,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false) String sort) {
        
        log.debug("Search request - query: '{}', filters: genre={}, language={}, format={}, price=[{}, {}], city={}, page={}, size={}, sort={}",
                q, genre, language, format, minPrice, maxPrice, city, page, size, sort);
        
        // Build SearchRequest from query parameters
        SearchRequest request = SearchRequest.builder()
                .q(q)
                .genres(genre)
                .languages(language)
                .formats(format)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .cities(city)
                .page(page != null ? page : 1)
                .size(size != null ? size : 20)
                .sort(sort)
                .build();
        
        // Perform search
        SearchResponse response = searchService.searchBooks(request);
        
        log.debug("Search completed - found {} results in {}ms", 
                response.getBooks() != null ? response.getBooks().size() : 0,
                response.getExecutionTimeMs());
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(response)
                .build());
    }

    /**
     * Get autocomplete suggestions for search query
     * 
     * @param q Partial query string (required)
     * @param limit Maximum number of suggestions (default: 10)
     * @return List of suggestion strings
     */
    @GetMapping("/search/suggest")
    public ResponseEntity<BaseResponse> getSuggestions(
            @RequestParam(required = true) String q,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        
        log.debug("Getting suggestions for query: '{}', limit: {}", q, limit);
        
        // Get suggestions from search service
        List<String> suggestions = searchService.getSuggestions(q);
        
        // Limit results if needed
        if (limit != null && suggestions.size() > limit) {
            suggestions = suggestions.subList(0, limit);
        }
        
        // Build response data
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("suggestions", suggestions);
        
        log.debug("Returning {} suggestions", suggestions.size());
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(responseData)
                .build());
    }
}



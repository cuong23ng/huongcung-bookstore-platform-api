package com.huongcung.core.search.service.impl;

import com.huongcung.core.product.model.dto.AbstractBookDTO;
import com.huongcung.core.product.service.AbstractBookService;
import com.huongcung.core.search.model.dto.PaginationInfo;
import com.huongcung.core.search.model.dto.SearchFacet;
import com.huongcung.core.search.model.dto.SearchRequest;
import com.huongcung.core.search.model.dto.SearchResponse;
import com.huongcung.core.search.repository.BookSearchRepository;
import com.huongcung.core.search.service.SearchPerformanceMonitor;
import com.huongcung.core.search.service.SearchService;
import com.huongcung.webstore.bookstore.mapper.BookViewMapper;
import com.huongcung.webstore.bookstore.model.BookData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of SearchService using Solr with fallback to database search
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SolrSearchServiceImpl implements SearchService {
    
    private final BookSearchRepository bookSearchRepository;
    private final AbstractBookService abstractBookService;
    private final BookViewMapper bookViewMapper;
    private final SearchPerformanceMonitor performanceMonitor;
    
    @Override
    @Cacheable(value = "searchResults", key = "#request.toString()", unless = "#result.fallbackUsed == true")
    public SearchResponse searchBooks(SearchRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Searching books with query: '{}', filters: {}", request.getQ(), buildFilterLog(request));
            
            // Build Solr query (pass unescaped query - repository will handle escaping for fuzzy search)
            // Note: We pass the raw query string to allow the repository to build fuzzy queries properly
            String queryString = request.getQ() != null ? request.getQ() : "*:*";
            Map<String, String> filters = buildFilters(request);
            List<String> facetFields = Arrays.asList("genreNames", "language", "format");
            
            int start = (request.getPage() - 1) * request.getSize();
            int rows = request.getSize();
            
            // Parse sort parameter
            String sortField = parseSortField(request.getSort());
            String sortOrder = parseSortOrder(request.getSort());
            
            // Execute Solr search
            QueryResponse solrResponse = bookSearchRepository.searchWithFacets(
                queryString, filters, facetFields, sortField, sortOrder, start, rows);
            
            // Process results
            SearchResponse response = processSolrResponse(solrResponse, request);
            
            long executionTime = System.currentTimeMillis() - startTime;
            response.setExecutionTimeMs(executionTime);
            response.setFallbackUsed(false);
            
            // Record performance metrics
            performanceMonitor.recordSearchTime("books", executionTime);
            
            log.info("Search completed in {}ms. Found {} results", executionTime, response.getPagination().getTotalResults());
            
            return response;
            
        } catch (Exception e) {
            log.warn("Solr search failed, falling back to database search: {}", e.getMessage());
            SearchResponse fallbackResponse = fallbackToDatabaseSearch(request, startTime);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordSearchTime("books", executionTime);
            return fallbackResponse;
        }
    }
    
    @Override
    @Cacheable(value = "searchSuggestions", key = "#query")
    public List<String> getSuggestions(String query) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.debug("Getting suggestions for query: '{}'", query);
            
            var suggesterResponse = bookSearchRepository.getSuggestions(query, 10);
            
            List<String> suggestions = Collections.emptyList();
            if (suggesterResponse != null && suggesterResponse.getSuggestions() != null) {
                suggestions = suggesterResponse.getSuggestions().values().stream()
                    .flatMap(suggestionList -> suggestionList.stream())
                    .map(suggestion -> suggestion.getTerm())
                    .limit(10)
                    .collect(Collectors.toList());
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordSuggestionTime("autocomplete", executionTime);
            
            return suggestions;
            
        } catch (Exception e) {
            log.warn("Solr suggestions failed: {}", e.getMessage());
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordSuggestionTime("autocomplete", executionTime);
            return Collections.emptyList();
        }
    }
    
    @Override
    @Cacheable(value = "searchFacets", key = "#request.toString()")
    public Map<String, List<SearchFacet>> getFacets(SearchRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.debug("Getting facets for request: {}", request);
            
            // Pass unescaped query - repository handles escaping for fuzzy search
            String queryString = request.getQ() != null ? request.getQ() : "*:*";
            Map<String, String> filters = buildFilters(request);
            List<String> facetFields = Arrays.asList("genreNames", "language", "format");
            
            String sortField = parseSortField(request.getSort());
            String sortOrder = parseSortOrder(request.getSort());
            
            QueryResponse solrResponse = bookSearchRepository.searchWithFacets(
                queryString, filters, facetFields, sortField, sortOrder, 0, 0);
            
            Map<String, List<SearchFacet>> facets = extractFacets(solrResponse);
            
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordFacetTime("facets", executionTime);
            
            return facets;
            
        } catch (Exception e) {
            log.warn("Solr facets failed: {}", e.getMessage());
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordFacetTime("facets", executionTime);
            return Collections.emptyMap();
        }
    }
    
    /**
     * Process Solr query response into SearchResponse
     */
    private SearchResponse processSolrResponse(QueryResponse solrResponse, SearchRequest request) {
        SolrDocumentList documents = solrResponse.getResults();
        
        // Extract book IDs from Solr results (with null safety)
        List<String> bookIds = documents.stream()
            .map(doc -> {
                Object idValue = doc.getFieldValue("id");
                return idValue != null ? idValue.toString() : null;
            })
            .filter(id -> id != null)
            .collect(Collectors.toList());
        
        // Fetch full book data from database
        List<BookData> books = fetchBooksByIds(bookIds);
        
        // Extract highlights
        Map<String, String> highlights = extractHighlights(solrResponse, bookIds);
        
        // Extract facets
        Map<String, List<SearchFacet>> facets = extractFacets(solrResponse);
        
        // Build pagination info
        PaginationInfo pagination = PaginationInfo.builder()
            .currentPage(request.getPage())
            .pageSize(request.getSize())
            .totalResults(documents.getNumFound())
            .totalPages((int) Math.ceil((double) documents.getNumFound() / request.getSize()))
            .hasNext((request.getPage() * request.getSize()) < documents.getNumFound())
            .hasPrevious(request.getPage() > 1)
            .build();
        
        return SearchResponse.builder()
            .books(books)
            .facets(facets)
            .pagination(pagination)
            .highlightedFields(highlights)
            .build();
    }
    
    /**
     * Fetch books from database by IDs
     */
    private List<BookData> fetchBooksByIds(List<String> bookIds) {
        if (bookIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Convert string IDs to Long IDs
        List<Long> longIds = bookIds.stream()
            .map(id -> {
                try {
                    return Long.parseLong(id);
                } catch (NumberFormatException e) {
                    log.warn("Invalid book ID format: {}", id);
                    return null;
                }
            })
            .filter(id -> id != null)
            .collect(Collectors.toList());
        
        if (longIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Fetch books by IDs (efficient query)
        List<AbstractBookDTO> books = abstractBookService.findByIds(longIds);
        
        // Maintain Solr result order
        Map<Long, AbstractBookDTO> bookMap = books.stream()
            .collect(Collectors.toMap(AbstractBookDTO::getId, book -> book));
        
        return longIds.stream()
            .map(bookMap::get)
            .filter(book -> book != null)
            .map(bookViewMapper::toBookData)
            .collect(Collectors.toList());
    }
    
    /**
     * Extract highlights from Solr response
     * Checks both standard fields (title, description) and Vietnamese fields (titleText, descriptionText)
     */
    private Map<String, String> extractHighlights(QueryResponse solrResponse, List<String> bookIds) {
        Map<String, String> highlights = new HashMap<>();
        
        if (solrResponse.getHighlighting() != null) {
            for (String bookId : bookIds) {
                Map<String, List<String>> docHighlights = solrResponse.getHighlighting().get(bookId);
                if (docHighlights != null) {
                    // Prefer title highlight, then titleText (Vietnamese), then description, then descriptionText
                    String highlight = docHighlights.getOrDefault("title", 
                        docHighlights.getOrDefault("titleText",
                        docHighlights.getOrDefault("description", 
                        docHighlights.getOrDefault("descriptionText", Collections.emptyList()))))
                        .stream()
                        .findFirst()
                        .orElse(null);
                    if (highlight != null) {
                        highlights.put(bookId, highlight);
                    }
                }
            }
        }
        
        return highlights;
    }
    
    /**
     * Extract facets from Solr response
     */
    private Map<String, List<SearchFacet>> extractFacets(QueryResponse solrResponse) {
        Map<String, List<SearchFacet>> facets = new HashMap<>();
        
        if (solrResponse.getFacetFields() != null) {
            solrResponse.getFacetFields().forEach(facetField -> {
                List<SearchFacet> facetList = facetField.getValues().stream()
                    .map(count -> SearchFacet.builder()
                        .value(count.getName())
                        .count(count.getCount())
                        .build())
                    .collect(Collectors.toList());
                facets.put(facetField.getName(), facetList);
            });
        }
        
        return facets;
    }
    
    /**
     * Build filter map from SearchRequest
     */
    private Map<String, String> buildFilters(SearchRequest request) {
        Map<String, String> filters = new HashMap<>();
        
        // Genre filters
        if (request.getGenres() != null && !request.getGenres().isEmpty()) {
            String genreFilter = request.getGenres().stream()
                .map(genre -> "\"" + genre + "\"")
                .collect(Collectors.joining(" OR "));
            filters.put("genreNames", "(" + genreFilter + ")");
        }
        
        // Language filters
        if (request.getLanguages() != null && !request.getLanguages().isEmpty()) {
            String languageFilter = request.getLanguages().stream()
                .map(lang -> "\"" + lang + "\"")
                .collect(Collectors.joining(" OR "));
            filters.put("language", "(" + languageFilter + ")");
        }
        
        // Format filters
        if (request.getFormats() != null && !request.getFormats().isEmpty()) {
            String formatFilter = request.getFormats().stream()
                .map(format -> "\"" + format + "\"")
                .collect(Collectors.joining(" OR "));
            filters.put("format", "(" + formatFilter + ")");
        }
        
        // Price filters
        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            double minPrice = request.getMinPrice() != null ? request.getMinPrice() : 0.0;
            double maxPrice = request.getMaxPrice() != null ? request.getMaxPrice() : Double.MAX_VALUE;
            filters.put("physicalPrice", "[" + minPrice + " TO " + maxPrice + "]");
        }
        
        // City availability filters
        if (request.getCities() != null && !request.getCities().isEmpty()) {
            for (String city : request.getCities()) {
                String cityField = mapCityToField(city);
                if (cityField != null) {
                    filters.put(cityField, "true");
                }
            }
        }
        
        return filters;
    }
    
    /**
     * Map city name to Solr field name
     */
    private String mapCityToField(String city) {
        if (city == null || city.trim().isEmpty()) {
            return null;
        }
        String normalized = city.trim().toUpperCase();
        return switch (normalized) {
            case "HANOI", "HÀ NỘI" -> "availableInHanoi";
            case "HCMC", "HOCHIMINH", "HỒ CHÍ MINH", "HO CHI MINH" -> "availableInHcmc";
            case "DANANG", "ĐÀ NẴNG", "DA NANG" -> "availableInDanang";
            default -> {
                log.warn("Unknown city name: {}, skipping filter", city);
                yield null;
            }
        };
    }
    
    /**
     * Escape Solr query string to prevent injection
     */
    private String escapeSolrQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "*:*";
        }
        // Escape special Solr characters: + - && || ! ( ) { } [ ] ^ " ~ * ? : \
        return query.replace("\\", "\\\\")
            .replace("+", "\\+")
            .replace("-", "\\-")
            .replace("&&", "\\&&")
            .replace("||", "\\||")
            .replace("!", "\\!")
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace("{", "\\{")
            .replace("}", "\\}")
            .replace("[", "\\[")
            .replace("]", "\\]")
            .replace("^", "\\^")
            .replace("\"", "\\\"")
            .replace("~", "\\~")
            .replace("*", "\\*")
            .replace("?", "\\?")
            .replace(":", "\\:");
    }
    
    /**
     * Parse sort field from sort parameter
     * Format: "field_order" (e.g., "price_asc", "date_desc", "relevance")
     */
    private String parseSortField(String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            return null; // Use default relevance
        }
        String[] parts = sort.split("_");
        String field = parts[0].toLowerCase();
        return switch (field) {
            case "price" -> "physicalPrice";
            case "date", "publicationdate" -> "publicationDate";
            case "relevance", "score" -> "score";
            case "rating" -> "averageRating";
            case "title" -> "title";
            default -> {
                log.warn("Unknown sort field: {}, using relevance", field);
                yield null;
            }
        };
    }
    
    /**
     * Parse sort order from sort parameter
     */
    private String parseSortOrder(String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            return "desc"; // Default to desc for relevance
        }
        String[] parts = sort.split("_");
        if (parts.length > 1) {
            String order = parts[1].toLowerCase();
            return "desc".equals(order) ? "desc" : "asc";
        }
        return "desc"; // Default
    }
    
    /**
     * Build filter log string for logging
     */
    private String buildFilterLog(SearchRequest request) {
        List<String> filterParts = new ArrayList<>();
        if (request.getGenres() != null && !request.getGenres().isEmpty()) {
            filterParts.add("genres=" + request.getGenres());
        }
        if (request.getLanguages() != null && !request.getLanguages().isEmpty()) {
            filterParts.add("languages=" + request.getLanguages());
        }
        if (request.getFormats() != null && !request.getFormats().isEmpty()) {
            filterParts.add("formats=" + request.getFormats());
        }
        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            filterParts.add("price=[" + request.getMinPrice() + "-" + request.getMaxPrice() + "]");
        }
        if (request.getCities() != null && !request.getCities().isEmpty()) {
            filterParts.add("cities=" + request.getCities());
        }
        return filterParts.isEmpty() ? "none" : String.join(", ", filterParts);
    }
    
    /**
     * Fallback to database search when Solr is unavailable
     */
    private SearchResponse fallbackToDatabaseSearch(SearchRequest request, long startTime) {
        log.info("Using database fallback search");
        
        try {
            // Get all books from database
            List<AbstractBookDTO> allBooks = abstractBookService.findAll();
            
            // Apply basic filtering in memory
            List<AbstractBookDTO> filteredBooks = allBooks.stream()
                .filter(book -> matchesQuery(book, request.getQ()))
                .filter(book -> matchesGenres(book, request.getGenres()))
                .filter(book -> matchesLanguage(book, request.getLanguages()))
                .filter(book -> matchesFormat(book, request.getFormats()))
                .collect(Collectors.toList());
            
            // Apply pagination
            int start = (request.getPage() - 1) * request.getSize();
            int end = Math.min(start + request.getSize(), filteredBooks.size());
            List<AbstractBookDTO> pagedBooks = filteredBooks.subList(
                Math.max(0, start), 
                Math.min(end, filteredBooks.size()));
            
            // Convert to BookData
            List<BookData> books = pagedBooks.stream()
                .map(bookViewMapper::toBookData)
                .collect(Collectors.toList());
            
            // Build pagination
            PaginationInfo pagination = PaginationInfo.builder()
                .currentPage(request.getPage())
                .pageSize(request.getSize())
                .totalResults((long) filteredBooks.size())
                .totalPages((int) Math.ceil((double) filteredBooks.size() / request.getSize()))
                .hasNext(end < filteredBooks.size())
                .hasPrevious(request.getPage() > 1)
                .build();
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            return SearchResponse.builder()
                .books(books)
                .facets(Collections.emptyMap())
                .pagination(pagination)
                .highlightedFields(Collections.emptyMap())
                .executionTimeMs(executionTime)
                .fallbackUsed(true)
                .build();
                
        } catch (Exception e) {
            log.error("Database fallback search also failed: {}", e.getMessage());
            throw new RuntimeException("Search failed", e);
        }
    }
    
    /**
     * Check if book matches query string (basic text matching)
     */
    private boolean matchesQuery(AbstractBookDTO book, String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        String lowerQuery = query.toLowerCase();
        return (book.getTitle() != null && book.getTitle().toLowerCase().contains(lowerQuery)) ||
               (book.getDescription() != null && book.getDescription().toLowerCase().contains(lowerQuery));
    }
    
    /**
     * Check if book matches genre filters
     * Note: AbstractBookDTO doesn't include genres, so this is a limitation of fallback mode.
     * In production, consider adding genres to DTO or using a separate genre lookup.
     */
    private boolean matchesGenres(AbstractBookDTO book, List<String> genres) {
        if (genres == null || genres.isEmpty()) {
            return true;
        }
        // Limitation: AbstractBookDTO doesn't have genres field
        // In fallback mode, genre filtering is not available
        // This is acceptable as fallback is a degraded mode
        log.debug("Genre filtering not available in fallback mode for book: {}", book.getCode());
        return true;
    }
    
    /**
     * Check if book matches language filters
     */
    private boolean matchesLanguage(AbstractBookDTO book, List<String> languages) {
        if (languages == null || languages.isEmpty()) {
            return true;
        }
        return book.getLanguage() != null && 
               languages.contains(book.getLanguage().toString());
    }
    
    /**
     * Check if book matches format filters
     */
    private boolean matchesFormat(AbstractBookDTO book, List<String> formats) {
        if (formats == null || formats.isEmpty()) {
            return true;
        }
        for (String format : formats) {
            if ("PHYSICAL".equals(format) && book.isHasPhysicalEdition()) {
                return true;
            }
            if ("DIGITAL".equals(format) && book.isHasElectricEdition()) {
                return true;
            }
            if ("BOTH".equals(format) && book.isHasPhysicalEdition() && book.isHasElectricEdition()) {
                return true;
            }
        }
        return false;
    }
}


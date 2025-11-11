package com.huongcung.platform.bookstore.controller;

import com.huongcung.core.search.model.dto.PaginationInfo;
import com.huongcung.core.search.model.dto.SearchFacet;
import com.huongcung.core.search.model.dto.SearchRequest;
import com.huongcung.core.search.model.dto.SearchResponse;
import com.huongcung.core.search.service.SearchService;
import com.huongcung.platform.bookstore.model.BookData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huongcung.platform.controller.SearchController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for SearchController using @WebMvcTest
 */
@WebMvcTest(SearchController.class)
@DisplayName("SearchController Unit Tests")
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchService searchService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should perform search with query parameter")
    void testSearchWithQuery() throws Exception {
        // Given
        SearchResponse searchResponse = SearchResponse.builder()
                .books(createMockBooks())
                .facets(createMockFacets())
                .pagination(createMockPagination())
                .highlightedFields(Collections.emptyMap())
                .executionTimeMs(45L)
                .fallbackUsed(false)
                .build();

        when(searchService.searchBooks(any(SearchRequest.class))).thenReturn(searchResponse);

        // When & Then
        mockMvc.perform(get("/api/books/search")
                        .param("q", "truyện kiều")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.books").isArray())
                .andExpect(jsonPath("$.data.books[0].code").value("BK001"))
                .andExpect(jsonPath("$.data.pagination.totalResults").value(150L));
    }

    @Test
    @DisplayName("Should perform search with filters")
    void testSearchWithFilters() throws Exception {
        // Given
        SearchResponse searchResponse = SearchResponse.builder()
                .books(createMockBooks())
                .facets(createMockFacets())
                .pagination(createMockPagination())
                .highlightedFields(Collections.emptyMap())
                .executionTimeMs(30L)
                .fallbackUsed(false)
                .build();

        when(searchService.searchBooks(any(SearchRequest.class))).thenReturn(searchResponse);

        // When & Then
        mockMvc.perform(get("/api/books/search")
                        .param("q", "truyện")
                        .param("genre", "Văn học")
                        .param("language", "Vietnamese")
                        .param("minPrice", "100000")
                        .param("maxPrice", "200000")
                        .param("city", "HANOI")
                        .param("page", "1")
                        .param("size", "20")
                        .param("sort", "relevance")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.books").isArray());
    }

    @Test
    @DisplayName("Should perform search with multiple filter values")
    void testSearchWithMultipleFilters() throws Exception {
        // Given
        SearchResponse searchResponse = SearchResponse.builder()
                .books(createMockBooks())
                .facets(createMockFacets())
                .pagination(createMockPagination())
                .highlightedFields(Collections.emptyMap())
                .executionTimeMs(35L)
                .fallbackUsed(false)
                .build();

        when(searchService.searchBooks(any(SearchRequest.class))).thenReturn(searchResponse);

        // When & Then
        mockMvc.perform(get("/api/books/search")
                        .param("genre", "Văn học", "Khoa học")
                        .param("city", "HANOI", "HCMC")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should return empty results when no matches found")
    void testSearchWithNoResults() throws Exception {
        // Given
        SearchResponse searchResponse = SearchResponse.builder()
                .books(Collections.emptyList())
                .facets(Collections.emptyMap())
                .pagination(PaginationInfo.builder()
                        .currentPage(1)
                        .pageSize(20)
                        .totalResults(0L)
                        .totalPages(0)
                        .hasNext(false)
                        .hasPrevious(false)
                        .build())
                .highlightedFields(Collections.emptyMap())
                .executionTimeMs(20L)
                .fallbackUsed(false)
                .build();

        when(searchService.searchBooks(any(SearchRequest.class))).thenReturn(searchResponse);

        // When & Then
        mockMvc.perform(get("/api/books/search")
                        .param("q", "nonexistent book")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.books").isArray())
                .andExpect(jsonPath("$.data.books").isEmpty())
                .andExpect(jsonPath("$.data.pagination.totalResults").value(0L));
    }

    @Test
    @DisplayName("Should get suggestions successfully")
    void testGetSuggestions() throws Exception {
        // Given
        List<String> suggestions = Arrays.asList(
                "truyện kiều",
                "truyện cổ tích",
                "truyện ngắn"
        );

        when(searchService.getSuggestions("truyện")).thenReturn(suggestions);

        // When & Then
        mockMvc.perform(get("/api/books/search/suggest")
                        .param("q", "truyện")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.suggestions").isArray())
                .andExpect(jsonPath("$.data.suggestions[0]").value("truyện kiều"))
                .andExpect(jsonPath("$.data.suggestions").isArray());
    }

    @Test
    @DisplayName("Should limit suggestions to specified limit")
    void testGetSuggestionsWithLimit() throws Exception {
        // Given
        List<String> allSuggestions = Arrays.asList(
                "truyện kiều", "truyện cổ tích", "truyện ngắn",
                "truyện tranh", "truyện dài", "truyện ngắn hay"
        );

        when(searchService.getSuggestions("truyện")).thenReturn(allSuggestions);

        // When & Then
        mockMvc.perform(get("/api/books/search/suggest")
                        .param("q", "truyện")
                        .param("limit", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.suggestions").isArray())
                .andExpect(jsonPath("$.data.suggestions.length()").value(3));
    }

    @Test
    @DisplayName("Should return empty suggestions when no matches")
    void testGetSuggestionsWithNoResults() throws Exception {
        // Given
        when(searchService.getSuggestions("nonexistent")).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/books/search/suggest")
                        .param("q", "nonexistent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.suggestions").isArray())
                .andExpect(jsonPath("$.data.suggestions").isEmpty());
    }

    @Test
    @DisplayName("Should use default pagination values")
    void testSearchWithDefaultPagination() throws Exception {
        // Given
        SearchResponse searchResponse = SearchResponse.builder()
                .books(createMockBooks())
                .facets(createMockFacets())
                .pagination(PaginationInfo.builder()
                        .currentPage(1)
                        .pageSize(20)
                        .totalResults(150L)
                        .totalPages(8)
                        .hasNext(true)
                        .hasPrevious(false)
                        .build())
                .highlightedFields(Collections.emptyMap())
                .executionTimeMs(40L)
                .fallbackUsed(false)
                .build();

        when(searchService.searchBooks(any(SearchRequest.class))).thenReturn(searchResponse);

        // When & Then - no page/size parameters
        mockMvc.perform(get("/api/books/search")
                        .param("q", "test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pagination.currentPage").value(1))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(20));
    }

    // Helper methods
    private List<BookData> createMockBooks() {
        BookData book = new BookData();
        book.setCode("BK001");
        book.setTitle("Truyện Kiều");
        book.setLanguage("Vietnamese");
        book.setHasPhysicalEdition(true);
        book.setHasElectricEdition(true);
        return Arrays.asList(book);
    }

    private Map<String, List<SearchFacet>> createMockFacets() {
        Map<String, List<SearchFacet>> facets = new HashMap<>();
        facets.put("genreNames", Arrays.asList(
                SearchFacet.builder().value("Văn học").count(45L).build(),
                SearchFacet.builder().value("Khoa học").count(23L).build()
        ));
        facets.put("language", Arrays.asList(
                SearchFacet.builder().value("Vietnamese").count(120L).build(),
                SearchFacet.builder().value("English").count(35L).build()
        ));
        return facets;
    }

    private PaginationInfo createMockPagination() {
        return PaginationInfo.builder()
                .currentPage(1)
                .pageSize(20)
                .totalResults(150L)
                .totalPages(8)
                .hasNext(true)
                .hasPrevious(false)
                .build();
    }
}



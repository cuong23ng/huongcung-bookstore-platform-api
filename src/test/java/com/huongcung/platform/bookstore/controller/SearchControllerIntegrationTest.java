package com.huongcung.platform.bookstore.controller;

import com.huongcung.core.search.service.SearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SearchController using @SpringBootTest
 * Tests the full Spring context and integration with other components
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("SearchController Integration Tests")
class SearchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchService searchService;

    @Test
    @DisplayName("Should return 200 OK for search endpoint")
    void testSearchEndpointExists() throws Exception {
        // This test verifies the endpoint is registered and accessible
        // The actual search logic is mocked via @MockBean
        mockMvc.perform(get("/api/books/search")
                        .param("q", "test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("Should return 200 OK for suggest endpoint")
    void testSuggestEndpointExists() throws Exception {
        // This test verifies the endpoint is registered and accessible
        mockMvc.perform(get("/api/books/search/suggest")
                        .param("q", "test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("Should handle CORS headers correctly")
    void testCorsHeaders() throws Exception {
        mockMvc.perform(get("/api/books/search")
                        .param("q", "test")
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("Should return BaseResponse format")
    void testResponseFormat() throws Exception {
        mockMvc.perform(get("/api/books/search")
                        .param("q", "test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists());
    }
}



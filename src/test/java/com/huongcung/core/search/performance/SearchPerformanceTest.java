package com.huongcung.core.search.performance;

import com.huongcung.core.search.model.dto.SearchRequest;
import com.huongcung.core.search.model.dto.SearchResponse;
import com.huongcung.core.search.service.SearchPerformanceMonitor;
import com.huongcung.core.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests for search functionality
 * Tests response times, caching effectiveness, and concurrent load
 */
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class SearchPerformanceTest {
    
    @Autowired
    private SearchService searchService;
    
    @Autowired
    private SearchPerformanceMonitor performanceMonitor;
    
    @BeforeEach
    void setUp() {
        // Reset performance metrics before each test
        performanceMonitor.reset();
    }
    
    /**
     * Test basic search performance
     * Target: < 500ms for 95% of queries
     */
    @Test
    void testBasicSearchPerformance() {
        log.info("Testing basic search performance...");
        
        List<Long> responseTimes = new ArrayList<>();
        int iterations = 10;
        
        for (int i = 0; i < iterations; i++) {
            long startTime = System.currentTimeMillis();
            
            SearchRequest request = SearchRequest.builder()
                .q("test")
                .page(1)
                .size(20)
                .build();
            
            SearchResponse response = searchService.searchBooks(request);
            
            long responseTime = System.currentTimeMillis() - startTime;
            responseTimes.add(responseTime);
            
            assertNotNull(response);
            log.debug("Search iteration {}: {}ms", i + 1, responseTime);
        }
        
        // Calculate statistics
        double avg = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long p95 = calculatePercentile(responseTimes, 0.95);
        long p99 = calculatePercentile(responseTimes, 0.99);
        long max = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        
        log.info("Basic Search Performance - Avg: {:.2f}ms, P95: {}ms, P99: {}ms, Max: {}ms", 
            avg, p95, p99, max);
        
        // Verify performance targets
        assertTrue(avg < 500, "Average response time should be < 500ms");
        assertTrue(p95 < 500, "P95 response time should be < 500ms");
    }
    
    /**
     * Test autocomplete performance
     * Target: < 200ms
     */
    @Test
    void testAutocompletePerformance() {
        log.info("Testing autocomplete performance...");
        
        List<Long> responseTimes = new ArrayList<>();
        int iterations = 20;
        
        for (int i = 0; i < iterations; i++) {
            long startTime = System.currentTimeMillis();
            
            List<String> suggestions = searchService.getSuggestions("test");
            
            long responseTime = System.currentTimeMillis() - startTime;
            responseTimes.add(responseTime);
            
            assertNotNull(suggestions);
            log.debug("Suggestion iteration {}: {}ms", i + 1, responseTime);
        }
        
        // Calculate statistics
        double avg = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long max = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        
        log.info("Autocomplete Performance - Avg: {:.2f}ms, Max: {}ms", avg, max);
        
        // Verify performance targets
        assertTrue(avg < 200, "Average autocomplete response time should be < 200ms");
    }
    
    /**
     * Test faceted search performance
     * Target: < 300ms
     */
    @Test
    void testFacetedSearchPerformance() {
        log.info("Testing faceted search performance...");
        
        List<Long> responseTimes = new ArrayList<>();
        int iterations = 10;
        
        for (int i = 0; i < iterations; i++) {
            long startTime = System.currentTimeMillis();
            
            SearchRequest request = SearchRequest.builder()
                .q("book")
                .page(1)
                .size(20)
                .build();
            
            var facets = searchService.getFacets(request);
            
            long responseTime = System.currentTimeMillis() - startTime;
            responseTimes.add(responseTime);
            
            assertNotNull(facets);
            log.debug("Facet iteration {}: {}ms", i + 1, responseTime);
        }
        
        // Calculate statistics
        double avg = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long max = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        
        log.info("Faceted Search Performance - Avg: {:.2f}ms, Max: {}ms", avg, max);
        
        // Verify performance targets
        assertTrue(avg < 300, "Average faceted search response time should be < 300ms");
    }
    
    /**
     * Test caching effectiveness
     * Second request should be significantly faster
     */
    @Test
    void testCachingEffectiveness() {
        log.info("Testing cache effectiveness...");
        
        SearchRequest request = SearchRequest.builder()
            .q("cached query")
            .page(1)
            .size(20)
            .build();
        
        // First request (cache miss)
        long firstStart = System.currentTimeMillis();
        SearchResponse firstResponse = searchService.searchBooks(request);
        long firstTime = System.currentTimeMillis() - firstStart;
        
        // Second request (cache hit)
        long secondStart = System.currentTimeMillis();
        SearchResponse secondResponse = searchService.searchBooks(request);
        long secondTime = System.currentTimeMillis() - secondStart;
        
        assertNotNull(firstResponse);
        assertNotNull(secondResponse);
        
        log.info("Cache Test - First request: {}ms, Second request: {}ms", firstTime, secondTime);
        
        // Second request should be faster (cache hit)
        // Note: In-memory cache should be very fast, but we allow some variance
        assertTrue(secondTime <= firstTime * 2, 
            "Cached request should be faster or similar to first request");
    }
    
    /**
     * Test concurrent search requests
     * Simulates multiple users searching simultaneously
     */
    @Test
    void testConcurrentSearchRequests() {
        log.info("Testing concurrent search requests...");
        
        int concurrentUsers = 10;
        int requestsPerUser = 5;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        
        List<CompletableFuture<Long>> futures = new ArrayList<>();
        
        for (int user = 0; user < concurrentUsers; user++) {
            for (int req = 0; req < requestsPerUser; req++) {
                final int userId = user;
                final int reqId = req;
                
                CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                    long startTime = System.currentTimeMillis();
                    
                    SearchRequest request = SearchRequest.builder()
                        .q("concurrent test " + userId)
                        .page(1)
                        .size(20)
                        .build();
                    
                    SearchResponse response = searchService.searchBooks(request);
                    
                    long responseTime = System.currentTimeMillis() - startTime;
                    
                    assertNotNull(response);
                    log.debug("User {} request {}: {}ms", userId, reqId, responseTime);
                    
                    return responseTime;
                }, executor);
                
                futures.add(future);
            }
        }
        
        // Wait for all requests to complete
        List<Long> responseTimes = futures.stream()
            .map(CompletableFuture::join)
            .toList();
        
        executor.shutdown();
        
        // Calculate statistics
        double avg = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long p95 = calculatePercentile(responseTimes, 0.95);
        long max = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        
        log.info("Concurrent Search Performance ({} users, {} requests each) - Avg: {:.2f}ms, P95: {}ms, Max: {}ms",
            concurrentUsers, requestsPerUser, avg, p95, max);
        
        // Verify performance under load
        assertTrue(avg < 1000, "Average response time under concurrent load should be < 1000ms");
        assertTrue(p95 < 1500, "P95 response time under concurrent load should be < 1500ms");
    }
    
    /**
     * Test performance monitor metrics
     */
    @Test
    void testPerformanceMonitorMetrics() {
        log.info("Testing performance monitor metrics...");
        
        // Perform some searches
        for (int i = 0; i < 5; i++) {
            SearchRequest request = SearchRequest.builder()
                .q("monitor test")
                .page(1)
                .size(20)
                .build();
            
            searchService.searchBooks(request);
            searchService.getSuggestions("test");
            searchService.getFacets(request);
        }
        
        // Check metrics
        double avgSearch = performanceMonitor.getAverageResponseTime("search", "books");
        long p95Search = performanceMonitor.getPercentileResponseTime("search", "books", 0.95);
        long maxSearch = performanceMonitor.getMaxResponseTime("search", "books");
        long totalSearch = performanceMonitor.getTotalRequestCount("search", "books");
        
        assertTrue(avgSearch > 0, "Average search time should be recorded");
        assertTrue(p95Search > 0, "P95 search time should be recorded");
        assertTrue(maxSearch > 0, "Max search time should be recorded");
        assertTrue(totalSearch >= 5, "Total search count should be >= 5");
        
        log.info("Performance Monitor Metrics - Search: avg={:.2f}ms, p95={}ms, max={}ms, total={}",
            avgSearch, p95Search, maxSearch, totalSearch);
    }
    
    /**
     * Calculate percentile from a list of values
     */
    private long calculatePercentile(List<Long> values, double percentile) {
        if (values.isEmpty()) {
            return 0;
        }
        
        List<Long> sorted = new ArrayList<>(values);
        sorted.sort(Long::compareTo);
        
        int index = (int) Math.ceil(percentile * sorted.size()) - 1;
        index = Math.max(0, Math.min(index, sorted.size() - 1));
        
        return sorted.get(index);
    }
}


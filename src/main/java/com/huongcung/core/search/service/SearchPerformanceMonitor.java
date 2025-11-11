package com.huongcung.core.search.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Performance monitor for search operations
 * Tracks response times and provides metrics
 */
@Component
@Slf4j
public class SearchPerformanceMonitor {
    
    // Response time tracking
    private final Map<String, List<Long>> responseTimes = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> totalRequests = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> totalResponseTime = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> maxResponseTime = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> slowQueryCount = new ConcurrentHashMap<>();
    
    // Thresholds
    private static final long SLOW_QUERY_THRESHOLD_MS = 1000; // 1 second
    private static final int MAX_SAMPLES_PER_OPERATION = 1000; // Keep last 1000 samples
    
    /**
     * Record a search operation response time
     */
    public void recordSearchTime(String operation, long responseTimeMs) {
        recordTime("search", operation, responseTimeMs);
    }
    
    /**
     * Record a suggestion operation response time
     */
    public void recordSuggestionTime(String operation, long responseTimeMs) {
        recordTime("suggestion", operation, responseTimeMs);
    }
    
    /**
     * Record a facet operation response time
     */
    public void recordFacetTime(String operation, long responseTimeMs) {
        recordTime("facet", operation, responseTimeMs);
    }
    
    /**
     * Record response time for an operation
     */
    private void recordTime(String category, String operation, long responseTimeMs) {
        String key = category + "." + operation;
        
        // Update statistics
        totalRequests.computeIfAbsent(key, k -> new LongAdder()).increment();
        totalResponseTime.computeIfAbsent(key, k -> new LongAdder()).add(responseTimeMs);
        maxResponseTime.computeIfAbsent(key, k -> new AtomicLong(0))
            .updateAndGet(current -> Math.max(current, responseTimeMs));
        
        // Track slow queries
        if (responseTimeMs > SLOW_QUERY_THRESHOLD_MS) {
            slowQueryCount.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
            log.warn("Slow {} operation detected: {}ms (threshold: {}ms)", 
                operation, responseTimeMs, SLOW_QUERY_THRESHOLD_MS);
        }
        
        // Store response time samples (for percentile calculation)
        responseTimes.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>()))
            .add(responseTimeMs);
        
        // Keep only last N samples
        List<Long> samples = responseTimes.get(key);
        if (samples.size() > MAX_SAMPLES_PER_OPERATION) {
            samples.remove(0); // Remove oldest sample
        }
    }
    
    /**
     * Get average response time for an operation
     */
    public double getAverageResponseTime(String category, String operation) {
        String key = category + "." + operation;
        LongAdder totalTime = totalResponseTime.get(key);
        LongAdder totalRequests = this.totalRequests.get(key);
        
        if (totalTime == null || totalRequests == null || totalRequests.sum() == 0) {
            return 0.0;
        }
        
        return (double) totalTime.sum() / totalRequests.sum();
    }
    
    /**
     * Get percentile response time (p95, p99, etc.)
     */
    public long getPercentileResponseTime(String category, String operation, double percentile) {
        String key = category + "." + operation;
        List<Long> samples = responseTimes.get(key);
        
        if (samples == null || samples.isEmpty()) {
            return 0;
        }
        
        List<Long> sorted = new ArrayList<>(samples);
        Collections.sort(sorted);
        
        int index = (int) Math.ceil(percentile * sorted.size()) - 1;
        index = Math.max(0, Math.min(index, sorted.size() - 1));
        
        return sorted.get(index);
    }
    
    /**
     * Get maximum response time
     */
    public long getMaxResponseTime(String category, String operation) {
        String key = category + "." + operation;
        AtomicLong max = maxResponseTime.get(key);
        return max != null ? max.get() : 0;
    }
    
    /**
     * Get slow query count
     */
    public long getSlowQueryCount(String category, String operation) {
        String key = category + "." + operation;
        AtomicLong count = slowQueryCount.get(key);
        return count != null ? count.get() : 0;
    }
    
    /**
     * Get total request count
     */
    public long getTotalRequestCount(String category, String operation) {
        String key = category + "." + operation;
        LongAdder count = totalRequests.get(key);
        return count != null ? count.sum() : 0;
    }
    
    /**
     * Log performance summary
     */
    public void logPerformanceSummary() {
        log.info("=== Search Performance Summary ===");
        
        // Search operations
        logMetrics("search", "books");
        logMetrics("suggestion", "autocomplete");
        logMetrics("facet", "facets");
        
        log.info("===================================");
    }
    
    private void logMetrics(String category, String operation) {
        long total = getTotalRequestCount(category, operation);
        if (total == 0) {
            return;
        }
        
        double avg = getAverageResponseTime(category, operation);
        long p95 = getPercentileResponseTime(category, operation, 0.95);
        long p99 = getPercentileResponseTime(category, operation, 0.99);
        long max = getMaxResponseTime(category, operation);
        long slowCount = getSlowQueryCount(category, operation);
        
        log.info("{} - Total: {}, Avg: {:.2f}ms, P95: {}ms, P99: {}ms, Max: {}ms, Slow: {}",
            operation, total, avg, p95, p99, max, slowCount);
    }
    
    /**
     * Reset all metrics (useful for testing)
     */
    public void reset() {
        responseTimes.clear();
        totalRequests.clear();
        totalResponseTime.clear();
        maxResponseTime.clear();
        slowQueryCount.clear();
    }
}


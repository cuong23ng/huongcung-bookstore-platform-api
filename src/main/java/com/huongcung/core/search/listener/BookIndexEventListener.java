package com.huongcung.core.search.listener;

import com.huongcung.core.product.model.entity.AbstractBookEntity;
import com.huongcung.core.search.event.BookCreatedEvent;
import com.huongcung.core.search.event.BookDeletedEvent;
import com.huongcung.core.search.event.BookUpdatedEvent;
import com.huongcung.core.search.service.SearchIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event listener for book index synchronization
 * Handles book create/update/delete events and updates Solr index asynchronously
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookIndexEventListener {
    
    private final SearchIndexService searchIndexService;
    
    @Value("${solr.indexing.enabled:true}")
    private boolean indexingEnabled;
    
    @Value("${solr.indexing.retry.max-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${solr.indexing.retry.delay-ms:1000}")
    private long retryDelayMs;
    
    /**
     * Handle book creation event
     * Indexes the new book in Solr asynchronously
     */
    @Async("searchIndexExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBookCreated(BookCreatedEvent event) {
        if (!indexingEnabled) {
            log.debug("Indexing is disabled, skipping book creation event");
            return;
        }
        
        AbstractBookEntity book = event.getBook();
        if (book == null) {
            log.warn("Received BookCreatedEvent with null book");
            return;
        }
        
        log.debug("Handling book creation event for book ID: {}", book.getId());
        
        boolean success = executeWithRetry(() -> {
            boolean indexed = searchIndexService.indexBook(book);
            if (!indexed) {
                throw new RuntimeException("Failed to index book " + book.getId());
            }
        }, "index book " + book.getId());
        
        if (success) {
            log.info("Successfully indexed book after creation: {} (ID: {})", book.getTitle(), book.getId());
        } else {
            log.error("Failed to index book after creation: {} (ID: {}) after {} retries", 
                book.getTitle(), book.getId(), maxRetryAttempts);
        }
    }
    
    /**
     * Handle book update event
     * Updates the book in Solr index asynchronously
     */
    @Async("searchIndexExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBookUpdated(BookUpdatedEvent event) {
        if (!indexingEnabled) {
            log.debug("Indexing is disabled, skipping book update event");
            return;
        }
        
        Long bookId = event.getBookId();
        if (bookId == null) {
            log.warn("Received BookUpdatedEvent with null bookId");
            return;
        }
        
        log.debug("Handling book update event for book ID: {}", bookId);
        
        boolean success = executeWithRetry(() -> {
            boolean updated = searchIndexService.updateBookIndex(bookId);
            if (!updated) {
                throw new RuntimeException("Failed to update book index " + bookId);
            }
        }, "update book " + bookId);
        
        if (success) {
            log.info("Successfully updated index for book ID: {}", bookId);
        } else {
            log.error("Failed to update index for book ID: {} after {} retries", bookId, maxRetryAttempts);
        }
    }
    
    /**
     * Handle book deletion event
     * Removes the book from Solr index asynchronously
     */
    @Async("searchIndexExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBookDeleted(BookDeletedEvent event) {
        if (!indexingEnabled) {
            log.debug("Indexing is disabled, skipping book deletion event");
            return;
        }
        
        Long bookId = event.getBookId();
        if (bookId == null) {
            log.warn("Received BookDeletedEvent with null bookId");
            return;
        }
        
        log.debug("Handling book deletion event for book ID: {}", bookId);
        
        boolean success = executeWithRetry(() -> {
            boolean deleted = searchIndexService.deleteBookFromIndex(bookId);
            if (!deleted) {
                throw new RuntimeException("Failed to delete book from index " + bookId);
            }
        }, "delete book " + bookId);
        
        if (success) {
            log.info("Successfully deleted book from index: {}", bookId);
        } else {
            log.error("Failed to delete book from index: {} after {} retries", bookId, maxRetryAttempts);
        }
    }
    
    /**
     * Execute an operation with retry logic
     * Uses exponential backoff for retries
     * 
     * @param operation The operation to execute
     * @param operationName Name of the operation for logging
     * @return true if operation succeeded, false otherwise
     */
    private boolean executeWithRetry(Runnable operation, String operationName) {
        int attempt = 0;
        long delay = retryDelayMs;
        
        while (attempt < maxRetryAttempts) {
            try {
                operation.run();
                if (attempt > 0) {
                    log.info("Operation '{}' succeeded on retry attempt {}", operationName, attempt + 1);
                }
                return true;
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxRetryAttempts) {
                    log.error("Operation '{}' failed after {} attempts: {}", 
                        operationName, maxRetryAttempts, e.getMessage(), e);
                    return false;
                }
                
                log.warn("Operation '{}' failed (attempt {}/{}): {}. Retrying in {}ms...", 
                    operationName, attempt, maxRetryAttempts, e.getMessage(), delay);
                
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Retry delay interrupted for operation '{}'", operationName);
                    return false;
                }
                
                // Exponential backoff: double the delay for next retry
                delay *= 2;
            }
        }
        
        return false;
    }
}


package com.huongcung.core.search.service;

import com.huongcung.core.product.model.entity.AbstractBookEntity;
import com.huongcung.core.search.event.BookCreatedEvent;
import com.huongcung.core.search.event.BookDeletedEvent;
import com.huongcung.core.search.event.BookUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Service for publishing book index events
 * Can be called from book service methods to trigger index synchronization
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookIndexEventPublisher {
    
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * Publish event when a book is created
     * 
     * @param book The created book entity
     */
    public void publishBookCreated(AbstractBookEntity book) {
        log.debug("Publishing BookCreatedEvent for book ID: {}", book.getId());
        eventPublisher.publishEvent(new BookCreatedEvent(this, book));
    }
    
    /**
     * Publish event when a book is updated
     * 
     * @param bookId The ID of the updated book
     * @param book The updated book entity (can be null if only ID is available)
     */
    public void publishBookUpdated(Long bookId, AbstractBookEntity book) {
        log.debug("Publishing BookUpdatedEvent for book ID: {}", bookId);
        eventPublisher.publishEvent(new BookUpdatedEvent(this, bookId, book));
    }
    
    /**
     * Publish event when a book is deleted
     * 
     * @param bookId The ID of the deleted book
     */
    public void publishBookDeleted(Long bookId) {
        log.debug("Publishing BookDeletedEvent for book ID: {}", bookId);
        eventPublisher.publishEvent(new BookDeletedEvent(this, bookId));
    }
}


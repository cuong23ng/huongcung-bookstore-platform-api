package com.huongcung.core.search.listener;

import com.huongcung.core.product.model.entity.AbstractBookEntity;
import com.huongcung.core.search.event.BookCreatedEvent;
import com.huongcung.core.search.event.BookDeletedEvent;
import com.huongcung.core.search.event.BookUpdatedEvent;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

/**
 * JPA Entity Listener for AbstractBookEntity
 * Publishes Spring events when books are created, updated, or deleted
 * 
 * Note: This listener is registered via META-INF/orm.xml
 * Since JPA entity listeners are not Spring-managed, we use a static reference
 * to ApplicationEventPublisher that is initialized by BookEntityListenerInitializer
 */
@Slf4j
public class BookEntityListener {

    /**
     * -- SETTER --
     *  Set the ApplicationEventPublisher (called by BookEntityListenerInitializer)
     */
    @Setter
    private static ApplicationEventPublisher eventPublisher;

    /**
     * Called after a book entity is persisted (created)
     */
    @PostPersist
    public void postPersist(AbstractBookEntity book) {
        if (eventPublisher != null && book != null) {
            log.debug("Book persisted, publishing BookCreatedEvent for book ID: {}", book.getId());
            eventPublisher.publishEvent(new BookCreatedEvent(this, book));
        } else if (eventPublisher == null) {
            log.warn("ApplicationEventPublisher not initialized in BookEntityListener");
        }
    }
    
    /**
     * Called after a book entity is updated
     */
    @PostUpdate
    public void postUpdate(AbstractBookEntity book) {
        if (eventPublisher != null && book != null) {
            log.debug("Book updated, publishing BookUpdatedEvent for book ID: {}", book.getId());
            eventPublisher.publishEvent(new BookUpdatedEvent(this, book.getId(), book));
        } else if (eventPublisher == null) {
            log.warn("ApplicationEventPublisher not initialized in BookEntityListener");
        }
    }
    
    /**
     * Called after a book entity is removed (deleted)
     */
    @PostRemove
    public void postRemove(AbstractBookEntity book) {
        if (eventPublisher != null && book != null) {
            log.debug("Book removed, publishing BookDeletedEvent for book ID: {}", book.getId());
            eventPublisher.publishEvent(new BookDeletedEvent(this, book.getId()));
        } else if (eventPublisher == null) {
            log.warn("ApplicationEventPublisher not initialized in BookEntityListener");
        }
    }
}


package com.huongcung.core.search.event;

import com.huongcung.core.catalog.model.entity.AbstractBookEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a book is updated
 */
@Getter
public class BookUpdatedEvent extends ApplicationEvent {
    
    private final Long bookId;
    private final AbstractBookEntity abstractBook;
    
    public BookUpdatedEvent(Object source, Long bookId, AbstractBookEntity book) {
        super(source);
        this.bookId = bookId;
        this.abstractBook = book;
    }
    
    /**
     * Get the book entity
     */
    public AbstractBookEntity getBook() {
        return abstractBook;
    }
}


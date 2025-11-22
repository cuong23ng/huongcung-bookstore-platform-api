package com.huongcung.core.search.event;

import com.huongcung.core.catalog.model.entity.AbstractBookEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a book is created
 */
@Getter
public class BookCreatedEvent extends ApplicationEvent {
    
    private final AbstractBookEntity abstractBook;
    
    public BookCreatedEvent(Object source, AbstractBookEntity book) {
        super(source);
        this.abstractBook = book;
    }
    
    /**
     * Get the book entity
     */
    public AbstractBookEntity getBook() {
        return abstractBook;
    }
}


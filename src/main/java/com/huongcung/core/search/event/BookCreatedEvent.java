package com.huongcung.core.search.event;

import com.huongcung.core.catalog.model.entity.AbstractBookEntity;
import com.huongcung.core.catalog.model.entity.BookEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a book is created
 * Supports both AbstractBookEntity (new structure) and BookEntity (backward compatibility)
 */
@Getter
public class BookCreatedEvent extends ApplicationEvent {
    
    private final AbstractBookEntity abstractBook;
    private final BookEntity book; // For backward compatibility
    
    public BookCreatedEvent(Object source, AbstractBookEntity book) {
        super(source);
        this.abstractBook = book;
        this.book = null;
    }
    
    public BookCreatedEvent(Object source, BookEntity book) {
        super(source);
        this.abstractBook = null;
        this.book = book;
    }
    
    /**
     * Get the book entity (prefer AbstractBookEntity if available)
     */
    public Object getBook() {
        return abstractBook != null ? abstractBook : book;
    }
}


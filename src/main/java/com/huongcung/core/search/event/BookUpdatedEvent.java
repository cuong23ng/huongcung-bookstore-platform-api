package com.huongcung.core.search.event;

import com.huongcung.core.catalog.model.entity.AbstractBookEntity;
import com.huongcung.core.catalog.model.entity.BookEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a book is updated
 * Supports both AbstractBookEntity (new structure) and BookEntity (backward compatibility)
 */
@Getter
public class BookUpdatedEvent extends ApplicationEvent {
    
    private final Long bookId;
    private final AbstractBookEntity abstractBook;
    private final BookEntity book; // For backward compatibility
    
    public BookUpdatedEvent(Object source, Long bookId, AbstractBookEntity book) {
        super(source);
        this.bookId = bookId;
        this.abstractBook = book;
        this.book = null;
    }
    
    public BookUpdatedEvent(Object source, Long bookId, BookEntity book) {
        super(source);
        this.bookId = bookId;
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


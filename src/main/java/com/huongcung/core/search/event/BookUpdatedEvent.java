package com.huongcung.core.search.event;

import com.huongcung.core.catalog.model.entity.BookEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a book is updated
 */
@Getter
public class BookUpdatedEvent extends ApplicationEvent {
    
    private final Long bookId;
    private final BookEntity book;
    
    public BookUpdatedEvent(Object source, Long bookId, BookEntity book) {
        super(source);
        this.bookId = bookId;
        this.book = book;
    }
}


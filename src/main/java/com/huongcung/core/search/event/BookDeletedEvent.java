package com.huongcung.core.search.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a book is deleted
 */
@Getter
public class BookDeletedEvent extends ApplicationEvent {
    
    private final Long bookId;
    
    public BookDeletedEvent(Object source, Long bookId) {
        super(source);
        this.bookId = bookId;
    }
}


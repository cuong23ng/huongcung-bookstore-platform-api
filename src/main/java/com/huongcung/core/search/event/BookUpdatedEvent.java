package com.huongcung.core.search.event;

import com.huongcung.core.product.model.entity.AbstractBookEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a book is updated
 */
@Getter
public class BookUpdatedEvent extends ApplicationEvent {
    
    private final Long bookId;
    private final AbstractBookEntity book;
    
    public BookUpdatedEvent(Object source, Long bookId, AbstractBookEntity book) {
        super(source);
        this.bookId = bookId;
        this.book = book;
    }
}


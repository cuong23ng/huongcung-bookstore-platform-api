package com.huongcung.core.search.event;

import com.huongcung.core.product.model.entity.AbstractBookEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a book is created
 */
@Getter
public class BookCreatedEvent extends ApplicationEvent {
    
    private final AbstractBookEntity book;
    
    public BookCreatedEvent(Object source, AbstractBookEntity book) {
        super(source);
        this.book = book;
    }
}


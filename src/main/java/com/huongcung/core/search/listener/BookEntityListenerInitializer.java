package com.huongcung.core.search.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Initializes the BookEntityListener with ApplicationEventPublisher
 * Since JPA entity listeners are not Spring-managed, we need to inject
 * the ApplicationEventPublisher statically
 */
@Component
@Slf4j
public class BookEntityListenerInitializer implements ApplicationListener<ContextRefreshedEvent> {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public BookEntityListenerInitializer(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Initializing BookEntityListener with ApplicationEventPublisher");
        BookEntityListener.setEventPublisher(eventPublisher);
    }
}


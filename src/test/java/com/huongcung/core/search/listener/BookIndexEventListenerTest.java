package com.huongcung.core.search.listener;

import com.huongcung.core.product.model.entity.AbstractBookEntity;
import com.huongcung.core.search.event.BookCreatedEvent;
import com.huongcung.core.search.event.BookDeletedEvent;
import com.huongcung.core.search.event.BookUpdatedEvent;
import com.huongcung.core.search.service.SearchIndexService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookIndexEventListener
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookIndexEventListener Unit Tests")
class BookIndexEventListenerTest {

    @Mock
    private SearchIndexService searchIndexService;

    @InjectMocks
    private BookIndexEventListener eventListener;

    private AbstractBookEntity testBook;

    @BeforeEach
    void setUp() {
        // Enable indexing for tests
        ReflectionTestUtils.setField(eventListener, "indexingEnabled", true);
        ReflectionTestUtils.setField(eventListener, "maxRetryAttempts", 3);
        ReflectionTestUtils.setField(eventListener, "retryDelayMs", 100L); // Short delay for tests
        
        testBook = new AbstractBookEntity();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
    }

    @Test
    @DisplayName("Should handle book created event successfully")
    void testHandleBookCreated_Success() {
        // Given
        when(searchIndexService.indexBook(any(AbstractBookEntity.class))).thenReturn(true);
        BookCreatedEvent event = new BookCreatedEvent(this, testBook);

        // When
        eventListener.handleBookCreated(event);

        // Then
        verify(searchIndexService, times(1)).indexBook(testBook);
    }

    @Test
    @DisplayName("Should retry on indexing failure")
    void testHandleBookCreated_WithRetry() {
        // Given
        when(searchIndexService.indexBook(any(AbstractBookEntity.class)))
            .thenThrow(new RuntimeException("Solr error"))
            .thenThrow(new RuntimeException("Solr error"))
            .thenReturn(true);
        BookCreatedEvent event = new BookCreatedEvent(this, testBook);

        // When
        eventListener.handleBookCreated(event);

        // Then
        verify(searchIndexService, times(3)).indexBook(testBook);
    }

    @Test
    @DisplayName("Should handle book updated event successfully")
    void testHandleBookUpdated_Success() {
        // Given
        when(searchIndexService.updateBookIndex(1L)).thenReturn(true);
        BookUpdatedEvent event = new BookUpdatedEvent(this, 1L, testBook);

        // When
        eventListener.handleBookUpdated(event);

        // Then
        verify(searchIndexService, times(1)).updateBookIndex(1L);
    }

    @Test
    @DisplayName("Should handle book deleted event successfully")
    void testHandleBookDeleted_Success() {
        // Given
        when(searchIndexService.deleteBookFromIndex(1L)).thenReturn(true);
        BookDeletedEvent event = new BookDeletedEvent(this, 1L);

        // When
        eventListener.handleBookDeleted(event);

        // Then
        verify(searchIndexService, times(1)).deleteBookFromIndex(1L);
    }

    @Test
    @DisplayName("Should skip processing when indexing is disabled")
    void testHandleBookCreated_IndexingDisabled() {
        // Given
        ReflectionTestUtils.setField(eventListener, "indexingEnabled", false);
        BookCreatedEvent event = new BookCreatedEvent(this, testBook);

        // When
        eventListener.handleBookCreated(event);

        // Then
        verify(searchIndexService, never()).indexBook(any());
    }

    @Test
    @DisplayName("Should handle null book gracefully")
    void testHandleBookCreated_NullBook() {
        // Given
        BookCreatedEvent event = new BookCreatedEvent(this, null);

        // When
        eventListener.handleBookCreated(event);

        // Then
        verify(searchIndexService, never()).indexBook(any());
    }

    @Test
    @DisplayName("Should handle null bookId gracefully")
    void testHandleBookUpdated_NullBookId() {
        // Given
        BookUpdatedEvent event = new BookUpdatedEvent(this, null, testBook);

        // When
        eventListener.handleBookUpdated(event);

        // Then
        verify(searchIndexService, never()).updateBookIndex(any());
    }

    @Test
    @DisplayName("Should fail after max retry attempts")
    void testHandleBookCreated_MaxRetriesExceeded() {
        // Given
        when(searchIndexService.indexBook(any(AbstractBookEntity.class)))
            .thenThrow(new RuntimeException("Persistent error"));
        BookCreatedEvent event = new BookCreatedEvent(this, testBook);

        // When
        eventListener.handleBookCreated(event);

        // Then
        verify(searchIndexService, times(3)).indexBook(testBook); // Max retries
    }
}


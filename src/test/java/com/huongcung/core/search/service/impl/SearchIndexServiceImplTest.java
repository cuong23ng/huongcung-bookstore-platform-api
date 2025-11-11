package com.huongcung.core.search.service.impl;

import com.huongcung.core.common.enumeration.Language;
import com.huongcung.core.contributor.model.entity.AuthorEntity;
import com.huongcung.core.contributor.model.entity.PublisherEntity;
import com.huongcung.core.product.model.entity.AbstractBookEntity;
import com.huongcung.core.product.model.entity.EbookEntity;
import com.huongcung.core.product.model.entity.GenreEntity;
import com.huongcung.core.product.model.entity.PhysicalBookEntity;
import com.huongcung.core.product.repository.AbstractBookRepository;
import com.huongcung.core.search.model.entity.BookSearchDocument;
import com.huongcung.core.search.repository.BookSearchRepository;
import com.huongcung.core.search.service.SearchIndexService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SearchIndexServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SearchIndexService Unit Tests")
class SearchIndexServiceImplTest {

    @Mock
    private BookSearchRepository bookSearchRepository;

    @Mock
    private AbstractBookRepository abstractBookRepository;

    @InjectMocks
    private SearchIndexServiceImpl searchIndexService;

    private AbstractBookEntity testBook;
    private PhysicalBookEntity physicalBook;
    private EbookEntity ebook;

    @BeforeEach
    void setUp() {
        // Create test publisher
        PublisherEntity publisher = new PublisherEntity();
        publisher.setId(1L);
        publisher.setName("Test Publisher");

        // Create test authors
        AuthorEntity author1 = new AuthorEntity();
        author1.setId(1L);
        author1.setName("Author One");

        AuthorEntity author2 = new AuthorEntity();
        author2.setId(2L);
        author2.setName("Author Two");

        // Create test genres
        GenreEntity genre1 = new GenreEntity();
        genre1.setId(1L);
        genre1.setName("Fiction");

        GenreEntity genre2 = new GenreEntity();
        genre2.setId(2L);
        genre2.setName("Science");

        // Create physical book
        physicalBook = new PhysicalBookEntity();
        physicalBook.setId(1L);
        physicalBook.setCode("BK001");
        physicalBook.setTitle("Test Physical Book");
        physicalBook.setDescription("Test description");
        physicalBook.setAuthors(Arrays.asList(author1, author2));
        physicalBook.setGenres(Arrays.asList(genre1, genre2));
        physicalBook.setPublisher(publisher);
        physicalBook.setLanguage(Language.VIETNAMESE);
        physicalBook.setPublicationDate(LocalDate.of(2020, 1, 1));
        physicalBook.setHasPhysicalEdition(true);
        physicalBook.setHasElectricEdition(false);
        physicalBook.setIsbn("1234567890");
        physicalBook.setCurrentPrice(new BigDecimal("150000"));

        // Create ebook
        ebook = new EbookEntity();
        ebook.setId(2L);
        ebook.setCode("BK002");
        ebook.setTitle("Test Ebook");
        ebook.setDescription("Ebook description");
        ebook.setAuthors(Arrays.asList(author1));
        ebook.setGenres(Arrays.asList(genre1));
        ebook.setPublisher(publisher);
        ebook.setLanguage(Language.ENGLISH);
        ebook.setPublicationDate(LocalDate.of(2021, 1, 1));
        ebook.setHasPhysicalEdition(false);
        ebook.setHasElectricEdition(true);
        ebook.setCurrentPrice(new BigDecimal("75000"));

        // Create basic book
        testBook = new AbstractBookEntity();
        testBook.setId(3L);
        testBook.setCode("BK003");
        testBook.setTitle("Test Book");
        testBook.setDescription("Description");
        testBook.setAuthors(Arrays.asList(author1));
        testBook.setGenres(Arrays.asList(genre1));
        testBook.setPublisher(publisher);
        testBook.setLanguage(Language.VIETNAMESE);
        testBook.setPublicationDate(LocalDate.of(2022, 1, 1));
        testBook.setHasPhysicalEdition(true);
        testBook.setHasElectricEdition(true);
    }

    @Test
    @DisplayName("Should successfully index a single book")
    void testIndexBook_Success() throws Exception {
        // When
        boolean result = searchIndexService.indexBook(physicalBook);

        // Then
        assertTrue(result);
        verify(bookSearchRepository, times(1)).index(any(BookSearchDocument.class));
    }

    @Test
    @DisplayName("Should handle indexing failure gracefully")
    void testIndexBook_Failure() throws Exception {
        // Given
        doThrow(new RuntimeException("Solr error")).when(bookSearchRepository).index(any(BookSearchDocument.class));

        // When
        boolean result = searchIndexService.indexBook(physicalBook);

        // Then
        assertFalse(result);
        verify(bookSearchRepository, times(1)).index(any(BookSearchDocument.class));
    }

    @Test
    @DisplayName("Should map physical book correctly to document")
    void testIndexBook_PhysicalBookMapping() throws Exception {
        // When
        searchIndexService.indexBook(physicalBook);

        // Then
        ArgumentCaptor<BookSearchDocument> captor = ArgumentCaptor.forClass(BookSearchDocument.class);
        verify(bookSearchRepository).index(captor.capture());
        
        BookSearchDocument document = captor.getValue();
        assertEquals("1", document.getId());
        assertEquals("Test Physical Book", document.getTitle());
        assertEquals("Test Physical Book", document.getTitleText());
        assertEquals("Test description", document.getDescription());
        assertEquals("1234567890", document.getIsbn());
        assertEquals(2, document.getAuthorNames().size());
        assertTrue(document.getAuthorNames().contains("Author One"));
        assertTrue(document.getAuthorNames().contains("Author Two"));
        assertEquals(2, document.getGenreNames().size());
        assertEquals("Test Publisher", document.getPublisherName());
        assertEquals("VIETNAMESE", document.getLanguage());
        assertEquals("PHYSICAL", document.getFormat());
        assertEquals(150000.0, document.getPhysicalPrice());
        assertNull(document.getDigitalPrice());
    }

    @Test
    @DisplayName("Should map ebook correctly to document")
    void testIndexBook_EbookMapping() throws Exception {
        // When
        searchIndexService.indexBook(ebook);

        // Then
        ArgumentCaptor<BookSearchDocument> captor = ArgumentCaptor.forClass(BookSearchDocument.class);
        verify(bookSearchRepository).index(captor.capture());
        
        BookSearchDocument document = captor.getValue();
        assertEquals("2", document.getId());
        assertEquals("Test Ebook", document.getTitle());
        assertEquals("DIGITAL", document.getFormat());
        assertEquals(75000.0, document.getDigitalPrice());
        assertNull(document.getPhysicalPrice());
    }

    @Test
    @DisplayName("Should map book with both editions correctly")
    void testIndexBook_BothEditions() throws Exception {
        // When
        searchIndexService.indexBook(testBook);

        // Then
        ArgumentCaptor<BookSearchDocument> captor = ArgumentCaptor.forClass(BookSearchDocument.class);
        verify(bookSearchRepository).index(captor.capture());
        
        BookSearchDocument document = captor.getValue();
        assertEquals("BOTH", document.getFormat());
    }

    @Test
    @DisplayName("Should index all books in batches")
    void testIndexAllBooks_Success() throws Exception {
        // Given
        List<AbstractBookEntity> books = Arrays.asList(physicalBook, ebook, testBook);
        when(abstractBookRepository.findAll()).thenReturn(books);
        doNothing().when(bookSearchRepository).indexBatch(anyList());

        // When
        SearchIndexService.IndexingResult result = searchIndexService.indexAllBooks();

        // Then
        assertEquals(3, result.getTotalBooks());
        assertEquals(3, result.getIndexedCount());
        assertEquals(0, result.getErrorCount());
        assertTrue(result.getDurationMs() > 0);
        verify(bookSearchRepository, atLeastOnce()).indexBatch(anyList());
    }

    @Test
    @DisplayName("Should handle batch indexing errors gracefully")
    void testIndexAllBooks_WithErrors() throws Exception {
        // Given
        List<AbstractBookEntity> books = Arrays.asList(physicalBook, ebook, testBook);
        when(abstractBookRepository.findAll()).thenReturn(books);
        doThrow(new RuntimeException("Batch error")).when(bookSearchRepository).indexBatch(anyList());
        doNothing().when(bookSearchRepository).index(any(BookSearchDocument.class));

        // When
        SearchIndexService.IndexingResult result = searchIndexService.indexAllBooks();

        // Then
        assertEquals(3, result.getTotalBooks());
        // Should have attempted individual indexing after batch failure
        verify(bookSearchRepository, atLeast(3)).index(any(BookSearchDocument.class));
    }

    @Test
    @DisplayName("Should return empty result when no books found")
    void testIndexAllBooks_NoBooks() throws Exception {
        // Given
        when(abstractBookRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        SearchIndexService.IndexingResult result = searchIndexService.indexAllBooks();

        // Then
        assertEquals(0, result.getTotalBooks());
        assertEquals(0, result.getIndexedCount());
        assertEquals(0, result.getErrorCount());
        verify(bookSearchRepository, never()).indexBatch(anyList());
    }

    @Test
    @DisplayName("Should update book index successfully")
    void testUpdateBookIndex_Success() {
        // Given
        when(abstractBookRepository.findById(1L)).thenReturn(Optional.of(physicalBook));
        try {
            doNothing().when(bookSearchRepository).index(any(BookSearchDocument.class));
        } catch (Exception e) {
            // Ignore
        }

        // When
        boolean result = searchIndexService.updateBookIndex(1L);

        // Then
        assertTrue(result);
        verify(abstractBookRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should handle update when book not found")
    void testUpdateBookIndex_BookNotFound() {
        // Given
        when(abstractBookRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        boolean result = searchIndexService.updateBookIndex(999L);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should delete book from index successfully")
    void testDeleteBookFromIndex_Success() throws Exception {
        // Given
        doNothing().when(bookSearchRepository).deleteById("1");

        // When
        boolean result = searchIndexService.deleteBookFromIndex(1L);

        // Then
        assertTrue(result);
        verify(bookSearchRepository, times(1)).deleteById("1");
    }

    @Test
    @DisplayName("Should handle deletion failure gracefully")
    void testDeleteBookFromIndex_Failure() throws Exception {
        // Given
        doThrow(new RuntimeException("Delete error")).when(bookSearchRepository).deleteById("1");

        // When
        boolean result = searchIndexService.deleteBookFromIndex(1L);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should handle null authors gracefully")
    void testIndexBook_NullAuthors() throws Exception {
        // Given
        physicalBook.setAuthors(null);

        // When
        boolean result = searchIndexService.indexBook(physicalBook);

        // Then
        assertTrue(result);
        ArgumentCaptor<BookSearchDocument> captor = ArgumentCaptor.forClass(BookSearchDocument.class);
        verify(bookSearchRepository).index(captor.capture());
        assertNull(captor.getValue().getAuthorNames());
    }

    @Test
    @DisplayName("Should handle null genres gracefully")
    void testIndexBook_NullGenres() throws Exception {
        // Given
        physicalBook.setGenres(null);

        // When
        boolean result = searchIndexService.indexBook(physicalBook);

        // Then
        assertTrue(result);
        ArgumentCaptor<BookSearchDocument> captor = ArgumentCaptor.forClass(BookSearchDocument.class);
        verify(bookSearchRepository).index(captor.capture());
        assertNull(captor.getValue().getGenreNames());
    }

    @Test
    @DisplayName("Should handle null publisher gracefully")
    void testIndexBook_NullPublisher() throws Exception {
        // Given
        physicalBook.setPublisher(null);

        // When
        boolean result = searchIndexService.indexBook(physicalBook);

        // Then
        assertTrue(result);
        ArgumentCaptor<BookSearchDocument> captor = ArgumentCaptor.forClass(BookSearchDocument.class);
        verify(bookSearchRepository).index(captor.capture());
        assertNull(captor.getValue().getPublisherName());
    }
}


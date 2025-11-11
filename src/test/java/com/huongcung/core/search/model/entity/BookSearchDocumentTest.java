package com.huongcung.core.search.model.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BookSearchDocument
 */
@DisplayName("BookSearchDocument Tests")
class BookSearchDocumentTest {
    
    private BookSearchDocument document;
    
    @BeforeEach
    void setUp() {
        document = new BookSearchDocument();
    }
    
    @Test
    @DisplayName("Should create document with no-args constructor")
    void testNoArgsConstructor() {
        assertNotNull(document);
        assertNull(document.getId());
        assertNull(document.getTitle());
    }
    
    @Test
    @DisplayName("Should set and get all fields correctly")
    void testFieldGettersAndSetters() {
        // String fields
        document.setId("1");
        document.setTitle("Test Book Title");
        document.setTitleText("Test Book Title");
        document.setDescription("Test book description");
        document.setDescriptionText("Test book description");
        document.setIsbn("978-1234567890");
        document.setPublisherName("Test Publisher");
        document.setLanguage("Vietnamese");
        document.setFormat("BOTH");
        
        // Multi-valued fields
        List<String> authors = Arrays.asList("Author 1", "Author 2");
        document.setAuthorNames(authors);
        
        List<String> genres = Arrays.asList("Fiction", "Adventure");
        document.setGenreNames(genres);
        
        // Numeric fields
        document.setPhysicalPrice(100000.0);
        document.setDigitalPrice(50000.0);
        document.setAverageRating(4.5);
        document.setReviewCount(25);
        
        // Boolean fields
        document.setAvailableInHanoi(true);
        document.setAvailableInHcmc(true);
        document.setAvailableInDanang(false);
        
        // Date fields
        Date now = new Date();
        document.setPublicationDate(now);
        document.setCreatedAt(now);
        
        // Assertions
        assertEquals("1", document.getId());
        assertEquals("Test Book Title", document.getTitle());
        assertEquals("Test Book Title", document.getTitleText());
        assertEquals("Test book description", document.getDescription());
        assertEquals("Test book description", document.getDescriptionText());
        assertEquals("978-1234567890", document.getIsbn());
        assertEquals(authors, document.getAuthorNames());
        assertEquals("Test Publisher", document.getPublisherName());
        assertEquals(genres, document.getGenreNames());
        assertEquals("Vietnamese", document.getLanguage());
        assertEquals("BOTH", document.getFormat());
        assertEquals(100000.0, document.getPhysicalPrice());
        assertEquals(50000.0, document.getDigitalPrice());
        assertEquals(4.5, document.getAverageRating());
        assertEquals(25, document.getReviewCount());
        assertTrue(document.getAvailableInHanoi());
        assertTrue(document.getAvailableInHcmc());
        assertFalse(document.getAvailableInDanang());
        assertEquals(now, document.getPublicationDate());
        assertEquals(now, document.getCreatedAt());
    }
    
    @Test
    @DisplayName("Should handle null values correctly")
    void testNullValues() {
        document.setId(null);
        document.setTitle(null);
        document.setAuthorNames(null);
        document.setGenreNames(null);
        document.setPhysicalPrice(null);
        document.setDigitalPrice(null);
        document.setAvailableInHanoi(null);
        
        assertNull(document.getId());
        assertNull(document.getTitle());
        assertNull(document.getAuthorNames());
        assertNull(document.getGenreNames());
        assertNull(document.getPhysicalPrice());
        assertNull(document.getDigitalPrice());
        assertNull(document.getAvailableInHanoi());
    }
    
    @Test
    @DisplayName("Should handle empty lists for multi-valued fields")
    void testEmptyLists() {
        document.setAuthorNames(Arrays.asList());
        document.setGenreNames(Arrays.asList());
        
        assertNotNull(document.getAuthorNames());
        assertTrue(document.getAuthorNames().isEmpty());
        assertNotNull(document.getGenreNames());
        assertTrue(document.getGenreNames().isEmpty());
    }
    
    @Test
    @DisplayName("Should create document with all-args constructor")
    void testAllArgsConstructor() {
        Date now = new Date();
        List<String> authors = Arrays.asList("Author 1");
        List<String> genres = Arrays.asList("Fiction");
        
        BookSearchDocument doc = new BookSearchDocument(
            "1",
            "Test Title",
            "Test Title",
            "Test Description",
            "Test Description",
            "978-1234567890",
            authors,
            "Test Publisher",
            genres,
            "Vietnamese",
            "PHYSICAL",
            100000.0,
            50000.0,
            now,
            true,
            true,
            false,
            4.5,
            25,
            now
        );
        
        assertEquals("1", doc.getId());
        assertEquals("Test Title", doc.getTitle());
        assertEquals(authors, doc.getAuthorNames());
        assertEquals(genres, doc.getGenreNames());
        assertEquals("PHYSICAL", doc.getFormat());
        assertTrue(doc.getAvailableInHanoi());
        assertFalse(doc.getAvailableInDanang());
    }
    
    @Test
    @DisplayName("Should handle format values correctly")
    void testFormatValues() {
        document.setFormat("PHYSICAL");
        assertEquals("PHYSICAL", document.getFormat());
        
        document.setFormat("DIGITAL");
        assertEquals("DIGITAL", document.getFormat());
        
        document.setFormat("BOTH");
        assertEquals("BOTH", document.getFormat());
    }
    
    @Test
    @DisplayName("Should handle price values correctly")
    void testPriceValues() {
        document.setPhysicalPrice(0.0);
        assertEquals(0.0, document.getPhysicalPrice());
        
        document.setPhysicalPrice(999999.99);
        assertEquals(999999.99, document.getPhysicalPrice());
        
        document.setDigitalPrice(0.0);
        assertEquals(0.0, document.getDigitalPrice());
    }
}


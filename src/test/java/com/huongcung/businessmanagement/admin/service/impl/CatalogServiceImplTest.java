package com.huongcung.businessmanagement.admin.service.impl;

import com.huongcung.businessmanagement.admin.mapper.BookMapper;
import com.huongcung.businessmanagement.admin.model.BookCreateRequest;
import com.huongcung.businessmanagement.admin.model.BookDetailDTO;
import com.huongcung.businessmanagement.admin.model.BookImageData;
import com.huongcung.businessmanagement.admin.model.BookListDTO;
import com.huongcung.businessmanagement.admin.model.BookUpdateRequest;
import com.huongcung.core.common.enumeration.Language;
import com.huongcung.core.contributor.model.entity.AuthorEntity;
import com.huongcung.core.contributor.model.entity.PublisherEntity;
import com.huongcung.core.contributor.model.entity.TranslatorEntity;
import com.huongcung.core.contributor.repository.AuthorRepository;
import com.huongcung.core.contributor.repository.PublisherRepository;
import com.huongcung.core.contributor.repository.TranslatorRepository;
import com.huongcung.core.media.model.entity.BookImageEntity;
import com.huongcung.core.media.repository.BookImageRepository;
import com.huongcung.core.media.service.ImageService;
import com.huongcung.core.product.model.entity.AbstractBookEntity;
import com.huongcung.core.product.model.entity.EbookEntity;
import com.huongcung.core.product.model.entity.GenreEntity;
import com.huongcung.core.product.model.entity.PhysicalBookEntity;
import com.huongcung.core.product.repository.AbstractBookRepository;
import com.huongcung.core.product.repository.GenreRepository;
import com.huongcung.core.search.model.dto.PaginationInfo;
import com.huongcung.core.search.service.SearchIndexService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CatalogServiceImpl
 * Test ID: 3.1-UNIT-001
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CatalogServiceImpl Unit Tests")
class CatalogServiceImplTest {
    
    @Mock
    private AbstractBookRepository bookRepository;
    
    @Mock
    private AuthorRepository authorRepository;
    
    @Mock
    private PublisherRepository publisherRepository;
    
    @Mock
    private TranslatorRepository translatorRepository;
    
    @Mock
    private GenreRepository genreRepository;
    
    @Mock
    private BookMapper bookMapper;
    
    @Mock
    private ImageService imageService;
    
    @Mock
    private BookImageRepository bookImageRepository;
    
    @Mock
    private EntityManager entityManager;
    
    @Mock
    private SearchIndexService searchIndexService;
    
    @Mock
    private CriteriaBuilder criteriaBuilder;
    
    @Mock
    private CriteriaQuery<AbstractBookEntity> criteriaQuery;
    
    @Mock
    private CriteriaQuery<Long> countQuery;
    
    @Mock
    private TypedQuery<AbstractBookEntity> typedQuery;
    
    @Mock
    private TypedQuery<Long> countTypedQuery;
    
    @InjectMocks
    private CatalogServiceImpl catalogService;
    
    private AuthorEntity testAuthor;
    private PublisherEntity testPublisher;
    private GenreEntity testGenre;
    private PhysicalBookEntity testPhysicalBook;
    private EbookEntity testEbook;
    
    @BeforeEach
    void setUp() {
        // Setup test author
        testAuthor = new AuthorEntity();
        testAuthor.setId(1L);
        testAuthor.setName("Test Author");
        
        // Setup test publisher
        testPublisher = new PublisherEntity();
        testPublisher.setId(1L);
        testPublisher.setName("Test Publisher");
        
        // Setup test genre
        testGenre = new GenreEntity();
        testGenre.setId(1L);
        testGenre.setName("Fiction");
        
        // Setup test physical book
        testPhysicalBook = new PhysicalBookEntity();
        testPhysicalBook.setId(1L);
        testPhysicalBook.setCode("BK001");
        testPhysicalBook.setTitle("Test Physical Book");
        testPhysicalBook.setLanguage(Language.VIETNAMESE);
        testPhysicalBook.setAuthors(Arrays.asList(testAuthor));
        testPhysicalBook.setPublisher(testPublisher);
        testPhysicalBook.setGenres(Arrays.asList(testGenre));
        testPhysicalBook.setIsbn("1234567890");
        testPhysicalBook.setCurrentPrice(new BigDecimal("150000"));
        testPhysicalBook.setIsActive(true);
        
        // Setup test ebook
        testEbook = new EbookEntity();
        testEbook.setId(2L);
        testEbook.setCode("BK002");
        testEbook.setTitle("Test Ebook");
        testEbook.setLanguage(Language.ENGLISH);
        testEbook.setAuthors(Arrays.asList(testAuthor));
        testEbook.setFileUrl("https://example.com/book.pdf");
        testEbook.setFileName("book.pdf");
        testEbook.setFileFormat("PDF");
        testEbook.setCurrentPrice(new BigDecimal("100000"));
        testEbook.setIsActive(true);
    }
    
    @Test
    @DisplayName("Should create PhysicalBook successfully")
    void testCreateBook_PhysicalBook_Success() {
        // Given
        BookCreateRequest request = new BookCreateRequest();
        request.setTitle("New Physical Book");
        request.setLanguage(Language.VIETNAMESE);
        request.setAuthorIds(Arrays.asList(1L));
        request.setBookType("PHYSICAL");
        request.setIsbn("9876543210");
        request.setCurrentPrice(new BigDecimal("200000"));
        
        when(authorRepository.findByIdIn(anyList())).thenReturn(Arrays.asList(testAuthor));
        when(bookRepository.save(any(AbstractBookEntity.class))).thenAnswer(invocation -> {
            PhysicalBookEntity book = invocation.getArgument(0);
            book.setId(1L);
            book.setCode("NEW-ABC12345");
            return book;
        });
        
        BookDetailDTO bookDTO = BookDetailDTO.builder()
                .id(1L)
                .code("NEW-ABC12345")
                .title("New Physical Book")
                .build();
        when(bookMapper.toDetailDTO(any(AbstractBookEntity.class))).thenReturn(bookDTO);
        
        // When
        BookDetailDTO result = catalogService.createBook(request);
        
        // Then
        assertNotNull(result);
        assertEquals("New Physical Book", result.getTitle());
        verify(bookRepository, times(1)).save(any(PhysicalBookEntity.class));
        verify(authorRepository, times(1)).findByIdIn(anyList());
    }
    
    @Test
    @DisplayName("Should create Ebook successfully")
    void testCreateBook_Ebook_Success() {
        // Given
        BookCreateRequest request = new BookCreateRequest();
        request.setTitle("New Ebook");
        request.setLanguage(Language.ENGLISH);
        request.setAuthorIds(Arrays.asList(1L));
        request.setBookType("EBOOK");
        request.setFileUrl("https://example.com/ebook.pdf");
        request.setFileName("ebook.pdf");
        request.setFileFormat("PDF");
        request.setCurrentPrice(new BigDecimal("150000"));
        
        when(authorRepository.findByIdIn(anyList())).thenReturn(Arrays.asList(testAuthor));
        when(bookRepository.save(any(AbstractBookEntity.class))).thenAnswer(invocation -> {
            EbookEntity book = invocation.getArgument(0);
            book.setId(2L);
            book.setCode("NEW-DEF67890");
            return book;
        });
        
        BookDetailDTO bookDTO = BookDetailDTO.builder()
                .id(2L)
                .code("NEW-DEF67890")
                .title("New Ebook")
                .bookType("EBOOK")
                .build();
        when(bookMapper.toDetailDTO(any(AbstractBookEntity.class))).thenReturn(bookDTO);
        
        // When
        BookDetailDTO result = catalogService.createBook(request);
        
        // Then
        assertNotNull(result);
        assertEquals("New Ebook", result.getTitle());
        assertEquals("EBOOK", result.getBookType());
        verify(bookRepository, times(1)).save(any(EbookEntity.class));
    }
    
    @Test
    @DisplayName("Should throw exception when title is missing")
    void testCreateBook_MissingTitle_ThrowsException() {
        // Given
        BookCreateRequest request = new BookCreateRequest();
        request.setLanguage(Language.VIETNAMESE);
        request.setAuthorIds(Arrays.asList(1L));
        request.setBookType("PHYSICAL");
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> catalogService.createBook(request));
        assertEquals("Title is required", exception.getMessage());
        verify(bookRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should throw exception when author is missing")
    void testCreateBook_MissingAuthor_ThrowsException() {
        // Given
        BookCreateRequest request = new BookCreateRequest();
        request.setTitle("Test Book");
        request.setLanguage(Language.VIETNAMESE);
        request.setBookType("PHYSICAL");
        request.setAuthorIds(Collections.emptyList());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> catalogService.createBook(request));
        assertEquals("At least one author is required", exception.getMessage());
        verify(bookRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should throw exception when author ID not found")
    void testCreateBook_AuthorNotFound_ThrowsException() {
        // Given
        BookCreateRequest request = new BookCreateRequest();
        request.setTitle("Test Book");
        request.setLanguage(Language.VIETNAMESE);
        request.setAuthorIds(Arrays.asList(999L));
        request.setBookType("PHYSICAL");
        
        when(authorRepository.findByIdIn(anyList())).thenReturn(Collections.emptyList());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> catalogService.createBook(request));
        assertTrue(exception.getMessage().contains("author IDs not found"));
        verify(bookRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should upload images when provided in create request")
    void testCreateBook_WithImages_UploadsImages() {
        // Given
        BookCreateRequest request = new BookCreateRequest();
        request.setTitle("New Book with Images");
        request.setLanguage(Language.VIETNAMESE);
        request.setAuthorIds(Arrays.asList(1L));
        request.setBookType("PHYSICAL");
        
        BookImageData imageData = new BookImageData();
        imageData.setBase64Data("data:image/jpeg;base64,/9j/4AAQSkZJRg==");
        imageData.setAltText("Cover image");
        imageData.setPosition(1);
        request.setImages(Arrays.asList(imageData));
        
        when(authorRepository.findByIdIn(anyList())).thenReturn(Arrays.asList(testAuthor));
        when(bookRepository.save(any(AbstractBookEntity.class))).thenAnswer(invocation -> {
            PhysicalBookEntity book = invocation.getArgument(0);
            book.setId(1L);
            book.setCode("NEW-IMG12345");
            return book;
        });
        when(imageService.saveImageFromBase64(anyString(), anyString(), anyString()))
                .thenReturn("books/1/image_1.jpg");
        when(imageService.getFullUrl(anyString())).thenReturn("http://s3.example.com/books/1/image_1.jpg");
        
        BookDetailDTO bookDTO = BookDetailDTO.builder()
                .id(1L)
                .code("NEW-IMG12345")
                .title("New Book with Images")
                .build();
        when(bookMapper.toDetailDTO(any(AbstractBookEntity.class))).thenReturn(bookDTO);
        
        // When
        BookDetailDTO result = catalogService.createBook(request);
        
        // Then
        assertNotNull(result);
        verify(imageService, times(1)).saveImageFromBase64(anyString(), anyString(), anyString());
        verify(bookImageRepository, times(1)).save(any(BookImageEntity.class));
    }
    
    @Test
    @DisplayName("Should get book by ID successfully")
    void testGetBookById_Success() {
        // Given
        Long bookId = 1L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(testPhysicalBook));
        
        BookDetailDTO bookDTO = BookDetailDTO.builder()
                .id(1L)
                .code("BK001")
                .title("Test Physical Book")
                .build();
        when(bookMapper.toDetailDTO(testPhysicalBook)).thenReturn(bookDTO);
        
        // When
        BookDetailDTO result = catalogService.getBookById(bookId);
        
        // Then
        assertNotNull(result);
        assertEquals("BK001", result.getCode());
        assertEquals("Test Physical Book", result.getTitle());
        verify(bookRepository, times(1)).findById(bookId);
    }
    
    @Test
    @DisplayName("Should throw exception when book not found")
    void testGetBookById_NotFound_ThrowsException() {
        // Given
        Long bookId = 999L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> catalogService.getBookById(bookId));
        assertTrue(exception.getMessage().contains("Book not found"));
    }
    
    @Test
    @DisplayName("Should update book successfully")
    void testUpdateBook_Success() {
        // Given
        Long bookId = 1L;
        BookUpdateRequest request = new BookUpdateRequest();
        request.setTitle("Updated Title");
        request.setDescription("Updated description");
        
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(testPhysicalBook));
        when(bookRepository.save(any(AbstractBookEntity.class))).thenReturn(testPhysicalBook);
        
        BookDetailDTO bookDTO = BookDetailDTO.builder()
                .id(1L)
                .title("Updated Title")
                .description("Updated description")
                .build();
        when(bookMapper.toDetailDTO(testPhysicalBook)).thenReturn(bookDTO);
        
        // When
        BookDetailDTO result = catalogService.updateBook(bookId, request, "admin@example.com");
        
        // Then
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        verify(bookRepository, times(1)).save(any(AbstractBookEntity.class));
    }
    
    @Test
    @DisplayName("Should update book relationships successfully")
    void testUpdateBook_UpdateRelationships_Success() {
        // Given
        Long bookId = 1L;
        BookUpdateRequest request = new BookUpdateRequest();
        request.setAuthorIds(Arrays.asList(1L, 2L));
        request.setPublisherId(1L);
        request.setGenreIds(Arrays.asList(1L, 2L));
        
        AuthorEntity author2 = new AuthorEntity();
        author2.setId(2L);
        author2.setName("Author Two");
        
        GenreEntity genre2 = new GenreEntity();
        genre2.setId(2L);
        genre2.setName("Science");
        
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(testPhysicalBook));
        when(authorRepository.findByIdIn(anyList())).thenReturn(Arrays.asList(testAuthor, author2));
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(testPublisher));
        when(genreRepository.findByIdIn(anyList())).thenReturn(Arrays.asList(testGenre, genre2));
        when(bookRepository.save(any(AbstractBookEntity.class))).thenReturn(testPhysicalBook);
        
        BookDetailDTO bookDTO = BookDetailDTO.builder()
                .id(1L)
                .build();
        when(bookMapper.toDetailDTO(testPhysicalBook)).thenReturn(bookDTO);
        
        // When
        BookDetailDTO result = catalogService.updateBook(bookId, request, "admin@example.com");
        
        // Then
        assertNotNull(result);
        verify(authorRepository, times(1)).findByIdIn(anyList());
        verify(publisherRepository, times(1)).findById(1L);
        verify(genreRepository, times(1)).findByIdIn(anyList());
    }
    
    @Test
    @DisplayName("Should deactivate book successfully")
    void testDeactivateBook_Success() {
        // Given
        Long bookId = 1L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(testPhysicalBook));
        when(bookRepository.save(any(AbstractBookEntity.class))).thenAnswer(invocation -> {
            AbstractBookEntity book = invocation.getArgument(0);
            book.setIsActive(false);
            return book;
        });
        
        BookDetailDTO bookDTO = BookDetailDTO.builder()
                .id(1L)
                .isActive(false)
                .build();
        when(bookMapper.toDetailDTO(any(AbstractBookEntity.class))).thenReturn(bookDTO);
        
        // When
        BookDetailDTO result = catalogService.deactivateBook(bookId, "admin@example.com");
        
        // Then
        assertNotNull(result);
        assertFalse(result.getIsActive());
        verify(bookRepository, times(1)).save(any(AbstractBookEntity.class));
    }
    
    @Test
    @DisplayName("Should trigger search index update when creating book")
    void testCreateBook_WithSearchIndexService_UpdatesIndex() {
        // Given
        BookCreateRequest request = new BookCreateRequest();
        request.setTitle("New Book");
        request.setLanguage(Language.VIETNAMESE);
        request.setAuthorIds(Arrays.asList(1L));
        request.setBookType("PHYSICAL");
        
        when(authorRepository.findByIdIn(anyList())).thenReturn(Arrays.asList(testAuthor));
        when(bookRepository.save(any(AbstractBookEntity.class))).thenAnswer(invocation -> {
            PhysicalBookEntity book = invocation.getArgument(0);
            book.setId(1L);
            book.setCode("NEW-ABC12345");
            return book;
        });
        when(searchIndexService.indexBook(any(AbstractBookEntity.class))).thenReturn(true);
        
        BookDetailDTO bookDTO = BookDetailDTO.builder()
                .id(1L)
                .code("NEW-ABC12345")
                .title("New Book")
                .build();
        when(bookMapper.toDetailDTO(any(AbstractBookEntity.class))).thenReturn(bookDTO);
        
        // When
        catalogService.createBook(request);
        
        // Then
        verify(searchIndexService, times(1)).indexBook(any(AbstractBookEntity.class));
    }
    
    @Test
    @DisplayName("Should handle search index service unavailable gracefully")
    void testCreateBook_WithoutSearchIndexService_NoError() {
        // Given
        // Set searchIndexService to null to simulate service not available
        org.springframework.test.util.ReflectionTestUtils.setField(catalogService, "searchIndexService", null);
        
        BookCreateRequest request = new BookCreateRequest();
        request.setTitle("New Book");
        request.setLanguage(Language.VIETNAMESE);
        request.setAuthorIds(Arrays.asList(1L));
        request.setBookType("PHYSICAL");
        
        when(authorRepository.findByIdIn(anyList())).thenReturn(Arrays.asList(testAuthor));
        when(bookRepository.save(any(AbstractBookEntity.class))).thenAnswer(invocation -> {
            PhysicalBookEntity book = invocation.getArgument(0);
            book.setId(1L);
            book.setCode("NEW-ABC12345");
            return book;
        });
        
        BookDetailDTO bookDTO = BookDetailDTO.builder()
                .id(1L)
                .code("NEW-ABC12345")
                .title("New Book")
                .build();
        when(bookMapper.toDetailDTO(any(AbstractBookEntity.class))).thenReturn(bookDTO);
        
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> catalogService.createBook(request));
    }
}


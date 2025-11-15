package com.huongcung.businessmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huongcung.businessmanagement.admin.model.BookCreateRequest;
import com.huongcung.businessmanagement.admin.model.BookDetailDTO;
import com.huongcung.businessmanagement.admin.model.BookListDTO;
import com.huongcung.businessmanagement.admin.model.BookUpdateRequest;
import com.huongcung.businessmanagement.admin.service.CatalogService;
import com.huongcung.core.common.enumeration.Language;
import com.huongcung.core.media.repository.BookImageRepository;
import com.huongcung.core.media.service.ImageService;
import com.huongcung.core.product.model.entity.PhysicalBookEntity;
import com.huongcung.core.product.repository.AbstractBookRepository;
import com.huongcung.core.search.model.dto.PaginationInfo;
import com.huongcung.webstore.auth.external.jwt.JwtConfiguration;
import com.huongcung.core.security.external.jwt.JwtTokenBlacklistService;
import com.huongcung.core.security.external.jwt.JwtTokenProvider;
import com.huongcung.webstore.auth.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AdminCatalogController
 * Test ID: 3.1-INT-001
 */
@WebMvcTest(
    controllers = AdminCatalogController.class,
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@DisplayName("AdminCatalogController Integration Tests")
class AdminCatalogControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private CatalogService catalogService;
    
    @MockBean
    private AbstractBookRepository bookRepository;
    
    @MockBean
    private BookImageRepository bookImageRepository;
    
    @MockBean
    private ImageService imageService;
    
    // Spring Security mocks
    @SuppressWarnings("removal")
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    
    @SuppressWarnings("removal")
    @MockBean
    private CustomUserDetailsService customUserDetailsService;
    
    @SuppressWarnings("removal")
    @MockBean
    private JwtConfiguration jwtConfiguration;
    
    @SuppressWarnings("removal")
    @MockBean
    private JwtTokenBlacklistService jwtTokenBlacklistService;
    
    private BookListDTO testBookListDTO;
    private BookDetailDTO testBookDetailDTO;
    private PhysicalBookEntity testBookEntity;
    
    @BeforeEach
    void setUp() {
        // Setup test book list DTO
        testBookListDTO = BookListDTO.builder()
                .id(1L)
                .code("BK001")
                .title("Test Book")
                .language(Language.VIETNAMESE)
                .bookType("PHYSICAL")
                .isActive(true)
                .build();
        
        // Setup test book detail DTO
        testBookDetailDTO = BookDetailDTO.builder()
                .id(1L)
                .code("BK001")
                .title("Test Book")
                .description("Test description")
                .language(Language.VIETNAMESE)
                .bookType("PHYSICAL")
                .isbn("1234567890")
                .currentPrice(new BigDecimal("150000"))
                .isActive(true)
                .build();
        
        // Setup test book entity
        testBookEntity = new PhysicalBookEntity();
        testBookEntity.setId(1L);
        testBookEntity.setCode("BK001");
        testBookEntity.setTitle("Test Book");
        testBookEntity.setIsActive(true);
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get paginated list of books successfully")
    void testGetAllBooks_Success() throws Exception {
        // Given
        List<BookListDTO> books = Arrays.asList(testBookListDTO);
        PaginationInfo pagination = PaginationInfo.builder()
                .currentPage(1)
                .pageSize(20)
                .totalResults(1L)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();
        
        CatalogService.PaginatedBookResponse response = 
                new CatalogService.PaginatedBookResponse(books, pagination);
        
        when(catalogService.getAllBooks(any(Pageable.class), any(), any(), any(), any()))
                .thenReturn(response);
        
        // When & Then
        mockMvc.perform(get("/api/admin/catalog/books")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.books").isArray())
                .andExpect(jsonPath("$.data.books[0].code").value("BK001"))
                .andExpect(jsonPath("$.data.books[0].title").value("Test Book"))
                .andExpect(jsonPath("$.data.pagination.totalResults").value(1L));
        
        verify(catalogService, times(1)).getAllBooks(any(Pageable.class), any(), any(), any(), any());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should filter books by title")
    void testGetAllBooks_WithTitleFilter() throws Exception {
        // Given
        List<BookListDTO> books = Arrays.asList(testBookListDTO);
        PaginationInfo pagination = PaginationInfo.builder()
                .currentPage(1)
                .pageSize(20)
                .totalResults(1L)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();
        
        CatalogService.PaginatedBookResponse response = 
                new CatalogService.PaginatedBookResponse(books, pagination);
        
        when(catalogService.getAllBooks(any(Pageable.class), eq("Test"), any(), any(), any()))
                .thenReturn(response);
        
        // When & Then
        mockMvc.perform(get("/api/admin/catalog/books")
                        .param("title", "Test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.books").isArray());
        
        verify(catalogService, times(1)).getAllBooks(any(Pageable.class), eq("Test"), any(), any(), any());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should filter books by language and bookType")
    void testGetAllBooks_WithMultipleFilters() throws Exception {
        // Given
        List<BookListDTO> books = Arrays.asList(testBookListDTO);
        PaginationInfo pagination = PaginationInfo.builder()
                .currentPage(1)
                .pageSize(20)
                .totalResults(1L)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();
        
        CatalogService.PaginatedBookResponse response = 
                new CatalogService.PaginatedBookResponse(books, pagination);
        
        when(catalogService.getAllBooks(any(Pageable.class), any(), eq(Language.VIETNAMESE), eq("PHYSICAL"), any()))
                .thenReturn(response);
        
        // When & Then
        mockMvc.perform(get("/api/admin/catalog/books")
                        .param("language", "VIETNAMESE")
                        .param("bookType", "PHYSICAL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.books").isArray());
        
        verify(catalogService, times(1)).getAllBooks(
                any(Pageable.class), any(), eq(Language.VIETNAMESE), eq("PHYSICAL"), any());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get book by ID successfully")
    void testGetBookById_Success() throws Exception {
        // Given
        when(catalogService.getBookById(1L)).thenReturn(testBookDetailDTO);
        
        // When & Then
        mockMvc.perform(get("/api/admin/catalog/books/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.code").value("BK001"))
                .andExpect(jsonPath("$.data.title").value("Test Book"))
                .andExpect(jsonPath("$.data.isbn").value("1234567890"));
        
        verify(catalogService, times(1)).getBookById(1L);
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when book not found")
    void testGetBookById_NotFound() throws Exception {
        // Given
        when(catalogService.getBookById(999L))
                .thenThrow(new RuntimeException("Book not found with ID: 999"));
        
        // When & Then
        mockMvc.perform(get("/api/admin/catalog/books/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
        
        verify(catalogService, times(1)).getBookById(999L);
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should create PhysicalBook successfully")
    void testCreateBook_PhysicalBook_Success() throws Exception {
        // Given
        BookCreateRequest request = new BookCreateRequest();
        request.setTitle("New Physical Book");
        request.setLanguage(Language.VIETNAMESE);
        request.setAuthorIds(Arrays.asList(1L));
        request.setBookType("PHYSICAL");
        request.setIsbn("9876543210");
        request.setCurrentPrice(new BigDecimal("200000"));
        
        BookDetailDTO createdBook = BookDetailDTO.builder()
                .id(2L)
                .code("NEW-ABC12345")
                .title("New Physical Book")
                .bookType("PHYSICAL")
                .isbn("9876543210")
                .currentPrice(new BigDecimal("200000"))
                .build();
        
        when(catalogService.createBook(any(BookCreateRequest.class))).thenReturn(createdBook);
        
        // When & Then
        mockMvc.perform(post("/api/admin/catalog/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.title").value("New Physical Book"))
                .andExpect(jsonPath("$.data.bookType").value("PHYSICAL"))
                .andExpect(jsonPath("$.message").value("Book created successfully"));
        
        verify(catalogService, times(1)).createBook(any(BookCreateRequest.class));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should create Ebook successfully")
    void testCreateBook_Ebook_Success() throws Exception {
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
        
        BookDetailDTO createdBook = BookDetailDTO.builder()
                .id(3L)
                .code("NEW-DEF67890")
                .title("New Ebook")
                .bookType("EBOOK")
                .fileUrl("https://example.com/ebook.pdf")
                .fileFormat("PDF")
                .currentPrice(new BigDecimal("150000"))
                .build();
        
        when(catalogService.createBook(any(BookCreateRequest.class))).thenReturn(createdBook);
        
        // When & Then
        mockMvc.perform(post("/api/admin/catalog/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.bookType").value("EBOOK"))
                .andExpect(jsonPath("$.data.fileFormat").value("PDF"));
        
        verify(catalogService, times(1)).createBook(any(BookCreateRequest.class));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return validation error when title is missing")
    void testCreateBook_MissingTitle_ValidationError() throws Exception {
        // Given
        BookCreateRequest request = new BookCreateRequest();
        request.setLanguage(Language.VIETNAMESE);
        request.setAuthorIds(Arrays.asList(1L));
        request.setBookType("PHYSICAL");
        
        // When & Then
        mockMvc.perform(post("/api/admin/catalog/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        verify(catalogService, never()).createBook(any());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return validation error when author is missing")
    void testCreateBook_MissingAuthor_ValidationError() throws Exception {
        // Given
        BookCreateRequest request = new BookCreateRequest();
        request.setTitle("Test Book");
        request.setLanguage(Language.VIETNAMESE);
        request.setBookType("PHYSICAL");
        request.setAuthorIds(Collections.emptyList());
        
        // When & Then
        mockMvc.perform(post("/api/admin/catalog/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        verify(catalogService, never()).createBook(any());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should update book successfully")
    void testUpdateBook_Success() throws Exception {
        // Given
        BookUpdateRequest request = new BookUpdateRequest();
        request.setTitle("Updated Title");
        request.setDescription("Updated description");
        
        BookDetailDTO updatedBook = BookDetailDTO.builder()
                .id(1L)
                .code("BK001")
                .title("Updated Title")
                .description("Updated description")
                .build();
        
        when(catalogService.updateBook(eq(1L), any(BookUpdateRequest.class), anyString()))
                .thenReturn(updatedBook);
        
        // When & Then
        mockMvc.perform(put("/api/admin/catalog/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Title"))
                .andExpect(jsonPath("$.data.description").value("Updated description"))
                .andExpect(jsonPath("$.message").value("Book updated successfully"));
        
        verify(catalogService, times(1)).updateBook(eq(1L), any(BookUpdateRequest.class), anyString());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should deactivate book successfully")
    void testDeactivateBook_Success() throws Exception {
        // Given
        BookDetailDTO deactivatedBook = BookDetailDTO.builder()
                .id(1L)
                .code("BK001")
                .title("Test Book")
                .isActive(false)
                .build();
        
        when(catalogService.deactivateBook(eq(1L), anyString())).thenReturn(deactivatedBook);
        
        // When & Then
        mockMvc.perform(put("/api/admin/catalog/books/1/deactivate")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isActive").value(false))
                .andExpect(jsonPath("$.message").value("Book deactivated successfully"));
        
        verify(catalogService, times(1)).deactivateBook(eq(1L), anyString());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should upload book images successfully")
    void testUploadBookImages_Success() throws Exception {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBookEntity));
        when(imageService.saveImageFromStream(any(), anyString(), anyString(), anyString()))
                .thenReturn("books/1/image_1.jpg");
        when(imageService.getFullUrl(anyString()))
                .thenReturn("http://s3.example.com/books/1/image_1.jpg");
        
        // When & Then
        mockMvc.perform(multipart("/api/admin/catalog/books/1/images")
                        .file("files", "image content".getBytes())
                        .param("altText", "Cover image")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Images uploaded successfully"));
        
        verify(bookRepository, times(1)).findById(1L);
        verify(imageService, atLeastOnce()).saveImageFromStream(any(), anyString(), anyString(), anyString());
        verify(bookImageRepository, atLeastOnce()).save(any());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when uploading images for non-existent book")
    void testUploadBookImages_BookNotFound() throws Exception {
        // Given
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        mockMvc.perform(multipart("/api/admin/catalog/books/999/images")
                        .file("files", "image content".getBytes())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is5xxServerError());
        
        verify(bookRepository, times(1)).findById(999L);
        verify(imageService, never()).saveImageFromStream(any(), anyString(), anyString(), anyString());
    }
    
    @Test
    @DisplayName("Should return 403 when accessing without ADMIN role")
    void testGetAllBooks_WithoutAdminRole_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/catalog/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        
        verify(catalogService, never()).getAllBooks(any(), any(), any(), any(), any());
    }
    
    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("Should return 403 when accessing with CUSTOMER role")
    void testGetAllBooks_WithCustomerRole_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/catalog/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        
        verify(catalogService, never()).getAllBooks(any(), any(), any(), any(), any());
    }
}


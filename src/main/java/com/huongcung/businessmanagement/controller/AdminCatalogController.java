package com.huongcung.businessmanagement.controller;

import com.huongcung.businessmanagement.admin.model.*;
import com.huongcung.businessmanagement.admin.service.CatalogService;
import com.huongcung.businessmanagement.admin.service.ContributorService;
import com.huongcung.core.common.enumeration.Language;
import com.huongcung.core.common.model.response.BaseResponse;
import com.huongcung.core.contributor.model.dto.AuthorDTO;
import com.huongcung.core.contributor.model.dto.PublisherDTO;
import com.huongcung.core.contributor.model.dto.TranslatorDTO;
import com.huongcung.core.media.model.entity.BookImageEntity;
import com.huongcung.core.media.repository.BookImageRepository;
import com.huongcung.core.media.service.ImageService;
import com.huongcung.core.product.model.entity.AbstractBookEntity;
import com.huongcung.core.product.repository.AbstractBookRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * REST controller for Admin catalog management operations
 * All endpoints require ADMIN role (enforced by Spring Security /api/admin/** pattern)
 */
@RestController
@RequestMapping("api/admin/catalog/books")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminCatalogController {
    
    private final CatalogService catalogService;
    private final ContributorService contributorService;
    
    /**
     * Get paginated list of all books with optional filtering
     * 
     * @param pageable pagination parameters (page, size, sort) - defaults to page=0, size=20
     * @param title optional filter by title (partial match)
     * @param language optional filter by language
     * @param bookType optional filter by book type (PHYSICAL, EBOOK)
     * @param isActive optional filter by active status
     * @return BaseResponse containing paginated list of BookListDTO with PaginationInfo
     */
    @GetMapping
    public ResponseEntity<BaseResponse> getAllBooks(
            @PageableDefault(size = 20, page = 0) Pageable pageable,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Language language,
            @RequestParam(required = false) String bookType,
            @RequestParam(required = false) Boolean isActive) {
        
        log.debug("Fetching books list - page: {}, size: {}, title: {}, language: {}, bookType: {}, isActive: {}", 
                pageable.getPageNumber(), pageable.getPageSize(), title, language, bookType, isActive);
        
        CatalogService.PaginatedBookResponse response = catalogService.getAllBooks(pageable, title, language, bookType, isActive);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(Map.of(
                        "books", response.books(),
                        "pagination", response.pagination()
                ))
                .build());
    }
    
    /**
     * Get detailed book information by ID
     * 
     * @param id the book ID
     * @return BaseResponse containing BookDetailDTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> getBookById(@PathVariable Long id) {
        log.debug("Fetching book by ID: {}", id);
        
        BookDetailDTO bookDTO = catalogService.getBookById(id);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(bookDTO)
                .build());
    }
    
    /**
     * Create a new book entry
     * 
     * @param request the book creation request
     * @return BaseResponse containing BookDetailDTO with created book information
     */
    @PostMapping
    public ResponseEntity<BaseResponse> createBook(@Valid @RequestBody BookCreateRequest request) {
        log.info("Creating book: title={}, bookType={}", request.getTitle(), request.getBookType());
        
        BookDetailDTO bookDTO = catalogService.createBook(request);
        
        log.info("Book created successfully with ID: {}, code: {}", bookDTO.getId(), bookDTO.getCode());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.builder()
                        .data(bookDTO)
                        .message("Book created successfully")
                        .build());
    }
    
    /**
     * Update existing book entry
     * 
     * @param id the book ID
     * @param request the update request (partial update - only non-null fields are updated)
     * @return BaseResponse containing updated BookDetailDTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookUpdateRequest request) {
        
        String updatedBy = getCurrentAdminEmail();
        log.info("Updating book ID: {}, updatedBy: {}", id, updatedBy);
        
        BookDetailDTO bookDTO = catalogService.updateBook(id, request, updatedBy);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(bookDTO)
                .message("Book updated successfully")
                .build());
    }
    
    /**
     * Deactivate book entry (soft delete)
     * 
     * @param id the book ID
     * @return BaseResponse with success message
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<BaseResponse> deactivateBook(@PathVariable Long id) {
        String deactivatedBy = getCurrentAdminEmail();
        log.info("Deactivating book ID: {}, deactivatedBy: {}", id, deactivatedBy);
        
        BookDetailDTO bookDTO = catalogService.deactivateBook(id, deactivatedBy);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(bookDTO)
                .message("Book deactivated successfully")
                .build());
    }
    
    /**
     * Upload book images using MultipartFile
     * 
     * @param id the book ID
     * @param files array of image files to upload
     * @return BaseResponse with success message
     */
    @PostMapping("/{id}/images")
    public ResponseEntity<BaseResponse> uploadBookImages(
            @PathVariable Long id,
            @RequestParam("files") MultipartFile[] files) {

        catalogService.uploadBookImages(id, files);

        return ResponseEntity.ok(BaseResponse.builder()
                .message("Images uploaded successfully")
                .build());
    }
    
    // ========== Author Endpoints ==========
    
    @GetMapping("/authors")
    public ResponseEntity<BaseResponse> getAllAuthors(
            @PageableDefault(size = 20, page = 0) Pageable pageable,
            @RequestParam(required = false) String name) {
        
        log.debug("Fetching authors list - page: {}, size: {}, name: {}", 
                pageable.getPageNumber(), pageable.getPageSize(), name);
        
        ContributorService.PaginatedAuthorResponse response = contributorService.getAllAuthors(pageable, name);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(Map.of(
                        "authors", response.authors(),
                        "pagination", response.pagination()
                ))
                .build());
    }
    
    @GetMapping("/authors/{id}")
    public ResponseEntity<BaseResponse> getAuthorById(@PathVariable Long id) {
        log.debug("Fetching author by ID: {}", id);
        
        AuthorDTO authorDTO = contributorService.getAuthorById(id);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(authorDTO)
                .build());
    }
    
    @PostMapping("/authors")
    public ResponseEntity<BaseResponse> createAuthor(@Valid @RequestBody AuthorCreateRequest request) {
        log.info("Creating author: name={}", request.getName());
        
        AuthorDTO authorDTO = contributorService.createAuthor(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.builder()
                        .data(authorDTO)
                        .message("Author created successfully")
                        .build());
    }
    
    @PutMapping("/authors/{id}")
    public ResponseEntity<BaseResponse> updateAuthor(
            @PathVariable Long id,
            @Valid @RequestBody AuthorUpdateRequest request) {
        
        log.info("Updating author ID: {}", id);
        
        AuthorDTO authorDTO = contributorService.updateAuthor(id, request);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(authorDTO)
                .message("Author updated successfully")
                .build());
    }
    
    @DeleteMapping("/authors/{id}")
    public ResponseEntity<BaseResponse> deleteAuthor(@PathVariable Long id) {
        log.info("Deleting author ID: {}", id);
        
        contributorService.deleteAuthor(id);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .message("Author deleted successfully")
                .build());
    }
    
    // ========== Translator Endpoints ==========
    
    @GetMapping("/translators")
    public ResponseEntity<BaseResponse> getAllTranslators(
            @PageableDefault(size = 20, page = 0) Pageable pageable,
            @RequestParam(required = false) String name) {
        
        ContributorService.PaginatedTranslatorResponse response = contributorService.getAllTranslators(pageable, name);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(Map.of(
                        "translators", response.translators(),
                        "pagination", response.pagination()
                ))
                .build());
    }
    
    @GetMapping("/translators/{id}")
    public ResponseEntity<BaseResponse> getTranslatorById(@PathVariable Long id) {
        TranslatorDTO translatorDTO = contributorService.getTranslatorById(id);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(translatorDTO)
                .build());
    }
    
    @PostMapping("/translators")
    public ResponseEntity<BaseResponse> createTranslator(@Valid @RequestBody TranslatorCreateRequest request) {
        TranslatorDTO translatorDTO = contributorService.createTranslator(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.builder()
                        .data(translatorDTO)
                        .message("Translator created successfully")
                        .build());
    }
    
    @PutMapping("/translators/{id}")
    public ResponseEntity<BaseResponse> updateTranslator(
            @PathVariable Long id,
            @Valid @RequestBody TranslatorUpdateRequest request) {
        
        TranslatorDTO translatorDTO = contributorService.updateTranslator(id, request);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(translatorDTO)
                .message("Translator updated successfully")
                .build());
    }
    
    @DeleteMapping("/translators/{id}")
    public ResponseEntity<BaseResponse> deleteTranslator(@PathVariable Long id) {
        contributorService.deleteTranslator(id);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .message("Translator deleted successfully")
                .build());
    }
    
    // ========== Publisher Endpoints ==========
    
    @GetMapping("/publishers")
    public ResponseEntity<BaseResponse> getAllPublishers(
            @PageableDefault(size = 20, page = 0) Pageable pageable,
            @RequestParam(required = false) String name) {
        
        ContributorService.PaginatedPublisherResponse response = contributorService.getAllPublishers(pageable, name);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(Map.of(
                        "publishers", response.publishers(),
                        "pagination", response.pagination()
                ))
                .build());
    }
    
    @GetMapping("/publishers/{id}")
    public ResponseEntity<BaseResponse> getPublisherById(@PathVariable Long id) {
        PublisherDTO publisherDTO = contributorService.getPublisherById(id);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(publisherDTO)
                .build());
    }
    
    @PostMapping("/publishers")
    public ResponseEntity<BaseResponse> createPublisher(@Valid @RequestBody PublisherCreateRequest request) {
        PublisherDTO publisherDTO = contributorService.createPublisher(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.builder()
                        .data(publisherDTO)
                        .message("Publisher created successfully")
                        .build());
    }
    
    @PutMapping("/publishers/{id}")
    public ResponseEntity<BaseResponse> updatePublisher(
            @PathVariable Long id,
            @Valid @RequestBody PublisherUpdateRequest request) {
        
        PublisherDTO publisherDTO = contributorService.updatePublisher(id, request);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(publisherDTO)
                .message("Publisher updated successfully")
                .build());
    }
    
    @DeleteMapping("/publishers/{id}")
    public ResponseEntity<BaseResponse> deletePublisher(@PathVariable Long id) {
        contributorService.deletePublisher(id);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .message("Publisher deleted successfully")
                .build());
    }
    
    // ========== Genre Endpoints ==========
    
    @GetMapping("/genres")
    public ResponseEntity<BaseResponse> getAllGenres(
            @PageableDefault(size = 20, page = 0) Pageable pageable,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Boolean isActive) {
        
        ContributorService.PaginatedGenreResponse response = contributorService.getAllGenres(pageable, name, parentId, isActive);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(Map.of(
                        "genres", response.genres(),
                        "pagination", response.pagination()
                ))
                .build());
    }
    
    @GetMapping("/genres/{id}")
    public ResponseEntity<BaseResponse> getGenreById(@PathVariable Long id) {
        GenreListDTO genreDTO = contributorService.getGenreById(id);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(genreDTO)
                .build());
    }
    
    @PostMapping("/genres")
    public ResponseEntity<BaseResponse> createGenre(@Valid @RequestBody GenreCreateRequest request) {
        GenreListDTO genreDTO = contributorService.createGenre(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.builder()
                        .data(genreDTO)
                        .message("Genre created successfully")
                        .build());
    }
    
    @PutMapping("/genres/{id}")
    public ResponseEntity<BaseResponse> updateGenre(
            @PathVariable Long id,
            @Valid @RequestBody GenreUpdateRequest request) {
        
        GenreListDTO genreDTO = contributorService.updateGenre(id, request);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(genreDTO)
                .message("Genre updated successfully")
                .build());
    }
    
    @DeleteMapping("/genres/{id}")
    public ResponseEntity<BaseResponse> deleteGenre(@PathVariable Long id) {
        contributorService.deleteGenre(id);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .message("Genre deleted successfully")
                .build());
    }
    
    /**
     * Extract current admin user email from SecurityContext for audit logging
     * @return the email of the currently authenticated admin user
     */
    private String getCurrentAdminEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName(); // Returns the email (username)
        }
        return "system"; // Fallback if no authentication found
    }
}



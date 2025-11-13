package com.huongcung.businessmanagement.controller;

import com.huongcung.businessmanagement.admin.model.BookCreateRequest;
import com.huongcung.businessmanagement.admin.model.BookDetailDTO;
import com.huongcung.businessmanagement.admin.model.BookUpdateRequest;
import com.huongcung.businessmanagement.admin.service.CatalogService;
import com.huongcung.core.common.enumeration.Language;
import com.huongcung.core.common.model.response.BaseResponse;
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
    private final AbstractBookRepository bookRepository;
    private final BookImageRepository bookImageRepository;
    private final ImageService imageService;
    
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
     * @param altText optional alt text for images
     * @return BaseResponse with success message
     */
    @PostMapping("/{id}/images")
    public ResponseEntity<BaseResponse> uploadBookImages(
            @PathVariable Long id,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(required = false) String altText) {
        
        log.info("Uploading {} images for book ID: {}", files.length, id);
        
        AbstractBookEntity book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + id));
        
        String folderPath = "books/" + id;
        
        // Upload images using ImageService
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            if (file.isEmpty()) {
                continue;
            }
            
            try {
                // Get filename
                String fileName = file.getOriginalFilename();
                if (fileName == null || fileName.isBlank()) {
                    fileName = "image_" + (i + 1) + ".jpg";
                }
                
                // Get content type
                String contentType = file.getContentType();
                if (contentType == null || contentType.isBlank()) {
                    contentType = "image/jpeg";
                }
                
                // Save image to S3 with correct folder path
                String relativePath = imageService.saveImageFromStream(
                    file.getInputStream(),
                    fileName,
                    folderPath,
                    contentType
                );
                
                // Get full URL
                String fullUrl = imageService.getFullUrl(relativePath);
                
                // Create BookImageEntity
                BookImageEntity bookImage = new BookImageEntity();
                bookImage.setBook(book);
                bookImage.setUrl(fullUrl);
                bookImage.setAltText(altText != null ? altText : fileName);
                bookImage.setPosition(i + 1); // Position starts from 1
                
                bookImageRepository.save(bookImage);
                
                log.debug("Image uploaded for book ID: {}, position: {}, url: {}", id, i + 1, fullUrl);
            } catch (Exception e) {
                log.error("Failed to upload image for book ID: {}", id, e);
                throw new RuntimeException("Failed to upload image: " + e.getMessage());
            }
        }
        
        return ResponseEntity.ok(BaseResponse.builder()
                .message("Images uploaded successfully")
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



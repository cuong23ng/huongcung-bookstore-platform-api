package com.huongcung.businessmanagement.admin.service;

import com.huongcung.businessmanagement.admin.model.BookCreateRequest;
import com.huongcung.businessmanagement.admin.model.BookDetailDTO;
import com.huongcung.businessmanagement.admin.model.BookListDTO;
import com.huongcung.businessmanagement.admin.model.BookUpdateRequest;
import com.huongcung.core.common.enumeration.Language;
import com.huongcung.core.search.model.dto.PaginationInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for catalog management operations
 * Handles CRUD operations for books (PhysicalBook and Ebook)
 */
public interface CatalogService {
    
    /**
     * Get paginated list of all books with optional filtering
     * @param pageable pagination parameters (page, size, sort)
     * @param title optional filter by title (partial match)
     * @param language optional filter by language
     * @param bookType optional filter by book type ("PHYSICAL" or "EBOOK")
     * @param isActive optional filter by active status
     * @return PaginatedBookResponse containing list of BookListDTO and PaginationInfo
     */
    PaginatedBookResponse getAllBooks(Pageable pageable, String title, Language language, String bookType, Boolean isActive);
    
    /**
     * Get detailed book information by ID
     * @param id the book ID
     * @return BookDetailDTO containing detailed book information including all relationships
     * @throws RuntimeException if book not found
     */
    BookDetailDTO getBookById(Long id);
    
    /**
     * Create a new book entry
     * @param request the book creation request
     * @return BookDetailDTO containing the created book information
     * @throws IllegalArgumentException if validation fails (missing required fields, invalid bookType, etc.)
     * @throws RuntimeException if code already exists or related entities not found
     */
    BookDetailDTO createBook(BookCreateRequest request);
    
    /**
     * Update existing book entry
     * @param id the book ID
     * @param request the update request (partial update - only non-null fields are updated)
     * @param updatedBy the admin user ID who made the update (for audit logging)
     * @return BookDetailDTO containing updated book information
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException if book not found or related entities not found
     */
    BookDetailDTO updateBook(Long id, BookUpdateRequest request, String updatedBy);

    void uploadBookImages(Long id, MultipartFile[] files);
    
    /**
     * Deactivate book entry (soft delete)
     * @param id the book ID
     * @param deactivatedBy the admin user ID who deactivated the book (for audit logging)
     * @return BookDetailDTO containing deactivated book information
     * @throws RuntimeException if book not found
     */
    BookDetailDTO deactivateBook(Long id, String deactivatedBy);
    
    /**
     * Response wrapper for paginated book list
     */
    record PaginatedBookResponse(List<BookListDTO> books, PaginationInfo pagination) {}
}



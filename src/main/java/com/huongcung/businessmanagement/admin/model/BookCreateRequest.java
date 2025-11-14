package com.huongcung.businessmanagement.admin.model;

import com.huongcung.core.common.enumeration.Language;
import com.huongcung.core.product.enumeration.CoverType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for creating a new book entry
 * Supports both PhysicalBook and Ebook creation based on bookType
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookCreateRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 500, message = "Title must be between 1 and 500 characters")
    private String title;
    
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
    
    @NotNull(message = "Language is required")
    private Language language;
    
    private LocalDate publicationDate;
    
    private Integer pageCount;
    
    private Integer edition;
    
    @NotNull(message = "At least one author is required")
    @Size(min = 1, message = "At least one author is required")
    private List<Long> authorIds; // IDs of AuthorEntity
    
    private List<Long> translatorIds; // IDs of TranslatorEntity
    
    private Long publisherId; // ID of PublisherEntity
    
    private List<Long> genreIds; // IDs of GenreEntity
    
    @NotBlank(message = "Book type is required")
    private String bookType; // "PHYSICAL" or "EBOOK"
    
    // PhysicalBookEntity specific fields
    private String isbn;
    private CoverType coverType;
    private Double weightGrams;
    private String dimensions; // Format: "L x W x H cm"
    private BigDecimal currentPrice;
    
    // EbookEntity specific fields
    private String fileUrl;
    private String fileName;
    private Long fileSize; // in bytes
    private String fileFormat; // PDF, EPUB, MOBI
    
    // Common flags
    private Boolean hasPhysicalEdition = false;
    private Boolean hasElectricEdition = false;
    
    // Images to upload (Base64 encoded)
    private List<BookImageData> images;
}



package com.huongcung.businessmanagement.admin.model;

import com.huongcung.core.common.enumeration.Language;
import com.huongcung.core.product.enumeration.CoverType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for updating an existing book entry
 * All fields are optional for partial updates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookUpdateRequest {
    
    @Size(min = 1, max = 500, message = "Title must be between 1 and 500 characters")
    private String title;
    
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
    
    private Language language;
    
    private LocalDate publicationDate;
    
    private Integer pageCount;
    
    private Integer edition;
    
    private List<Long> authorIds; // IDs of AuthorEntity
    
    private List<Long> translatorIds; // IDs of TranslatorEntity
    
    private Long publisherId; // ID of PublisherEntity
    
    private List<Long> genreIds; // IDs of GenreEntity
    
    // PhysicalBookEntity specific fields
    private String isbn;
    private CoverType coverType;
    private Double weightGrams;
    private String dimensions;
    private BigDecimal currentPrice;
    
    // EbookEntity specific fields
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private String fileFormat;
    
    // Common flags
    private Boolean hasPhysicalEdition;
    private Boolean hasElectricEdition;
    
    private Boolean isActive;
}



package com.huongcung.businessmanagement.admin.model.request;

import com.huongcung.businessmanagement.admin.model.BookImageData;
import com.huongcung.core.common.enumeration.Language;
import com.huongcung.core.catalog.enumeration.CoverType;
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
    private List<Long> authorIds;
    
    private List<Long> translatorIds;
    
    private Long publisherId;
    
    private List<Long> genreIds;

    // Common flags
    private Boolean hasPhysicalEdition = false;
    private Boolean hasElectricEdition = false;
    
    // PhysicalBookEntity specific fields
    private String isbn;
    private CoverType coverType;
    private Integer weightGrams;
    private Integer heightCm;
    private Integer widthCm;
    private Integer lengthCm;
    private BigDecimal physicalBookPrice;
    
    // EbookEntity specific fields
    private String eisbn;
    private BigDecimal ebookPrice;
    
    // Images to upload (Base64 encoded)
    private List<BookImageData> images;
}


